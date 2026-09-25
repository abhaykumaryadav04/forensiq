package com.forensiq.identity_scanning.information.dto;


import com.forensiq.identity_scanning.information.model.ExtractedFieldType;
import com.forensiq.identity_scanning.information.model.ExtractedSource;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ExtractedField {

private ExtractedFieldType extractedFieldType;
private double confidencce;
private String value;
private ExtractedSource  source;
}
