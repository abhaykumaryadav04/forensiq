package com.forensiq.identity_scanning.varification.tempering.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ElaAnalysisResult {
 private boolean analyzed;

    private double averageErrorLevel;

    private double maximumErrorLevel;

    private double suspicionScore;

    private boolean suspicious;

    private String message;
}
