package com.forensiq.identity_scanning.ai.dto;
import java.util.List;
import lombok.Data;
@Data
public class OllamaDocumentAnalysisResult {
    private String documentType;
    private double confidence;
    private boolean documentDetected;
    private List<String> observations;
    private List<String> suspiciousAreas;
    private String summary;
}