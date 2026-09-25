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
public class IdenityCardInformationExtractor implements InformationExtractor{
    @Override
    public DocumentType getSupportedDocumentType(){
        return DocumentType.ID_CARD;
    }
    @Override
     public ExtractionResponse extract(String ocrText,List<OcrWord> ocrWords) {
      
        long startTime = System.currentTimeMillis();

        List<ExtractedField> fields = new ArrayList<>();

        extractAadhaarNumber(ocrText, fields);
        extractName(ocrText, fields);
        extractDateOfBirth(ocrText, fields);
        extractGender(ocrText, fields);

        long endTime = System.currentTimeMillis();

        double confidence =
                OverallConfidenceCalculator.calculate(fields);

        return ExtractionResponse.builder()
                .documentType(getSupportedDocumentType())
                .extractedVersion("aadhaar-v1")
                .fields(fields)
                .confodence(confidence)
                .processTimeMs(endTime - startTime)
                .build();
    }

    private void extractAadhaarNumber( String ocrText,List<ExtractedField> fields) {

        Pattern pattern = Pattern.compile(
                "\\b(\\d{4}\\s?\\d{4}\\s?\\d{4})\\b"
        );

        Matcher matcher = pattern.matcher(ocrText);

        if (matcher.find()) {

            String aadhaarNumber =
                    matcher.group(1)
                            .replaceAll("\\s", "");

            boolean patternValid =
                    aadhaarNumber.matches("\\d{12}");

            boolean valueValid =
                    aadhaarNumber.length() == 12;

            addField(
                    fields,
                    ExtractedFieldType.AADHAAR_NUMBER,
                    aadhaarNumber,
                    false,
                    patternValid,
                    valueValid
            );
        }
    }

    private void extractName(
            String ocrText,
            List<ExtractedField> fields
    ) {

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

    private void extractDateOfBirth(
            String ocrText,
            List<ExtractedField> fields
    ) {

        Pattern pattern = Pattern.compile(
                "(?i)(date\\s*of\\s*birth|dob|year\\s*of\\s*birth)"
                        + "\\s*[:\\-]?\\s*"
                        + "(\\d{1,2}[/.\\-]\\d{1,2}[/.\\-]\\d{4}|\\d{4})"
        );

        Matcher matcher = pattern.matcher(ocrText);

        if (matcher.find()) {

            String value = matcher.group(2);

            if (value.matches("\\d{4}")) {

                addField(
                        fields,
                        ExtractedFieldType.DATE_OF_BIRTH,
                        value,
                        true,
                        true,
                        true
                );

            } else {

                String normalizedDate =
                        DataNomalizer.normalize(value);

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
    }

    private void extractGender(
            String ocrText,
            List<ExtractedField> fields
    ) {

        Pattern pattern = Pattern.compile(
                "(?i)(male|female|gender)\\s*[:\\-]?\\s*(male|female|m|f)"
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

        double confidence =
                FieldConfidenceCanculator.calculate(
                        labelFound,
                        patternValid,
                        valueValid
                );

        fields.add(
                ExtractedField.builder()
                        .extractedFieldType(fieldType)
                        .value(value.trim())
                        .confidencce(confidence)
                        .source(ExtractedSource.OCR)
                        .build()
        );
    }
}
