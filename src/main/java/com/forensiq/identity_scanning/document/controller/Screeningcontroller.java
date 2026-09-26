package com.forensiq.identity_scanning.document.controller;

import java.nio.file.Path;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.forensiq.identity_scanning.audit.service.ScreeningAuditService;
import com.forensiq.identity_scanning.classification.dto.ClassificationResponse;
import com.forensiq.identity_scanning.classification.service.ClassificationService;
import com.forensiq.identity_scanning.common.util.FileHashUtil;
import com.forensiq.identity_scanning.document.dto.CameraScanResponse;
import com.forensiq.identity_scanning.document.dto.DocumentResponse;
import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.document.entity.DocumentPage;
import com.forensiq.identity_scanning.document.entity.DocumentType;
import com.forensiq.identity_scanning.document.entity.ScreeningRequest;
import com.forensiq.identity_scanning.document.entity.ScreeningStatus;
import com.forensiq.identity_scanning.document.repo.DocumentPageRepo;
import com.forensiq.identity_scanning.document.repo.DocumentRepo;
import com.forensiq.identity_scanning.document.repo.ScreeningRequestRepo;
import com.forensiq.identity_scanning.document.service.DocumentOcrService;
import com.forensiq.identity_scanning.document.service.DocumentService;
import com.forensiq.identity_scanning.processing.dto.DcumentProcessngResult;
import com.forensiq.identity_scanning.processing.dto.FinalVerdictResponse;
import com.forensiq.identity_scanning.processing.enummeration.ProcessingStatus;
import com.forensiq.identity_scanning.processing.service.FinalVerdictMapper;
import com.forensiq.identity_scanning.processing.service.MasterProcessingService;
import com.forensiq.identity_scanning.result.service.ScreeningResultCacheService;
import jakarta.servlet.http.HttpServletRequest;
@RestController
@RequestMapping("/api/v1/screenigs")
public class Screeningcontroller {
    private final ScreeningRequestRepo screeningRequestRepo;
    private final DocumentService documentService;
    private final DocumentOcrService documentOcrService;
    private final DocumentRepo documentRepo;
    private final DocumentPageRepo documentPageRepo;
    private final ClassificationService classificationService;
    private final MasterProcessingService masterProcessingService;
    private final FinalVerdictMapper finalVerdictMapper;
    private final ScreeningResultCacheService screeningResultCacheService;
    private final ScreeningAuditService screeningAuditService;
    private final HttpServletRequest httpServletRequest;
    public Screeningcontroller(ScreeningRequestRepo screeningRequestRepo,DocumentService documentService,DocumentOcrService documentOcrService,DocumentRepo documentRepo,DocumentPageRepo documentPageRepo,ClassificationService classificationService,MasterProcessingService masterProcessingService,FinalVerdictMapper finalVerdictMapper,ScreeningResultCacheService screeningResultCacheService,ScreeningAuditService screeningAuditService,HttpServletRequest httpServletRequest){
        this.screeningRequestRepo=screeningRequestRepo;
        this.documentService=documentService;
        this.documentOcrService=documentOcrService;
        this.documentRepo=documentRepo;
        this.documentPageRepo=documentPageRepo;
        this.classificationService=classificationService;
        this.masterProcessingService=masterProcessingService;
        this.finalVerdictMapper=finalVerdictMapper;
        this.screeningResultCacheService=screeningResultCacheService;
        this.screeningAuditService=screeningAuditService;
        this.httpServletRequest=httpServletRequest;
    }
    @PostMapping
    public ResponseEntity<ScreeningRequest> createScanning(){
        ScreeningRequest request=ScreeningRequest.builder()
                .status(ScreeningStatus.CREATED)
                .build();
        return ResponseEntity.ok(screeningRequestRepo.save(request));
    }
    @PostMapping(value="/{requestId}/documents",consumes="multipart/form-data")
    public ResponseEntity<DocumentResponse> uploadDocument(@PathVariable String requestId,@RequestParam("file") MultipartFile file,@RequestParam("documentType") DocumentType documentType){
        try{
            if(file==null||file.isEmpty()){
                return ResponseEntity.badRequest().build();
            }
            if(documentType==null){
                return ResponseEntity.badRequest().build();
            }
            Document document=documentService.upload(requestId,file,documentType);
            DocumentResponse response=buildDocumentResponse(document);
            return ResponseEntity.ok(response);
        }catch(Exception exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping(value="/{requestId}/camera",consumes="multipart/form-data")
    public ResponseEntity<CameraScanResponse> uploadCameraImage(@PathVariable String requestId,@RequestParam("image") MultipartFile image){
        try{
            if(image==null||image.isEmpty()){
                return ResponseEntity.badRequest()
                        .body(CameraScanResponse.builder()
                                .accepted(false)
                                .message("Camera image is empty")
                                .build());
            }
            String hash=FileHashUtil.sha256(image);
            FinalVerdictResponse cachedResult=screeningResultCacheService.findCachedResultByHash(hash);
            if(cachedResult!=null){
                Document existingDocument=documentRepo.findByFileHash(hash).orElse(null);
                return ResponseEntity.ok(
                        CameraScanResponse.builder()
                                .document(existingDocument!=null?buildDocumentResponse(existingDocument):null)
                                .accepted(true)
                                .message("Previously processed result returned")
                                .finalVerdict(cachedResult)
                                .build()
                );
            }
            Document existingDocument=documentRepo.findByFileHash(hash).orElse(null);
            CameraScanResponse cameraResponse;
            Document document;
            if(existingDocument!=null){
                document=existingDocument;
                cameraResponse=CameraScanResponse.builder()
                        .document(buildDocumentResponse(document))
                        .accepted(true)
                        .message("Previously uploaded document found; continuing screening")
                        .build();
            }else{
                cameraResponse=documentService.uploadCameraImage(requestId,image);
                if(!cameraResponse.isAccepted()){
                    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(cameraResponse);
                }
                document=documentRepo.findById(cameraResponse.getDocument().getId())
                        .orElseThrow(()->new RuntimeException("Document not found after camera upload"));
            }
            List<DocumentPage> pages=documentPageRepo.findByDocumentIdOrderByPageNumber(document.getId());
            if(pages.isEmpty()){
                documentOcrService.documentService(document.getId());
                pages=documentPageRepo.findByDocumentIdOrderByPageNumber(document.getId());
            }
            if(pages.isEmpty()){
                throw new RuntimeException("No document page found");
            }
            DocumentPage page=pages.get(0);
            if(page.getOcrText()==null||page.getOcrText().isBlank()){
                documentOcrService.documentService(document.getId());
                pages=documentPageRepo.findByDocumentIdOrderByPageNumber(document.getId());
                page=pages.get(0);
            }
            if(document.getDocumentType()==null||document.getDocumentType()==DocumentType.UNKOWN){
                ClassificationResponse classificationResponse=classificationService.documentPageClassify(page.getId());
                if(classificationResponse!=null&&classificationResponse.getDocumentType()!=null&&classificationResponse.getDocumentType()!=DocumentType.UNKOWN){
                    document.setDocumentType(classificationResponse.getDocumentType());
                    documentRepo.save(document);
                }
            }
            if(document.getDocumentType()==null||document.getDocumentType()==DocumentType.UNKOWN){
                cameraResponse.setAccepted(false);
                cameraResponse.setMessage("Document type could not be determined");
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(cameraResponse);
            }
            Path imagePath=Path.of(document.getStoragePath());
            DcumentProcessngResult processingResult=masterProcessingService.process(document,imagePath);
            FinalVerdictResponse finalResponse=finalVerdictMapper.map(processingResult);
            finalResponse.setCachedResult(false);
            if(processingResult.getStatus()==ProcessingStatus.SUCCESS){
                screeningResultCacheService.save(document,finalResponse);
                screeningAuditService.recorded(document,finalResponse,httpServletRequest);
                cameraResponse.setDocument(buildDocumentResponse(document));
                cameraResponse.setAccepted(true);
                cameraResponse.setFinalVerdict(finalResponse);
                cameraResponse.setMessage("Document screening completed successfully");
                return ResponseEntity.ok(cameraResponse);
            }
            cameraResponse.setDocument(buildDocumentResponse(document));
            cameraResponse.setAccepted(false);
            cameraResponse.setFinalVerdict(finalResponse);
            cameraResponse.setMessage("Document screening failed");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(cameraResponse);
        }catch(Exception exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CameraScanResponse.builder()
                            .accepted(false)
                            .message("Camera screening failed: "+(exception.getMessage()!=null?exception.getMessage():"Unknown error"))
                            .build());
        }
    }
    @PostMapping("/documents/{documentId}")
    public ResponseEntity<String> processDocument(@PathVariable Long documentId){
        try{
            documentOcrService.documentService(documentId);
            return ResponseEntity.ok("OCR Complete");
        }catch(Exception exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("OCR failed: "+(exception.getMessage()!=null?exception.getMessage():"Unknown error"));
        }
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