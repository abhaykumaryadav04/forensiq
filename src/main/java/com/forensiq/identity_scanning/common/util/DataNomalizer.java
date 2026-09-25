package com.forensiq.identity_scanning.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public final class DataNomalizer {
       private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(

            DateTimeFormatter.ofPattern("dd/MM/yyyy",Locale.ENGLISH),

            DateTimeFormatter.ofPattern("dd-MM-yyyy",Locale.ENGLISH),

            DateTimeFormatter.ofPattern("dd.MM.yyyy",Locale.ENGLISH),

            DateTimeFormatter.ofPattern("yyyy-MM-dd",Locale.ENGLISH),

            DateTimeFormatter.ofPattern("d MMM yyyy",Locale.ENGLISH),

            DateTimeFormatter.ofPattern("dd MMM yyyy",Locale.ENGLISH)

    );
    public static String normalize(String dateText) {

        if (dateText == null || dateText.isBlank()) {
            return null;
        }
        String cleanedDate =dateText.trim().replaceAll("\\s+", " ");
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                 LocalDate date = LocalDate.parse(
                                cleanedDate,
                                formatter );
                return date.toString();
            } catch (DateTimeParseException exception) {

                // Try next format

            }
        }
        return null;
    }
}
