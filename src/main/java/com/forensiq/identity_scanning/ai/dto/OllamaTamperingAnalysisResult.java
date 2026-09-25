package com.forensiq.identity_scanning.ai.dto;
import java.util.List;
import lombok.Data;
@Data
public class OllamaTamperingAnalysisResult {
    private boolean suspicious;
    private double tamperingScore;
    private String attackType;
    private double attackConfidence;
    private List<String> suspiciousFields;
    private List<String> observations;
    private String reasoning;
}