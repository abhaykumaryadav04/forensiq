package com.forensiq.identity_scanning.varification.mrz.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class Td3MrzData {
    private String documentCode;
    private String issueCountry;
    private String surname;
    private String givenName;
    private String passportNumber;
    private String nationality;
    private String dateOfBirth;
    private String sex;
    private String expiryDate;
    private String optionalData;
    private char passportNumberCheckDigit;
    private char dateOfBirthCheckDigit;
    private char expiryDateCheckDigit;
    private char optionalDataCheckDigit;
    private char compositeCheckDigit;

}
