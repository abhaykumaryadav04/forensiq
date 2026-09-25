package com.forensiq.identity_scanning.varification.mrz.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MrzValidationResult {

 private boolean valid;

    private boolean formatValid;

    private boolean checkDigitsValid;

    private boolean crossFieldValid;

    private Td3MrzData mrzData;

    private List<String> issues;
}
