package com.forensiq.identity_scanning.information.uttil;

public final class FieldConfidenceCanculator {

  private   FieldConfidenceCanculator() {

    }

    public static double calculate( boolean labelFound,boolean patternValid,boolean valueValid) {
        double confidence = 0.0;

        if (labelFound) {
            confidence += 0.30;
        }

        if (patternValid) {
            confidence += 0.40;
        }

        if (valueValid) {
            confidence += 0.30;
        }

        return Math.min(confidence, 1.0);
    }
}
