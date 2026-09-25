package com.forensiq.identity_scanning.document.dto;
import java.util.List;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class ImageQualityResult {
    private boolean acceptable;
    private double qualityScore;
    private int width;
    private int height;
    private double blurScore;
    private double brightness;
    private double contrast;
    private List<String> issues;
}