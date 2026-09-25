package com.forensiq.identity_scanning.varification.tempering.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TemperingIndicator {
   private String analyzerName;

    private String description;

    private double suspicionScore;

    private boolean suspicious;
}
