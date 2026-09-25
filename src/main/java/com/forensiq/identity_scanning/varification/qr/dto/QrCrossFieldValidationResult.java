package com.forensiq.identity_scanning.varification.qr.dto;



import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QrCrossFieldValidationResult {
    private boolean valid;
    private int totalfieldcheckFields;
    private int matchedFields;
    private int mismatchedFields;
    private List<String> mismatchs;


}
