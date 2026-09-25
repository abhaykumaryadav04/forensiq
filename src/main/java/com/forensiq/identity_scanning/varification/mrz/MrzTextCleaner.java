package com.forensiq.identity_scanning.varification.mrz;

import org.springframework.stereotype.Component;

@Component
public class MrzTextCleaner {

    public String clean(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("MRZ text cannot be null or empty");
        }

        String cleaned = rawText
                .toUpperCase()
                .replace("\r\n", "\n")
                .replace("\r", "\n");

        String[] lines = cleaned.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            String cleanedLine = line
                    .replaceAll("\\s+", "")
                    .replaceAll("[^A-Z0-9<]", "");

            if (!cleanedLine.isBlank()) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(cleanedLine);
            }
        }

        return result.toString();
    }
}