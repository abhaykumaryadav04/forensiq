package com.forensiq.identity_scanning.information.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.forensiq.identity_scanning.common.util.DataNomalizer;
import com.forensiq.identity_scanning.document.entity.DocumentType;
import com.forensiq.identity_scanning.information.dto.ExtractedField;
import com.forensiq.identity_scanning.information.dto.ExtractionResponse;
import com.forensiq.identity_scanning.information.model.ExtractedFieldType;
import com.forensiq.identity_scanning.information.model.ExtractedSource;
import com.forensiq.identity_scanning.information.uttil.FieldConfidenceCanculator;
import com.forensiq.identity_scanning.information.uttil.OverallConfidenceCalculator;
import com.forensiq.identity_scanning.ocr.dto.OcrWord;

@Component
public class DrivingLicenseInformationExtractor
        implements InformationExtractor {

    @Override
    public DocumentType getSupportedDocumentType() {
        return DocumentType.DRIVING_LICENSE;
    }

    @Override
    public ExtractionResponse extract(String ocrText,List<OcrWord> ocrWords) {

        long startTime = System.currentTimeMillis();

        List<ExtractedField> fields = new ArrayList<>();

        extractLicenseNumber(ocrText, fields);
        extractName(ocrText, fields);
        extractDateOfBirth(ocrText, fields);
        extractIssueDate(ocrText, fields);
        extractExpiryDate(ocrText, fields);
        extractGender(ocrText, fields);

        long endTime = System.currentTimeMillis();

        double confidence =
                OverallConfidenceCalculator.calculate(fields);

        return ExtractionResponse.builder()
                .documentType(getSupportedDocumentType())
                .extractedVersion("driving-license-v1")
                .fields(fields)
                .confodence(confidence)
                .processTimeMs(endTime - startTime)
                .build();
    }

    private void extractLicenseNumber(String ocrText, List<ExtractedField> fields) {

        Pattern pattern = Pattern.compile(
                "(?i)(driving\\s*licence\\s*(no|number)?|dl\\s*(no|number)?)"
                        + "\\s*[:\\-]?\\s*"
                        + "([A-Z]{2}[0-9]{2}[0-9]{4,13})"
        );

        Matcher matcher = pattern.matcher(ocrText);

        if (matcher.find()) {

            String licenseNumber =
                    matcher.group(4).toUpperCase();

            boolean patternValid =
                    licenseNumber.matches(
                            "[A-Z]{2}[0-9]{2}[0-9]{4,13}"
                    );

            boolean valueValid =
                    licenseNumber.length() >= 8;

            addField(
                    fields,
                    ExtractedFieldType.LICENSE_NUMBER,
                    licenseNumber,
                    true,
                    patternValid,
                    valueValid
            );
        }
    }

    private void extractName(String ocrText,List<ExtractedField> fields) {

        Pattern pattern = Pattern.compile(
                "(?i)(name)\\s*[:\\-]?\\s*([A-Z ]{3,})"
        );

        Matcher matcher = pattern.matcher(ocrText);

        if (matcher.find()) {

            String name = matcher.group(2).trim();

            addField(
                    fields,
                    ExtractedFieldType.FULL_NAME,
                    name,
                    true,
                    name.matches("[A-Z ]{3,}"),
                    !name.isBlank()
            );
        }
    }

    private void extractDateOfBirth( String ocrText,  List<ExtractedField> fields ) {

        Pattern pattern = Pattern.compile(
                "(?i)(date\\s*of\\s*birth|dob)"
                        + "\\s*[:\\-]?\\s*"
                        + "(\\d{1,2}[/.\\-]\\d{1,2}[/.\\-]\\d{4})"
        );

        Matcher matcher = pattern.matcher(ocrText);

        if (matcher.find()) {

            String normalizedDate =
                    DataNomalizer.normalize(matcher.group(2));

            if (normalizedDate != null) {

                addField(
                        fields,
                        ExtractedFieldType.DATE_OF_BIRTH,
                        normalizedDate,
                        true,
                        true,
                        true
                );
            }
        }
    }

    private void extractIssueDate( String ocrText, List<ExtractedField> fields ) {

        Pattern pattern = Pattern.compile(
                "(?i)(date\\s*of\\s*issue|issue\\s*date|issued)"
                        + "\\s*[:\\-]?\\s*"
                        + "(\\d{1,2}[/.\\-]\\d{1,2}[/.\\-]\\d{4})"
        );

        Matcher matcher = pattern.matcher(ocrText);

        if (matcher.find()) {

            String normalizedDate =
                    DataNomalizer.normalize(matcher.group(2));

            if (normalizedDate != null) {

                addField(
                        fields,
                        ExtractedFieldType.ISSUE_DATE,
                        normalizedDate,
                        true,
                        true,
                        true
                );
            }
        }
    }

    private void extractExpiryDate(String ocrText, List<ExtractedField> fields ) {

        Pattern pattern = Pattern.compile(
                "(?i)(valid\\s*upto|expiry\\s*date|date\\s*of\\s*expiry)"
                        + "\\s*[:\\-]?\\s*"
                        + "(\\d{1,2}[/.\\-]\\d{1,2}[/.\\-]\\d{4})"
        );

        Matcher matcher = pattern.matcher(ocrText);

        if (matcher.find()) {

            String normalizedDate =
                    DataNomalizer.normalize(matcher.group(2));

            if (normalizedDate != null) {

                addField(
                        fields,
                        ExtractedFieldType.EXPIRY_DATE,
                        normalizedDate,
                        true,
                        true,
                        true
                );
            }
        }
    }

    private void extractGender( String ocrText,List<ExtractedField> fields) {

        Pattern pattern = Pattern.compile(
                "(?i)(gender|sex)\\s*[:\\-]?\\s*([MF])"
        );
        Matcher matcher = pattern.matcher(ocrText);
        if (matcher.find()) {
            String gender = matcher.group(2).toUpperCase();

            addField(
                    fields,
                    ExtractedFieldType.GENDER,
                    gender,
                    true,
                    true,
                    true
            );
        }
    }

    private void addField(
            List<ExtractedField> fields,
            ExtractedFieldType fieldType,
            String value,
            boolean labelFound,
            boolean patternValid,
            boolean valueValid
    ) {

        if (value == null || value.isBlank()) {
            return;
        }

        double confidence = FieldConfidenceCanculator.calculate(
                        labelFound,
                        patternValid,
                        valueValid
                );

        fields.add( ExtractedField.builder()
                        .extractedFieldType(fieldType)
                        .value(value.trim())
                        .confidencce(confidence)
                        .source(ExtractedSource.OCR)
                        .build()
        );
    }
}