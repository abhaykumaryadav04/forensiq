package com.forensiq.identity_scanning.ml.dto;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class FieldTamperingRisk {
    private int regionId;
    private String affectedField;
    private String affectedText;
    private double fieldRiskScore;
    private String riskLevel;
    private double mappingConfidence;
    private double regionConfidence;
    private String attackType;
    private double attackConfidence;
    private String reason;

}