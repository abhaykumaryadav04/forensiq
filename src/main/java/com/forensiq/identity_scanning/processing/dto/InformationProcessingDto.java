package com.forensiq.identity_scanning.processing.dto;

import com.forensiq.identity_scanning.information.dto.ExtractionResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InformationProcessingDto {
   private boolean successful;
   private String message;
   private ExtractionResponse extractionResponse;
}
