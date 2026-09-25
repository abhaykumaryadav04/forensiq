package com.forensiq.identity_scanning.classification.dto;

import com.forensiq.identity_scanning.document.entity.DocumentType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassificationPrediction {
   private double probability;
   private DocumentType documentType;
}
