package com.forensiq.identity_scanning.audit.service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import com.forensiq.identity_scanning.audit.entity.ScreeningAudit;
import com.forensiq.identity_scanning.audit.repo.ScreeningAuditRepo;
import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.processing.dto.FinalVerdictResponse;
@Service
public class ScreeningAuditService {
    private final ScreeningAuditRepo screeningAuditRepo;
    public ScreeningAuditService(ScreeningAuditRepo screeningAuditRepo){
        this.screeningAuditRepo=screeningAuditRepo;
    }
    @Transactional
    public ScreeningAudit recorded(Document document,FinalVerdictResponse response,HttpServletRequest request){
        ScreeningAudit previous=screeningAuditRepo.findTopByOrderByCreatedAtDesc().orElse(null);
        String previousHash=previous!=null?previous.getAuditHash():null;
        String reasons=response.getReasons()!=null?String.join(" | ",response.getReasons()):"";
        String warnings=response.getWarnings()!=null?String.join(" | ",response.getWarnings()):"";
        String createdAt=LocalDateTime.now().toString();
        String ipAddress=request!=null?request.getRemoteAddr():null;
        String payload=buildHashPayload(
                document.getFileHash(),
                response.getDocumentId(),
                response.getDocumentType(),
                response.getRiskScore(),
                response.getVerdict(),
                response.getDecisionConfidence(),
                reasons,
                warnings,
                createdAt,
                previousHash
        );
        String auditHash=sha256(payload);
        ScreeningAudit audit=ScreeningAudit.builder()
                .documentId(document.getId())
                .requestId(document.getScreeningRequest().getRequestId())
                .documentHash(document.getFileHash())
                .documentType(response.getDocumentType())
                .riskScore(response.getRiskScore())
                .verdict(response.getVerdict())
                .decisionConfidence(response.getDecisionConfidence())
                .checksPerformed(response.getChecksPerformed())
                .checksAvailable(response.getChecksAvailable())
                .reasons(reasons)
                .warnings(warnings)
                .createdAt(LocalDateTime.parse(createdAt))
                .ipAddress(ipAddress)
                .auditHash(auditHash)
                .previousAuditHash(previousHash)
                .build();
        return screeningAuditRepo.save(audit);
    }
    private String buildHashPayload(String documentHash,String documentId,String documentType,double riskScore,String verdict,String confidence,String reasons,String warnings,String createdAt,String previousHash){
        return String.join("|",
                nullSafe(documentHash),
                nullSafe(documentId),
                nullSafe(documentType),
                String.valueOf(riskScore),
                nullSafe(verdict),
                nullSafe(confidence),
                nullSafe(reasons),
                nullSafe(warnings),
                nullSafe(createdAt),
                nullSafe(previousHash)
        );
    }
    private String sha256(String value){
        try{
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            byte[] hash=digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result=new StringBuilder();
            for(byte b:hash){
                result.append(String.format("%02x",b));
            }
            return result.toString();
        }catch(Exception e){
            throw new IllegalStateException("Unable to create audit hash",e);
        }
    }
    private String nullSafe(String value){
        return value==null?"":value;
    }
}