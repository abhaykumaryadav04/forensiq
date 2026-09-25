package com.forensiq.identity_scanning.information.dto;

import java.util.List;

import com.forensiq.identity_scanning.document.entity.DocumentType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExtractionResponse {
    private List<ExtractedField> fields;
    private DocumentType documentType;
    private Double confodence;
    private long processTimeMs;
    private String extractedVersion;

}
