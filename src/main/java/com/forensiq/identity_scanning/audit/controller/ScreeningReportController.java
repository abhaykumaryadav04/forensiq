package com.forensiq.identity_scanning.audit.controller;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.forensiq.identity_scanning.audit.entity.ScreeningAudit;
import com.forensiq.identity_scanning.audit.repo.ScreeningAuditRepo;
import com.forensiq.identity_scanning.audit.service.ScreeningReportService;


@RestController
@RequestMapping("/api/v1/reports")
public class ScreeningReportController {
    private final ScreeningAuditRepo screeningAuditRepo;
    private final ScreeningReportService screeningReportService;
    public ScreeningReportController(ScreeningAuditRepo screeningAuditRepo,ScreeningReportService screeningReportService){
        this.screeningAuditRepo=screeningAuditRepo;
        this.screeningReportService=screeningReportService;
    }
    @GetMapping(value="/{documentId}",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateReport(@PathVariable Long documentId) throws Exception{
        ScreeningAudit audit=screeningAuditRepo.findTopByDocumentIdOrderByCreatedAtDesc(documentId)
                .orElseThrow(()->new RuntimeException("Audit record not found"));
        byte[] pdf=screeningReportService.generate(audit);
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("ForensiQ-Report-"+documentId+".pdf")
                        .build()
        );
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}