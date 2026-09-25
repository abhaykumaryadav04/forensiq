package com.forensiq.identity_scanning.processing.dto;

import java.util.List;

import com.forensiq.identity_scanning.information.dto.ExtractionResponse;
import com.forensiq.identity_scanning.ocr.dto.OcrResponse;
import com.forensiq.identity_scanning.processing.enummeration.ProcessingStatus;
import com.forensiq.identity_scanning.varification.mrz.dto.MrzValidationResult;
import com.forensiq.identity_scanning.varification.qr.dto.QrValidationResult;
import com.forensiq.identity_scanning.varification.tempering.dto.TamperingDetectionResult;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DcumentProcessngResult {
    private ProcessingStatus status;
    private String message;
    private String documentId;
    private String documentType;
    private OcrResponse ocrResult;
    private ExtractionResponse extractedInformation;
    private MrzValidationResult mrzValidation;
    private QrValidationResult qrValidation;
    private TamperingDetectionResult tamperingDetection;
    private List<String> warnings;
    private List<String> errors;
    private VarificationProcessingResult varificationProcessingResult;
    private long processTimeTaken;
    private InformationProcessingDto informationProcessingDto;
    private CentralRiskResult centralRiskResult;
}
