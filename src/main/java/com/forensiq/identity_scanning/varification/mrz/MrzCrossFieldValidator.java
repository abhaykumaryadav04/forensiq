package com.forensiq.identity_scanning.varification.mrz;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.forensiq.identity_scanning.varification.mrz.dto.MrzCrossFieldValidationResult;
import com.forensiq.identity_scanning.varification.mrz.dto.Td3MrzData;

@Component
public class MrzCrossFieldValidator {

    public MrzCrossFieldValidationResult mrzCrossValid(
            Td3MrzData data,
            String extractedName,
            String extractedPassportNumber,
            String extractedDateOfBirth,
            String extractedExpiryDate) {

        if (data == null) {
            throw new IllegalArgumentException("MRZ data cannot be null");
        }

        int totalFieldsChecked = 0;
        int matchedFields = 0;
        List<String> mismatches = new ArrayList<>();

        if (hasValue(extractedName)) {
            totalFieldsChecked++;

            if (namesMatch(
                    data.getSurname(),
                    data.getGivenName(),
                    extractedName)) {

                matchedFields++;

            } else {
                mismatches.add(
                        "Name mismatch: MRZ="
                                + safeValue(data.getSurname())
                                + " "
                                + safeValue(data.getGivenName())
                                + ", OCR="
                                + safeValue(extractedName)
                );
            }
        }

        if (hasValue(extractedPassportNumber)) {
            totalFieldsChecked++;

            String mrzPassportNumber =
                    normalizeField(data.getPassportNumber());

            String extractedPassport =
                    normalizeField(extractedPassportNumber);

            if (mrzPassportNumber.equals(extractedPassport)) {
                matchedFields++;
            } else {
                mismatches.add(
                        "Passport number mismatch: MRZ="
                                + mrzPassportNumber
                                + ", OCR="
                                + extractedPassport
                );
            }
        }

        if (hasValue(extractedDateOfBirth)) {
            totalFieldsChecked++;

            String mrzDateOfBirth =
                    normalizeDate(data.getDateOfBirth());

            String extractedDate =
                    normalizeDate(extractedDateOfBirth);

            if (mrzDateOfBirth.equals(extractedDate)) {
                matchedFields++;
            } else {
                mismatches.add(
                        "Date of birth mismatch: MRZ="
                                + mrzDateOfBirth
                                + ", OCR="
                                + extractedDate
                );
            }
        }

        if (hasValue(extractedExpiryDate)) {
            totalFieldsChecked++;

            String mrzExpiryDate =
                    normalizeDate(data.getExpiryDate());

            String extractedExpiry =
                    normalizeDate(extractedExpiryDate);

            if (mrzExpiryDate.equals(extractedExpiry)) {
                matchedFields++;
            } else {
                mismatches.add(
                        "Expiry date mismatch: MRZ="
                                + mrzExpiryDate
                                + ", OCR="
                                + extractedExpiry
                );
            }
        }

        boolean valid =
                totalFieldsChecked > 0
                        && matchedFields == totalFieldsChecked;

        return MrzCrossFieldValidationResult.builder()
                .matchedFields(matchedFields)
                .mismatchedFields(mismatches.size())
                .mismatches(mismatches)
                .totalFieldsChecked(totalFieldsChecked)
                .valid(valid)
                .build();
    }

    private boolean namesMatch(
            String surname,
            String givenNames,
            String extractedName) {

        String normalizedSurname =
                normalizeName(surname);

        String normalizedGivenNames =
                normalizeName(givenNames);

        String normalizedExtractedName =
                normalizeName(extractedName);

        if (normalizedExtractedName.isBlank()) {
            return false;
        }

        boolean surnameMatches =
                normalizedSurname.isBlank()
                        || normalizedExtractedName.contains(normalizedSurname);

        boolean givenNameMatches =
                normalizedGivenNames.isBlank()
                        || normalizedExtractedName.contains(normalizedGivenNames);

        return surnameMatches && givenNameMatches;
    }

    private String normalizeField(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toUpperCase()
                .replaceAll("\\s+", "")
                .trim();
    }

    private String normalizeDate(String date) {
        if (date == null || date.isBlank()) {
            return "";
        }

        String digits =
                date.replaceAll("[^0-9]", "");

        if (digits.length() == 6) {
            return digits;
        }

        if (digits.length() == 8) {
            if (digits.startsWith("19") || digits.startsWith("20")) {
                return digits.substring(2);
            }

            return digits.substring(6, 8)
                    + digits.substring(2, 4)
                    + digits.substring(0, 2);
        }

        return digits;
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toUpperCase()
                .replaceAll("[^A-Z]", "");
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}