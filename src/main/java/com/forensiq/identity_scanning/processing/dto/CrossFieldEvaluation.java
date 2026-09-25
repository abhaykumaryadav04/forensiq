package com.forensiq.identity_scanning.processing.dto;
import java.util.List;
import com.forensiq.identity_scanning.varification.dto.VarificationResult;
import com.forensiq.identity_scanning.varification.enumeration.VarificationStatus;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class CrossFieldEvaluation {
    private VarificationResult result;
    private VarificationStatus status;
    private double riskScore;
    private int checksPerformed;
    private int checksPassed;
    private List<String> issues;
    private List<String> warnings;
    private List<String> evidence;
}