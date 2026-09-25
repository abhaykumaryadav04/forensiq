package com.forensiq.identity_scanning.varification.mrz;

import org.springframework.stereotype.Component;

@Component
public class Td3MrzFormatValidator {

    private static final int LINE_LENGTH = 44;
    private static final int NUMBER_OF_LINES = 2;

    public boolean isValid(String mrzText) {
        if (mrzText == null || mrzText.isBlank()) {
            throw new IllegalArgumentException("MRZ text cannot be null or empty");
        }

        String[] lines = mrzText.trim().split("\\r?\\n");

        if (lines.length != NUMBER_OF_LINES) {
            return false;
        }

        for (String line : lines) {
            if (line.length() != LINE_LENGTH) {
                return false;
            }

            if (!line.matches("[A-Z0-9<]+")) {
                return false;
            }
        }

        return true;
    }
}