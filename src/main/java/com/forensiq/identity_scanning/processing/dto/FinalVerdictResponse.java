package com.forensiq.identity_scanning.processing.dto;
import java.util.List;
import com.forensiq.identity_scanning.information.dto.ExtractionResponse;
import com.forensiq.identity_scanning.varification.tempering.dto.TamperingDetectionResult;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class FinalVerdictResponse {
    private String documentId;
    private String documentType;
    private String status;
    private double riskScore;
    private String verdict;
    private String decisionConfidence;
    private int checksPerformed;
    private int checksAvailable;
    private List<String> reasons;
    private List<String> warnings;
    private ExtractionResponse extractedInformation;
    private VarificationProcessingResult verification;
    private TamperingDetectionResult tamperingAnalysis;
    private long processingTimeTaken;
    private boolean cachedResult;
}