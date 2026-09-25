package com.forensiq.identity_scanning.varification.tempering.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CopyMoveAnalysisResult {

    private boolean analyzed;

    private int suspiciousMatches;

    private double suspicionScore;

    private boolean suspicious;

    private String message;
}
