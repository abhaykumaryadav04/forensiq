package com.forensiq.identity_scanning.varification.qr;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.forensiq.identity_scanning.varification.qr.dto.ParsedCodeData;

@Component
public class QrDataParser {

    public ParsedCodeData parse(String rawData) {

        if (rawData == null || rawData.isBlank()) {
            return ParsedCodeData.builder()
                    .fields(Map.of())
                    .parsed(false)
                    .build();
        }

        Map<String, String> fields = new HashMap<>();

        try {
            String[] entries = rawData.split("[;\\n\\r]+");

            for (String entry : entries) {
                entry = entry.trim();

                if (entry.isBlank()) {
                    continue;
                }

                String[] keyValue = entry.split("=", 2);

                if (keyValue.length != 2) {
                    continue;
                }

                String key = keyValue[0]
                        .trim()
                        .toUpperCase();

                String value = keyValue[1]
                        .trim();

                if (!key.isBlank() && !value.isBlank()) {
                    fields.put(key, value);
                }
            }

            return ParsedCodeData.builder()
                    .fields(fields)
                    .parsed(!fields.isEmpty())
                    .build();

        } catch (Exception e) {
            return ParsedCodeData.builder()
                    .fields(fields)
                    .parsed(false)
                    .build();
        }
    }
}