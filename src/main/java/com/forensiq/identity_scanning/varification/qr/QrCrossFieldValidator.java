package com.forensiq.identity_scanning.varification.qr;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.forensiq.identity_scanning.varification.qr.dto.QrCrossFieldValidationResult;
@Component
public class QrCrossFieldValidator {
    public QrCrossFieldValidationResult validate(Map<String,String> qrFields,Map<String,String> ocrFields){
        if(qrFields==null||qrFields.isEmpty()){
            throw new IllegalArgumentException("QR fields cannot be null or empty");
        }
        if(ocrFields==null||ocrFields.isEmpty()){
            throw new IllegalArgumentException("OCR fields cannot be null or empty");
        }
        int totalFieldCheck=0;
        int matchedFields=0;
        List<String> mismatched=new ArrayList<>();
        for(Map.Entry<String,String> qrEntry:qrFields.entrySet()){
            String fieldName=normalizeKey(qrEntry.getKey());
            String qrValue=qrEntry.getValue();
            String ocrValue=findMatchingOcrField(fieldName,ocrFields);
            if(ocrValue==null||ocrValue.isBlank()){
                continue;
            }
            totalFieldCheck++;
            if(fieldsMatch(fieldName,qrValue,ocrValue)){
                matchedFields++;
            }else{
                mismatched.add(fieldName+" mismatch: QR="+safeValue(qrValue)+", OCR="+safeValue(ocrValue));
            }
        }
        return QrCrossFieldValidationResult.builder()
                .matchedFields(matchedFields)
                .mismatchedFields(mismatched.size())
                .mismatchs(mismatched)
                .totalfieldcheckFields(totalFieldCheck)
                .valid(!mismatched.isEmpty()?false:totalFieldCheck>0)
                .build();
    }
    private boolean fieldsMatch(String fieldName,String qrValue,String ocrValue){
        if(fieldName.contains("DOB")||fieldName.contains("DATEOFBIRTH")){
            return normalizeDate(qrValue).equals(normalizeDate(ocrValue));
        }
        return normalizeText(qrValue).equals(normalizeText(ocrValue));
    }
    private String normalizeText(String value){
        return value==null?"":value.toUpperCase().replaceAll("[^A-Z0-9]","");
    }
    private String normalizeDate(String date){
        if(date==null||date.isBlank()){
            return "";
        }
        String digits=date.replaceAll("[^0-9]","");
        if(digits.length()==8){
            if(digits.startsWith("19")||digits.startsWith("20")){
                return digits;
            }
            return digits.substring(4,8)+digits.substring(2,4)+digits.substring(0,2);
        }
        return digits;
    }

    private String normalizeKey(String value){
        return value==null?"":value.toUpperCase().replaceAll("[^A-Z]","");
    }
    
    private String findMatchingOcrField(String fieldName,Map<String,String> ocrFields){
        for(Map.Entry<String,String> entry:ocrFields.entrySet()){
            String normalizedOcrKey=normalizeKey(entry.getKey());
            if(normalizedOcrKey.equals(fieldName)){
                return entry.getValue();
            }
        }
        return null;
    }
    private String safeValue(String value){
        return value==null?"":value;
    }
}