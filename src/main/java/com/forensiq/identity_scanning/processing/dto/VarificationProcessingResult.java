package com.forensiq.identity_scanning.processing.dto;
import java.util.List;
import com.forensiq.identity_scanning.varification.dto.VarificationResult;
import com.forensiq.identity_scanning.varification.enumeration.VarificationStatus;
import com.forensiq.identity_scanning.varification.mrz.dto.MrzValidationResult;
import com.forensiq.identity_scanning.varification.qr.dto.QrValidationResult;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class VarificationProcessingResult {
    private MrzValidationResult mrzValidatinResult;
    private QrValidationResult qrvalidationResult;
    private VarificationStatus crossfieldCheck;
    private VarificationResult varificationResult;
    private double crossFieldRiskScore;
    private int crossFieldChecksPerformed;
    private int crossFieldChecksPassed;
    private List<String> crossFieldIssues;
    private List<String> crossFieldWarnings;
    private List<String> crossFieldEvidence;
}