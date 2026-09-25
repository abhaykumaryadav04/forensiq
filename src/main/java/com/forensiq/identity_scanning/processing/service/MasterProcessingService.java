package com.forensiq.identity_scanning.processing.service;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.document.entity.DocumentPage;
import com.forensiq.identity_scanning.document.entity.DocumentStatus;
import com.forensiq.identity_scanning.document.repo.DocumentPageRepo;
import com.forensiq.identity_scanning.document.repo.DocumentRepo;
import com.forensiq.identity_scanning.document.service.DocumentOcrService;
import com.forensiq.identity_scanning.information.dto.ExtractionResponse;
import com.forensiq.identity_scanning.ocr.dto.OcrResponse;
import com.forensiq.identity_scanning.ocr.dto.OcrWord;
import com.forensiq.identity_scanning.ocr.entity.OcrWordEntity;
import com.forensiq.identity_scanning.ocr.repo.OcrWordRepo;
import com.forensiq.identity_scanning.ocr.service.OcrService;
import com.forensiq.identity_scanning.processing.dto.CentralRiskResult;
import com.forensiq.identity_scanning.processing.dto.DcumentProcessngResult;
import com.forensiq.identity_scanning.processing.dto.InformationProcessingDto;
import com.forensiq.identity_scanning.processing.dto.VarificationProcessingResult;
import com.forensiq.identity_scanning.processing.enummeration.ProcessingStatus;
import com.forensiq.identity_scanning.varification.tempering.TamperingDetectionService;
import com.forensiq.identity_scanning.varification.tempering.dto.TamperingDetectionResult;
@Service
public class MasterProcessingService {
    private final OcrService ocrService;
    private final InformationProcessService informationProcessingService;
    private final VerificationProcessingService verificationProcessingService;
    private final TamperingDetectionService tamperingDetectionService;
    private final CentralRiskEngine centralRiskEngine;
    @Autowired
    private DocumentPageRepo documentPageRepo;
    @Autowired
    private OcrWordRepo ocrWordRepo;
    @Autowired
    private DocumentOcrService documentOcrService;
    @Autowired
    private DocumentRepo documentRepo;
    public MasterProcessingService(OcrService ocrService,InformationProcessService informationProcessingService,VerificationProcessingService verificationProcessingService,TamperingDetectionService tamperingDetectionService,CentralRiskEngine centralRiskEngine){
        this.ocrService=ocrService;
        this.informationProcessingService=informationProcessingService;
        this.verificationProcessingService=verificationProcessingService;
        this.tamperingDetectionService=tamperingDetectionService;
        this.centralRiskEngine=centralRiskEngine;
    }
    public DcumentProcessngResult process(Document document,Path imagePath){
        long startTime=System.currentTimeMillis();
        if(document==null){
            throw new IllegalArgumentException("Document cannot be null");
        }
        if(document.getDocumentType()==null||document.getDocumentType()==com.forensiq.identity_scanning.document.entity.DocumentType.UNKOWN){
            throw new IllegalArgumentException("Document type cannot be determined");
        }
        if(imagePath==null){
            throw new IllegalArgumentException("Image path cannot be null");
        }
        try{
            document.setStatus(DocumentStatus.PROCESSING);
            documentRepo.save(document);
            BufferedImage image=ImageIO.read(imagePath.toFile());
            if(image==null){
                throw new IllegalArgumentException("Unable to read image");
            }
            OcrResponse ocrResponse=loadExistingOcr(document);
            if(ocrResponse==null){
                documentOcrService.documentService(document.getId());
                ocrResponse=loadExistingOcr(document);
            }
            if(ocrResponse==null){
                ocrResponse=ocrService.ocrResponse(imagePath);
            }
            if(ocrResponse==null){
                throw new IllegalStateException("OCR returned null response");
            }
            String ocrText=ocrResponse.getText();
            List<OcrWord> ocrWords=ocrResponse.getOcrWords();
            if(ocrText==null||ocrText.isBlank()){
                throw new IllegalStateException("OCR text is empty");
            }
            if(ocrWords==null||ocrWords.isEmpty()){
                throw new IllegalStateException("OCR words are empty");
            }
            InformationProcessingDto informationResult=informationProcessingService.process(document.getDocumentType(),ocrText,document,ocrWords);
            if(informationResult==null){
                throw new IllegalStateException("Information processing returned null");
            }
            if(!informationResult.isSuccessful()){
                throw new IllegalStateException(informationResult.getMessage());
            }
            ExtractionResponse extractionResponse=informationResult.getExtractionResponse();
            if(extractionResponse==null){
                throw new IllegalStateException("Extraction response cannot be null");
            }
            TamperingDetectionResult tamperingResult=tamperingDetectionService.analyze(imagePath.toFile(),ocrWords,extractionResponse,ocrText);
            if(tamperingResult==null){
                throw new IllegalStateException("Tampering detection returned null");
            }
            VarificationProcessingResult verificationResult=verificationProcessingService.process(ocrWords,document,extractionResponse,ocrText,image,tamperingResult);
            if(verificationResult==null){
                throw new IllegalStateException("Verification processing returned null");
            }
            CentralRiskResult centralRiskResult=centralRiskEngine.calculateRisk(verificationResult,tamperingResult);
            if(centralRiskResult==null){
                throw new IllegalStateException("Central risk engine returned null");
            }
            document.setStatus(DocumentStatus.COMPLETED);
            documentRepo.save(document);
            long processingTime=System.currentTimeMillis()-startTime;
            return DcumentProcessngResult.builder()
                    .status(ProcessingStatus.SUCCESS)
                    .message("Document processed successfully")
                    .documentId(document.getId()!=null?String.valueOf(document.getId()):null)
                    .documentType(document.getDocumentType().name())
                    .ocrResult(ocrResponse)
                    .informationProcessingDto(informationResult)
                    .extractedInformation(extractionResponse)
                    .varificationProcessingResult(verificationResult)
                    .tamperingDetection(tamperingResult)
                    .centralRiskResult(centralRiskResult)
                    .processTimeTaken(processingTime)
                    .build();
        }catch(Exception e){
            document.setStatus(DocumentStatus.FAILED);
            documentRepo.save(document);
            return DcumentProcessngResult.builder()
                    .status(ProcessingStatus.FAILED)
                    .message("Document processing failed: "+e.getMessage())
                    .documentId(document.getId()!=null?String.valueOf(document.getId()):null)
                    .documentType(document.getDocumentType()!=null?document.getDocumentType().name():null)
                    .errors(List.of(e.getMessage()!=null?e.getMessage():"Unknown processing error"))
                    .processTimeTaken(System.currentTimeMillis()-startTime)
                    .build();
        }
    }
    private OcrResponse loadExistingOcr(Document document){
        List<DocumentPage> pages=documentPageRepo.findByDocumentIdOrderByPageNumber(document.getId());
        if(pages.isEmpty()){
            return null;
        }
        DocumentPage page=pages.get(0);
        if(page.getOcrText()==null||page.getOcrText().isBlank()){
            return null;
        }
        List<OcrWordEntity> entities=ocrWordRepo.findByDocumentPageId(page.getId());
        if(entities.isEmpty()){
            return null;
        }
        List<OcrWord> words=new ArrayList<>();
        for(OcrWordEntity entity:entities){
            words.add(OcrWord.builder()
                    .text(entity.getText())
                    .confidence(entity.getConfidence())
                    .x(entity.getX())
                    .y(entity.getY())
                    .width(entity.getWidth())
                    .height(entity.getHeight())
                    .build());
        }
        return OcrResponse.builder()
                .text(page.getOcrText())
                .confidence(page.getOcrConfidence()!=null?page.getOcrConfidence():0)
                .processingTimeMs(page.getOcrProcessingTimeMs())
                .ocrWords(words)
                .build();
    }
}