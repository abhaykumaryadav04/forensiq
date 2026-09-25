package com.forensiq.identity_scanning.varification.verifirer;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.information.model.ExtractedDocumentField;
import com.forensiq.identity_scanning.information.model.ExtractedFieldType;
import com.forensiq.identity_scanning.information.repo.ExtractedDocumentFieldRepo;
import com.forensiq.identity_scanning.varification.dto.VarificationResult;
import com.forensiq.identity_scanning.varification.enumeration.VarificationStatus;
import com.forensiq.identity_scanning.varification.enumeration.VarificationType;

@Component
public class CrossFieldConsistencyVarifier implements AuthenticityVarifier{
    @Autowired
    private  ExtractedDocumentFieldRepo repository;

    @Override
    public String getVarifierName() {
        return "Cross Field Consistency Verifier";
    }

    @Override
    public VarificationResult varify(Document document) {
     List<ExtractedDocumentField> fields =repository.findByDocumentId(document.getId());
      if (fields == null || fields.isEmpty()) {

            return VarificationResult.builder()
                    .varifierName(getVarifierName())
                    .varificationType(getVerificationType())
                    .varificationStatus(VarificationStatus.NOT_AVAILABLE)
                    .confidence(0.0)
                    .riskScore(0.0)
                    .message(
                            "No extracted fields available for verification"
                    )
                    .build();
                }
           Map<ExtractedFieldType, String> fieldMap =
                convertToMap(fields);

        return performConsistencyChecks(fieldMap);
    }

    private VarificationResult performConsistencyChecks(Map<ExtractedFieldType,String> fields) {
  int totalChecks = 0;
        int passedChecks = 0;
        StringBuilder issues = new StringBuilder();
       
        if (fields.containsKey(ExtractedFieldType.ISSUE_DATE)&& fields.containsKey(ExtractedFieldType.EXPIRY_DATE)) {
            totalChecks++;
            boolean valid = validateIssueAndExpiryDate(fields.get( ExtractedFieldType.ISSUE_DATE),
                            fields.get( ExtractedFieldType.EXPIRY_DATE));
            if (valid) {
                passedChecks++;
            } else {
                issues.append(
                        "Issue date is after expiry date. "
                );
            }
        }
        if (fields.containsKey(
                ExtractedFieldType.DATE_OF_BIRTH
        )) {
            totalChecks++;
            boolean valid =validateDateOfBirth(fields.get(ExtractedFieldType.DATE_OF_BIRTH )) ;
            if (valid) {
                passedChecks++;
            } else {
                issues.append(
                        "Date of birth is invalid or in the future. "
                );
            }
        }
        if (fields.containsKey(ExtractedFieldType.FULL_NAME ) && (fields.containsKey( ExtractedFieldType.FIRST_NAME )|| fields.containsKey( ExtractedFieldType.LAST_NAME))) {
            totalChecks++;
            boolean valid = validateFullName(fields);
            if (valid) {
                passedChecks++;
            } else {
                issues.append(
                        "Full name does not match first and last name. "
                );
            }
        }

        return buildResult(
                totalChecks,
                passedChecks,
                issues.toString()
        );
    }

    private boolean validateIssueAndExpiryDate(String issueDate, String expiryDate) {

        try {
            LocalDate issue = LocalDate.parse(issueDate);
            LocalDate expiry =LocalDate.parse(expiryDate);
            return issue.isBefore(expiry);
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean validateDateOfBirth( String dob) {

        try {

            LocalDate dateOfBirth =
                    LocalDate.parse(dob);

            return !dateOfBirth.isAfter(
                    LocalDate.now()
            );

        } catch (Exception exception) {

            return false;
        }
    }

    private boolean validateFullName(Map<ExtractedFieldType, String> fields) {

        String firstName =
                fields.getOrDefault(
                        ExtractedFieldType.FIRST_NAME,
                        ""
                );

        String lastName =
                fields.getOrDefault(
                        ExtractedFieldType.LAST_NAME,
                        ""
                );

        String fullName =
                fields.getOrDefault(
                        ExtractedFieldType.FULL_NAME,
                        ""
                );

        String expectedName =
                (firstName + " " + lastName)
                        .trim()
                        .replaceAll("\\s+", " ")
                        .toUpperCase();

        String actualName =
                fullName
                        .trim()
                        .replaceAll("\\s+", " ")
                        .toUpperCase();

        return expectedName.equals(actualName);
    }

    private VarificationResult buildResult(
            int totalChecks,
            int passedChecks,
            String issues
    ) {

        if (totalChecks == 0) {

            return VarificationResult.builder()
                    .varifierName(getVarifierName())
                    .varificationType(getVerificationType())
                    .varificationStatus(VarificationStatus.NOT_AVAILABLE)
                    .confidence(0.0)
                    .riskScore(0.0)
                    .message(
                            "Not enough fields available for consistency verification"
                    )
                    .build();
        }

        double successRate =
                (double) passedChecks / totalChecks;

        double confidence = successRate;

        double riskScore =
                (1.0 - successRate) * 100;

        VarificationStatus status;

        if (successRate == 1.0) {

            status = VarificationStatus.PASSED;

        } else if (successRate >= 0.5) {

            status = VarificationStatus.WARNING;

        } else {

            status = VarificationStatus.FAILED;
        }

        String message;

        if (issues.isBlank()) {

            message =
                    "All cross-field consistency checks passed";

        } else {

            message =
                    issues;
        }

        return VarificationResult.builder()
                .varifierName(getVarifierName())
                .varificationType(getVerificationType())
                 .varificationStatus(status)
                .confidence(confidence)
                .riskScore(riskScore)
                .message(message)
                .build();
    }

    private Map<ExtractedFieldType, String> convertToMap(List<ExtractedDocumentField> fields) {
       Map<ExtractedFieldType, String> fieldMap =
                new HashMap<>();

        for (ExtractedDocumentField field : fields) {

            if (field.getExtractedFieldType() != null
                    && field.getValue() != null) {

                fieldMap.put(
                        field.getExtractedFieldType(),
                        field.getValue()
                );
            }
        }

        return fieldMap;
    }

    @Override
    public VarificationType getVerificationType() {
     return VarificationType.CROSS_FIELD;
    }

}
