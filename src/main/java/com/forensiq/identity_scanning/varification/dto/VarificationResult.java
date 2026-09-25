package com.forensiq.identity_scanning.varification.dto;

import com.forensiq.identity_scanning.varification.enumeration.VarificationStatus;
import com.forensiq.identity_scanning.varification.enumeration.VarificationType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VarificationResult {
    private String varifierName;
    private VarificationStatus varificationStatus;
    private Double confidence;
    private Double riskScore;
    private String message;
    private VarificationType varificationType;

}
