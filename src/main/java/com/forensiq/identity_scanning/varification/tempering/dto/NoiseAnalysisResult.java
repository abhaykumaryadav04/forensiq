package com.forensiq.identity_scanning.varification.tempering.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoiseAnalysisResult {
     private boolean analyzed;

    private double averageNoise;

    private double noiseVariation;

    private double suspicionScore;

    private boolean suspicious;

    private String message;

}
