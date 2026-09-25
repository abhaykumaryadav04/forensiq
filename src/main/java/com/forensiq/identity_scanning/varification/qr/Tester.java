package com.forensiq.identity_scanning.varification.qr;

import java.util.Map;


import com.forensiq.identity_scanning.varification.qr.dto.QrCrossFieldValidationResult;

public class Tester {
public static void main(String[]args){
  QrCrossFieldValidator validator =
                new QrCrossFieldValidator();

        Map<String, String> qrFields =
                Map.of(
                        "NAME", "ABHAY KUMAR",
                        "DOB", "15/07/2001",
                        "ID", "123456"
                );

        Map<String, String> ocrFields =
                Map.of(
                        "NAME", "Abhay Kumar",
                        "DOB", "2001-07-15",
                        "ID", "123456"
                );

        QrCrossFieldValidationResult result =
                validator.validate(
                        qrFields,
                        ocrFields
                );

        System.out.println(
                "Valid: " + result.isValid()
        );

        System.out.println(
                "Total Fields Checked: "
                        + result.getTotalfieldcheckFields()
        );

        System.out.println(
                "Matched Fields: "
                        + result.getMatchedFields()
        );

        System.out.println(
                "Mismatches: "
                        + result.getMismatchs()
        );
    }
}

