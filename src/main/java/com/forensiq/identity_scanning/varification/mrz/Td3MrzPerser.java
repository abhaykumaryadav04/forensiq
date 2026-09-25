package com.forensiq.identity_scanning.varification.mrz;

import org.springframework.stereotype.Component;

import com.forensiq.identity_scanning.varification.mrz.dto.Td3MrzData;

@Component
public class Td3MrzPerser {

    private static final int LINE_LENGTH = 44;

    public Td3MrzData perse(String mrzText) {
        if (mrzText == null || mrzText.isBlank()) {
            throw new IllegalArgumentException("MRZ text cannot be null or empty");
        }

        String[] lines = mrzText
                .trim()
                .split("\\r?\\n");

        if (lines.length != 2) {
            throw new IllegalArgumentException("MRZ text must contain exactly two lines");
        }

        String line1 = normalizeMrzLine(lines[0]);
        String line2 = normalizeMrzLine(lines[1]);

        if (line1.length() != LINE_LENGTH || line2.length() != LINE_LENGTH) {
            throw new IllegalArgumentException("Each MRZ line must be exactly 44 characters");
        }

        if (!line1.matches("[A-Z0-9<]{44}") || !line2.matches("[A-Z0-9<]{44}")) {
            throw new IllegalArgumentException("MRZ contains invalid characters");
        }

        return Td3MrzData.builder()
                .documentCode(cleanField(line1.substring(0, 2)))
                .issueCountry(cleanField(line1.substring(2, 5)))
                .surname(extractSurname(line1.substring(5, 44)))
                .givenName(extractGivenName(line1.substring(5, 44)))
                .passportNumber(cleanField(line2.substring(0, 9)))
                .passportNumberCheckDigit(line2.charAt(9))
                .nationality(cleanField(line2.substring(10, 13)))
                .dateOfBirth(cleanField(line2.substring(13, 19)))
                .dateOfBirthCheckDigit(line2.charAt(19))
                .sex(String.valueOf(line2.charAt(20)))
                .expiryDate(cleanField(line2.substring(21, 27)))
                .expiryDateCheckDigit(line2.charAt(27))
                .optionalData(line2.substring(28, 42))
                .optionalDataCheckDigit(line2.charAt(42))
                .compositeCheckDigit(line2.charAt(43))
                .build();
    }

    private String extractSurname(String nameSection) {
        String[] parts = nameSection.split("<<", 2);
        return cleanName(parts[0]);
    }

    private String extractGivenName(String nameSection) {
        String[] parts = nameSection.split("<<", 2);

        if (parts.length < 2) {
            return "";
        }

        return cleanName(parts[1]);
    }

    private String cleanName(String value) {
        return value
                .replace('<', ' ')
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String cleanField(String value) {
        return value
                .replace("<", "")
                .trim();
    }

    private String normalizeMrzLine(String line) {
        return line
                .trim()
                .replaceAll("\\s+", "")
                .toUpperCase();
    }
}