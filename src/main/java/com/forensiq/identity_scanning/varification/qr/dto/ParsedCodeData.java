package com.forensiq.identity_scanning.varification.qr.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParsedCodeData {
    private Map<String,String> fields;
    private boolean parsed;


}
