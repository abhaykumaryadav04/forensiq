package com.forensiq.identity_scanning.document.dto;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class DocumentDetectionResult {
    private boolean detected;
    private double confidence;
    private double areaRatio;
    private double rectangularity;
    private double aspectRatio;
    private int x;
    private int y;
    private int width;
    private int height;
    private String message;
}