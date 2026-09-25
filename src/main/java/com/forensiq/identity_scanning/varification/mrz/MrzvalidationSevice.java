package com.forensiq.identity_scanning.varification.mrz;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.forensiq.identity_scanning.varification.mrz.dto.MrzCrossFieldValidationResult;
import com.forensiq.identity_scanning.varification.mrz.dto.MrzValidationResult;
import com.forensiq.identity_scanning.varification.mrz.dto.Td3MrzData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MrzvalidationSevice {

    private final MrzCheckDigitValidation mrzCheckDigitValidation;
    private final MrzTextCleaner mrzTextCleaner;
    private final Td3MrzFormatValidator td3MrzFormatValidator;
    private final MrzCrossFieldValidator mrzCrossFieldValidator;
    private final Td3MrzPerser td3MrzPerser;

    public MrzValidationResult validate(
            String rawMrzText,
            String extractedName,
            String extractedPassportNumber,
            String extractedDateOfBirth,
            String extractedExpiryDate) {

        List<String> issues = new ArrayList<>();

        if (rawMrzText == null || rawMrzText.isBlank()) {
            issues.add("MRZ text is empty");
            return buildFailureResult(
                    false,
                    false,
                    false,
                    null,
                    issues
            );
        }

        String cleanMrz;

        try {
            cleanMrz = mrzTextCleaner.clean(rawMrzText);
        } catch (Exception e) {
            issues.add("Unable to clean MRZ text");
            return buildFailureResult(
                    false,
                    false,
                    false,
                    null,
                    issues
            );
        }

        boolean formatValid;

        try {
            formatValid = td3MrzFormatValidator.isValid(cleanMrz);
        } catch (Exception e) {
            formatValid = false;
        }

        if (!formatValid) {
            issues.add("Invalid TD3 MRZ format");

            return buildFailureResult(
                    false,
                    false,
                    false,
                    null,
                    issues
            );
        }

        Td3MrzData td3MrzData;

        try {
            td3MrzData = td3MrzPerser.perse(cleanMrz);
        } catch (Exception e) {
            issues.add("Unable to parse TD3 MRZ");

            return buildFailureResult(
                    true,
                    false,
                    false,
                    null,
                    issues
            );
        }

        boolean checkDigitsValid;

        try {
            checkDigitsValid = validateCheckDigits(td3MrzData);
        } catch (Exception e) {
            checkDigitsValid = false;
        }

        if (!checkDigitsValid) {
            issues.add("MRZ check digit validation failed");
        }

        MrzCrossFieldValidationResult crossFieldResult;

        try {
            crossFieldResult = mrzCrossFieldValidator.mrzCrossValid(
                    td3MrzData,
                    extractedName,
                    extractedPassportNumber,
                    extractedDateOfBirth,
                    extractedExpiryDate
            );
        } catch (Exception e) {
            issues.add("MRZ cross-field validation failed");

            return MrzValidationResult.builder()
                    .valid(false)
                    .formatValid(true)
                    .checkDigitsValid(checkDigitsValid)
                    .crossFieldValid(false)
                    .mrzData(td3MrzData)
                    .issues(issues)
                    .build();
        }

        boolean crossFieldValid = crossFieldResult.isValid();

        if (!crossFieldValid) {
            issues.addAll(crossFieldResult.getMismatches());
        }

        boolean valid =
                formatValid
                        && checkDigitsValid
                        && crossFieldValid;

        return MrzValidationResult.builder()
                .valid(valid)
                .formatValid(formatValid)
                .checkDigitsValid(checkDigitsValid)
                .crossFieldValid(crossFieldValid)
                .mrzData(td3MrzData)
                .issues(issues)
                .build();
    }

    private boolean validateCheckDigits(Td3MrzData data) {

        if (data == null) {
            return false;
        }

        boolean passportNumberValid =
                mrzCheckDigitValidation.isValid(
                        data.getPassportNumber(),
                        data.getPassportNumberCheckDigit()
                );

        boolean dateOfBirthValid =
                mrzCheckDigitValidation.isValid(
                        data.getDateOfBirth(),
                        data.getDateOfBirthCheckDigit()
                );

        boolean expiryDateValid =
                mrzCheckDigitValidation.isValid(
                        data.getExpiryDate(),
                        data.getExpiryDateCheckDigit()
                );

        return passportNumberValid
                && dateOfBirthValid
                && expiryDateValid;
    }

    private MrzValidationResult buildFailureResult(
            boolean formatValid,
            boolean checkDigitsValid,
            boolean crossFieldValid,
            Td3MrzData mrzData,
            List<String> issues) {

        return MrzValidationResult.builder()
                .valid(false)
                .formatValid(formatValid)
                .checkDigitsValid(checkDigitsValid)
                .crossFieldValid(crossFieldValid)
                .mrzData(mrzData)
                .issues(issues)
                .build();
    }
}