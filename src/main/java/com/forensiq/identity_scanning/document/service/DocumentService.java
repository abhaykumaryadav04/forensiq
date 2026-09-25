package com.forensiq.identity_scanning.document.service;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.forensiq.identity_scanning.classification.dto.ClassificationResponse;
import com.forensiq.identity_scanning.classification.service.ClassificationService;
import com.forensiq.identity_scanning.common.util.FileHashUtil;
import com.forensiq.identity_scanning.document.dto.CameraScanResponse;
import com.forensiq.identity_scanning.document.dto.DocumentDetectionResult;
import com.forensiq.identity_scanning.document.dto.DocumentResponse;
import com.forensiq.identity_scanning.document.dto.ImageQualityResult;
import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.document.entity.DocumentPage;
import com.forensiq.identity_scanning.document.entity.DocumentStatus;
import com.forensiq.identity_scanning.document.entity.DocumentType;
import com.forensiq.identity_scanning.document.entity.PageStatus;
import com.forensiq.identity_scanning.document.entity.ScreeningRequest;
import com.forensiq.identity_scanning.document.repo.DocumentPageRepo;
import com.forensiq.identity_scanning.document.repo.DocumentRepo;
import com.forensiq.identity_scanning.document.repo.ScreeningRequestRepo;
import jakarta.transaction.Transactional;
@Service
public class DocumentService {
    @Autowired
    private DocumentValidationService documentValidationService;
    @Autowired
    private DocumentRepo documentRepo;
    @Autowired
    private ScreeningRequestRepo screeningRequestRepo;
    @Autowired
    private DocumentPageRepo documentPageRepo;
    @Autowired
    private DocumentPageService documentPageService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ImageQualityService imageQualityService;
    @Autowired
    private DocumentDetectionService documentDetectionService;
    @Autowired
    private DocumentOcrService documentOcrService;
    @Autowired
    private ClassificationService classificationService;
    @Transactional
    public Document upload(String requestId,MultipartFile file,DocumentType documentType) throws Exception{
        documentValidationService.validation(file);
        ScreeningRequest request=screeningRequestRepo.findByRequestId(requestId).orElseThrow(()->new Exception("Screening request not found"));
        String hash=FileHashUtil.sha256(file);
        Document existingDocument=documentRepo.findByFileHash(hash).orElse(null);
        if(existingDocument!=null){
            return existingDocument;
        }
        String fileId=UUID.randomUUID().toString();
        Path path=fileStorageService.store(fileId,file);
        Document document=Document.builder()
                .fileName(file.getOriginalFilename())
                .fileHash(hash)
                .fileSize(file.getSize())
                .documentType(documentType)
                .mimeType(file.getContentType())
                .screeningRequest(request)
                .status(DocumentStatus.UPLOAD)
                .storagePath(path.toString())
                .uploadedAt(LocalDateTime.now())
                .build();
        document=documentRepo.save(document);
        documentPageService.createImage(document);
        return document;
    }
    @Transactional
    public CameraScanResponse uploadCameraImage(String requestId,MultipartFile image) throws Exception{
        documentValidationService.validateCameraImage(image);
        ScreeningRequest request=screeningRequestRepo.findByRequestId(requestId).orElseThrow(()->new Exception("Screening request not found"));
        String hash=FileHashUtil.sha256(image);
        Document document=documentRepo.findByFileHash(hash).orElse(null);
        if(document==null){
            String fileId=UUID.randomUUID().toString();
            Path path=fileStorageService.store(fileId,image);
            document=Document.builder()
                    .fileName(image.getOriginalFilename())
                    .fileHash(hash)
                    .fileSize(image.getSize())
                    .documentType(DocumentType.UNKOWN)
                    .mimeType(image.getContentType())
                    .screeningRequest(request)
                    .status(DocumentStatus.VALIDATING)
                    .storagePath(path.toString())
                    .uploadedAt(LocalDateTime.now())
                    .build();
            document=documentRepo.save(document);
            documentPageService.createImage(document);
        }
        return prepareCameraDocument(document);
    }
    private CameraScanResponse prepareCameraDocument(Document document) throws Exception{
        Path path=Path.of(document.getStoragePath());
        ImageQualityResult qualityResult=imageQualityService.analyze(path);
        DocumentDetectionResult detectionResult=documentDetectionService.detect(path);
        boolean accepted=qualityResult.isAcceptable()&&detectionResult.isDetected();
        if(!accepted){
            document.setStatus(DocumentStatus.FAILED);
            documentRepo.save(document);
            return CameraScanResponse.builder()
                    .document(buildDocumentResponse(document))
                    .imageQuality(qualityResult)
                    .documentDetection(detectionResult)
                    .accepted(false)
                    .message("Camera image rejected; capture again with the document clearly visible")
                    .build();
        }
        document.setStatus(DocumentStatus.PROCESSING);
        documentRepo.save(document);
        List<DocumentPage> pages=documentPageRepo.findByDocumentIdOrderByPageNumber(document.getId());
        if(pages.isEmpty()){
            document.setStatus(DocumentStatus.FAILED);
            documentRepo.save(document);
            throw new IllegalStateException("No document page found");
        }
        DocumentPage page=pages.get(0);
        if(page.getStatus()!=PageStatus.OCR_COMPLETED||page.getOcrText()==null||page.getOcrText().isBlank()){
            documentOcrService.documentService(document.getId());
            pages=documentPageRepo.findByDocumentIdOrderByPageNumber(document.getId());
            if(pages.isEmpty()){
                document.setStatus(DocumentStatus.FAILED);
                documentRepo.save(document);
                throw new IllegalStateException("No document page found after OCR");
            }
            page=pages.get(0);
        }
        if(document.getDocumentType()==null||document.getDocumentType()==DocumentType.UNKOWN){
            ClassificationResponse classificationResponse=classificationService.documentPageClassify(page.getId());
            if(classificationResponse!=null&&classificationResponse.getDocumentType()!=null&&classificationResponse.getDocumentType()!=DocumentType.UNKOWN){
                document.setDocumentType(classificationResponse.getDocumentType());
            }
        }
        if(document.getDocumentType()==null||document.getDocumentType()==DocumentType.UNKOWN){
            document.setStatus(DocumentStatus.FAILED);
            documentRepo.save(document);
            return CameraScanResponse.builder()
                    .document(buildDocumentResponse(document))
                    .imageQuality(qualityResult)
                    .documentDetection(detectionResult)
                    .accepted(false)
                    .message("Document type could not be determined")
                    .build();
        }
        document.setStatus(DocumentStatus.READY_FOR_PROCESSING);
        documentRepo.save(document);
        return CameraScanResponse.builder()
                .document(buildDocumentResponse(document))
                .imageQuality(qualityResult)
                .documentDetection(detectionResult)
                .accepted(true)
                .message("Image accepted and document type detected as "+document.getDocumentType())
                .build();
    }
    private DocumentResponse buildDocumentResponse(Document document){
        return DocumentResponse.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .mimeType(document.getMimeType())
                .fileSize(document.getFileSize())
                .documentType(document.getDocumentType())
                .status(document.getStatus())
                .uploadedAt(document.getUploadedAt())
                .requestId(document.getScreeningRequest().getRequestId())
                .build();
    }
}