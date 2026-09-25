package com.forensiq.identity_scanning.varification.mrz;

import org.springframework.stereotype.Component;

@Component
public class MrzCheckDigitValidation {

    private static final int[] WEIGHTS = {7, 3, 1};

    public int calculateCheckDigit(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("MRZ value cannot be null or empty");
        }

        int sum = 0;
        char[] characters = value.toUpperCase().toCharArray();

        for (int i = 0; i < characters.length; i++) {
            int characterValue = getCharacterValue(characters[i]);
            int weight = WEIGHTS[i % 3];
            sum += characterValue * weight;
        }

        return sum % 10;
    }

    public boolean isValid(String value, char expectedCheckValue) {
        if (value == null || value.isBlank()) {
            return false;
        }

        if (!Character.isDigit(expectedCheckValue)) {
            return false;
        }

        int calculatedValue = calculateCheckDigit(value);
        int expectedValue = Character.digit(expectedCheckValue, 10);

        return calculatedValue == expectedValue;
    }

    private int getCharacterValue(char character) {
        if (character >= '0' && character <= '9') {
            return character - '0';
        }

        if (character >= 'A' && character <= 'Z') {
            return character - 'A' + 10;
        }

        if (character == '<') {
            return 0;
        }

        throw new IllegalArgumentException("Invalid MRZ character: " + character);
    }
}