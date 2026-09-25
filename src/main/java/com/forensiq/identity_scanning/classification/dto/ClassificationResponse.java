package com.forensiq.identity_scanning.classification.dto;



import java.util.List;

import com.forensiq.identity_scanning.document.entity.DocumentType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassificationResponse {
private DocumentType documentType;
private double confidence;
private String modelVersioString;
private long processingTimeMs;
  private List<ClassificationPrediction> predictions;
}
