package com.forensiq.identity_scanning.varification.qr.dto;


import java.util.List;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QrValidationResult {
    private boolean valid;
    private boolean detected;
    private boolean decoded;
    private boolean dataValid;
    private boolean crossFieldCheck;
    private DecodedCodeData ddecodedCodeData;
    private ParsedCodeData parsedCodeData;
    private List<String> issues;

}
