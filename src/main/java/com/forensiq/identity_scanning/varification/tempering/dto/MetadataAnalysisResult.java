package com.forensiq.identity_scanning.varification.tempering.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetadataAnalysisResult {
private boolean analyzed;

    private boolean suspicious;

    private double suspicionScore;

    private List<String> findings;

    private String message;
}
