package com.forensiq.identity_scanning.ocr.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OcrResponse {
  private String text;
  private double confidence;
  private Long processingTimeMs;
  private List<OcrWord> ocrWords;
}
