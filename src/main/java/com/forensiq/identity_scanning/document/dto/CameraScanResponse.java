package com.forensiq.identity_scanning.document.dto;
import com.forensiq.identity_scanning.processing.dto.FinalVerdictResponse;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class CameraScanResponse {
    private DocumentResponse document;
    private ImageQualityResult imageQuality;
    private DocumentDetectionResult documentDetection;
    private boolean accepted;
    private String message;
    private FinalVerdictResponse finalVerdict;
}