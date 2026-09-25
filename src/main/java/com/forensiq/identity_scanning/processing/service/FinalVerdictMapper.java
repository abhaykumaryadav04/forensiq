package com.forensiq.identity_scanning.processing.service;
import org.springframework.stereotype.Service;
import com.forensiq.identity_scanning.processing.dto.CentralRiskResult;
import com.forensiq.identity_scanning.processing.dto.DcumentProcessngResult;
import com.forensiq.identity_scanning.processing.dto.FinalVerdictResponse;
@Service
public class FinalVerdictMapper {
    public FinalVerdictResponse map(DcumentProcessngResult result){
        if(result==null){
            throw new IllegalArgumentException("Processing result cannot be null");
        }
        CentralRiskResult risk=result.getCentralRiskResult();
        if(risk==null){
            return FinalVerdictResponse.builder()
                    .documentId(result.getDocumentId())
                    .documentType(result.getDocumentType())
                    .status(result.getStatus()!=null?result.getStatus().name():null)
                    .reasons(result.getErrors())
                    .warnings(result.getWarnings())
                    .processingTimeTaken(result.getProcessTimeTaken())
                    .cachedResult(false)
                    .build();
        }
        return FinalVerdictResponse.builder()
                .documentId(result.getDocumentId())
                .documentType(result.getDocumentType())
                .status(result.getStatus()!=null?result.getStatus().name():null)
                .riskScore(risk.getRiskScore())
                .verdict(risk.getVerdict())
                .decisionConfidence(risk.getDecisionConfidence())
                .checksPerformed(risk.getChecksPerformed())
                .checksAvailable(risk.getChecksAvailable())
                .reasons(risk.getReasons())
                .warnings(risk.getWarnings())
                .extractedInformation(result.getExtractedInformation())
                .verification(result.getVarificationProcessingResult())
                .tamperingAnalysis(result.getTamperingDetection())
                .processingTimeTaken(result.getProcessTimeTaken())
                .cachedResult(false)
                .build();
    }
}