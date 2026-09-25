package com.forensiq.identity_scanning.processing.controller;
import java.nio.file.Path;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.forensiq.identity_scanning.audit.service.ScreeningAuditService;
import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.document.entity.DocumentType;
import com.forensiq.identity_scanning.document.service.DocumentService;
import com.forensiq.identity_scanning.processing.dto.DcumentProcessngResult;
import com.forensiq.identity_scanning.processing.dto.FinalVerdictResponse;
import com.forensiq.identity_scanning.processing.enummeration.ProcessingStatus;
import com.forensiq.identity_scanning.processing.service.FinalVerdictMapper;
import com.forensiq.identity_scanning.processing.service.MasterProcessingService;
import com.forensiq.identity_scanning.result.service.ScreeningResultCacheService;
import jakarta.servlet.http.HttpServletRequest;
@RestController
@RequestMapping("/api/v1/documents")
public class MasterProcessingController {
    private final MasterProcessingService masterProcessingService;
    private final DocumentService documentService;
    private final FinalVerdictMapper finalVerdictMapper;
    private final ScreeningResultCacheService screeningResultCacheService;
    private final ScreeningAuditService screeningAuditService;
    private final HttpServletRequest httpServletRequest;
    public MasterProcessingController(MasterProcessingService masterProcessingService,DocumentService documentService,FinalVerdictMapper finalVerdictMapper,ScreeningResultCacheService screeningResultCacheService,ScreeningAuditService screeningAuditService,HttpServletRequest httpServletRequest){
        this.masterProcessingService=masterProcessingService;
        this.documentService=documentService;
        this.finalVerdictMapper=finalVerdictMapper;
        this.screeningResultCacheService=screeningResultCacheService;
        this.screeningAuditService=screeningAuditService;
        this.httpServletRequest=httpServletRequest;
    }
    @PostMapping(value="/{requestId}/process",consumes="multipart/form-data")
    public ResponseEntity<FinalVerdictResponse> processDocument(@RequestParam("file") MultipartFile file,@RequestParam("documentType") DocumentType documentType,@PathVariable String requestId){
        try{
            if(file==null||file.isEmpty()){
                return ResponseEntity.badRequest()
                        .body(FinalVerdictResponse.builder()
                                .status(ProcessingStatus.FAILED.name())
                                .reasons(java.util.List.of("File is empty"))
                                .cachedResult(false)
                                .build());
            }
            if(documentType==null){
                return ResponseEntity.badRequest()
                        .body(FinalVerdictResponse.builder()
                                .status(ProcessingStatus.FAILED.name())
                                .reasons(java.util.List.of("Document type is required"))
                                .cachedResult(false)
                                .build());
            }
            Document document=documentService.upload(requestId,file,documentType);
            if(document==null||document.getId()==null){
                throw new IllegalStateException("Document was not saved");
            }
            FinalVerdictResponse cachedResponse=screeningResultCacheService.findCachedResult(document);
            if(cachedResponse!=null){
                cachedResponse.setCachedResult(true);
                return ResponseEntity.ok(cachedResponse);
            }
            Path imagePath=Path.of(document.getStoragePath());
            DcumentProcessngResult processingResult=masterProcessingService.process(document,imagePath);
            FinalVerdictResponse finalResponse=finalVerdictMapper.map(processingResult);
            finalResponse.setCachedResult(false);
            if(processingResult.getStatus()==ProcessingStatus.SUCCESS){
                screeningResultCacheService.save(document,finalResponse);
                screeningAuditService.recorded(document,finalResponse,httpServletRequest);
                return ResponseEntity.ok(finalResponse);
            }
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(finalResponse);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FinalVerdictResponse.builder()
                            .status(ProcessingStatus.FAILED.name())
                            .reasons(java.util.List.of(e.getMessage()!=null?e.getMessage():"Document processing failed"))
                            .cachedResult(false)
                            .build());
        }
    }
}