package com.forensiq.identity_scanning.ocr.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OcrBenchmarkResult {

    private String imageName;

    private double cer;

    private double wer;

    private double confidence;

    private long processingTimeMs;

    private String rawText;

    private String processedText;
}