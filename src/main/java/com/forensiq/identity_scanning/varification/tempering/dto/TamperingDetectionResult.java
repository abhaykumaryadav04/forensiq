package com.forensiq.identity_scanning.varification.tempering.dto;
import java.util.List;
import com.forensiq.identity_scanning.ai.dto.OllamaTamperingAnalysisResult;
import com.forensiq.identity_scanning.ml.dto.FieldTamperingRisk;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class TamperingDetectionResult {
    private boolean analyzed;
    private boolean suspicious;
    private double tamperingScore;
    private ElaAnalysisResult elaResult;
    private NoiseAnalysisResult noiseResult;
    private CopyMoveAnalysisResult copyMoveResult;
    private MetadataAnalysisResult metadataResult;
    private OllamaTamperingAnalysisResult aiAnalysis;
    private List<TemperingIndicator> indicators;
    private List<String> issues;
    private List<FieldTamperingRisk> fieldTamperingRisks;
}