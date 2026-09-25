package com.forensiq.identity_scanning.varification.mrz.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MrzCrossFieldValidationResult {
  private boolean valid;

    private int totalFieldsChecked;

    private int matchedFields;

    private int mismatchedFields;

    private List<String> mismatches;
}
