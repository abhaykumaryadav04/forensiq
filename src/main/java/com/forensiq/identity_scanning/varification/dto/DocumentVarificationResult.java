package com.forensiq.identity_scanning.varification.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentVarificationResult {
    private Long documentId;
    private List<VarificationResult> varificationResults;
    private Double authenticityScore;
    private Double riskScore;
    private String finalVerdit;
    private Long processTimeMs;


}
