package com.forensiq.identity_scanning.processing.dto;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class CentralRiskResult {
    private double riskScore;
    private String verdict;
    private List<String> reasons;
    private List<String> warnings;
    private int checksAvailable;
    private int checksPerformed;
    private String decisionConfidence;
    private Map<String,Double> componentRiskScores;
}