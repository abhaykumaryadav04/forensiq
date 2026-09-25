package com.forensiq.identity_scanning.ocr.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OcrBenchmarkResponse {

    private String imageName;

    private double rawCER;

    private double processedCER;

    private double rawWER;

    private double processedWER;

    private double rawConfidence;

    private double processedConfidence;

    private long rawProcessingTimeMs;

    private long processedProcessingTimeMs;
}