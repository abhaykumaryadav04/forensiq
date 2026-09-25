package com.forensiq.identity_scanning.varification.qr;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.forensiq.identity_scanning.varification.qr.dto.ParsedCodeData;


@Component
public class QrDataValidator {

    public boolean isValid(ParsedCodeData parsedCodeData) {
        if (parsedCodeData == null) {
            return false;
        }
        if (!parsedCodeData.isParsed()) {
            return false;
        }
        Map<String, String> fields = parsedCodeData.getFields();
        if (fields == null || fields.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || key.isBlank()) {
                return false;
            }
            if (value == null || value.isBlank()) {
                return false;
            }
        }
        return true;
    }
}