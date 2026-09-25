package com.forensiq.identity_scanning.information.uttil;

import java.util.List;

import com.forensiq.identity_scanning.information.dto.ExtractedField;

public final class OverallConfidenceCalculator {
  private OverallConfidenceCalculator() {
    }

    public static double calculate(List<ExtractedField> fields) {
        if (fields == null || fields.isEmpty()) {
            return 0.0;
        }
        return fields.stream()
                .map(ExtractedField::getConfidencce)
                .filter(confidence -> confidence != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}
