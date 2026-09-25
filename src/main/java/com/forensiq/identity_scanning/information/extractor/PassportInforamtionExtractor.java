package com.forensiq.identity_scanning.information.extractor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
public class PassportInforamtionExtractor implements InformationExtractor {
    private static final String VERSION="passport-v4";
    private static final String DATE_PATTERN="(\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}|\\d{1,2}\\s+[A-Za-z]{3,12}[,\\s]+\\d{4})";
    @Override
    public DocumentType getSupportedDocumentType(){
        return DocumentType.PASSPORT;
    }
    @Override
    public ExtractionResponse extract(String ocrText,List<OcrWord> ocrWords){
        long startTime=System.currentTimeMillis();
        List<ExtractedField> fields=new ArrayList<>();
        if((ocrText==null||ocrText.isBlank())&&(ocrWords==null||ocrWords.isEmpty())){
            return ExtractionResponse.builder()
                    .documentType(getSupportedDocumentType())
                    .extractedVersion(VERSION)
                    .fields(fields)
                    .confodence(0.0)
                    .processTimeMs(System.currentTimeMillis()-startTime)
                    .build();
        }
        String text=ocrText==null?"":normalizeOcrText(ocrText);
        if(!text.isBlank()){
            extractPassportNumber(text,fields);
            extractSurname(text,fields);
            extractGivenName(text,fields);
            extractNationality(text,fields);
            extractDateOfBirth(text,fields);
            extractGender(text,fields);
            extractIssueDate(text,fields);
            extractExpiryDate(text,fields);
        }
        if(ocrWords!=null&&!ocrWords.isEmpty()){
            extractFromOcrWords(ocrWords,fields);
        }
        if(!hasField(fields,ExtractedFieldType.NATIONALITY)){
            extractNationalityFallback(text,fields);
        }
        if(!hasField(fields,ExtractedFieldType.GENDER)){
            extractGenderFallback(text,fields);
        }
        extractFullName(fields);
        double confidence=OverallConfidenceCalculator.calculate(fields);
        long endTime=System.currentTimeMillis();
        return ExtractionResponse.builder()
                .documentType(getSupportedDocumentType())
                .extractedVersion(VERSION)
                .fields(fields)
                .confodence(confidence)
                .processTimeMs(endTime-startTime)
                .build();
    }
    private String normalizeOcrText(String text){
        return text.replace("\r","\n").replaceAll("[\\t]+"," ").replaceAll(" {2,}"," ").replaceAll("\\n{2,}","\n").trim();
    }
    private void extractPassportNumber(String ocrText,List<ExtractedField> fields){
        Pattern pattern=Pattern.compile("(?i)(passport\\s*(?:no|number|#)?|passport)\\s*[:\\-]?\\s*([A-Z0-9]{6,12})");
        Matcher matcher=pattern.matcher(ocrText);
        if(!matcher.find()){
            return;
        }
        String value=cleanAlphaNumeric(matcher.group(2));
        if(!isValidPassportNumber(value)){
            return;
        }
        addField(fields,ExtractedFieldType.PASSPORT_NUMBER,value,true,true,true);
    }
    private void extractPassportNumberFromWords(List<OcrWord> words,List<ExtractedField> fields){
        if(hasField(fields,ExtractedFieldType.PASSPORT_NUMBER)){
            return;
        }
        for(int i=0;i<words.size();i++){
            OcrWord label=words.get(i);
            String labelText=cleanToken(label.getText());
            if(!looksLikePassportLabel(labelText)){
                continue;
            }
            for(int j=i+1;j<Math.min(i+7,words.size());j++){
                OcrWord candidate=words.get(j);
                String value=cleanAlphaNumeric(candidate.getText());
                if(!isValidPassportNumber(value)){
                    continue;
                }
                if(!isNearby(label,candidate)){
                    continue;
                }
                if(value.matches("\\d+")&&!isVeryClose(label,candidate)){
                    continue;
                }
                addField(fields,ExtractedFieldType.PASSPORT_NUMBER,value,true,true,true);
                return;
            }
        }
    }
    private boolean looksLikePassportLabel(String text){
        if(text==null||text.isBlank()){
            return false;
        }
        String value=text.toUpperCase().replaceAll("[^A-Z]","");
        return value.equals("PASSPORT")||value.equals("PASPO")||value.equals("PASPORT")||value.equals("PASSPO")||value.equals("PASSPRT")||value.equals("PASPOR")||value.equals("PASSP");
    }
    private boolean isValidPassportNumber(String value){
        return value!=null&&value.matches("[A-Z0-9]{6,12}");
    }
    private void extractSurname(String ocrText,List<ExtractedField> fields){
        Pattern pattern=Pattern.compile("(?i)(surname|family\\s*name|last\\s*name)\\s*[:\\-]?\\s*([A-Z][A-Z ]{1,40})");
        Matcher matcher=pattern.matcher(ocrText);
        if(!matcher.find()){
            return;
        }
        String surname=cleanName(matcher.group(2));
        if(!isValidName(surname)){
            return;
        }
        addField(fields,ExtractedFieldType.LAST_NAME,surname,true,true,true);
    }
    private void extractGivenName(String ocrText,List<ExtractedField> fields){
        Pattern pattern=Pattern.compile("(?i)(given\\s*name|first\\s*name|given\\s*names)\\s*[:\\-]?\\s*([A-Z][A-Z ]{1,40})");
        Matcher matcher=pattern.matcher(ocrText);
        if(!matcher.find()){
            return;
        }
        String givenName=cleanName(matcher.group(2));
        if(!isValidName(givenName)){
            return;
        }
        addField(fields,ExtractedFieldType.FIRST_NAME,givenName,true,true,true);
    }
    private void extractNationality(String ocrText,List<ExtractedField> fields){
        Pattern pattern=Pattern.compile("(?i)nationality\\s*[:\\-]?\\s*([A-Z][A-Z ]{2,30})");
        Matcher matcher=pattern.matcher(ocrText);
        if(!matcher.find()){
            return;
        }
        String nationality=cleanName(matcher.group(1));
        if(!isValidName(nationality)){
            return;
        }
        addField(fields,ExtractedFieldType.NATIONALITY,nationality,true,true,true);
    }
    private void extractNationalityFallback(String ocrText,List<ExtractedField> fields){
        if(ocrText==null||ocrText.isBlank()){
            return;
        }
        Pattern pattern=Pattern.compile("\\b(INDIAN|HAITIENNE|HAITIAN|FRENCH|GERMAN|BRITISH|CANADIAN|AMERICAN|AUSTRALIAN|JAPANESE|CHINESE)\\b",Pattern.CASE_INSENSITIVE);
        Matcher matcher=pattern.matcher(ocrText);
        if(matcher.find()){
            addField(fields,ExtractedFieldType.NATIONALITY,matcher.group(1).toUpperCase(),false,true,true);
        }
    }
    private void extractDateOfBirth(String ocrText,List<ExtractedField> fields){
        Pattern pattern=Pattern.compile("(?i)(date\\s*of\\s*birth|dob|birth)\\s*[:\\-]?\\s*"+DATE_PATTERN);
        Matcher matcher=pattern.matcher(ocrText);
        if(!matcher.find()){
            return;
        }
        String date=extractDateFromMatcher(matcher);
        if(date==null){
            return;
        }
        addField(fields,ExtractedFieldType.DATE_OF_BIRTH,date,true,true,true);
    }
    private void extractIssueDate(String ocrText,List<ExtractedField> fields){
        Pattern pattern=Pattern.compile("(?i)(date\\s*of\\s*issue|issue\\s*date|issued)\\s*[:\\-]?\\s*"+DATE_PATTERN);
        Matcher matcher=pattern.matcher(ocrText);
        if(!matcher.find()){
            return;
        }
        String date=extractDateFromMatcher(matcher);
        if(date==null){
            return;
        }
        addField(fields,ExtractedFieldType.ISSUE_DATE,date,true,true,true);
    }
    private void extractExpiryDate(String ocrText,List<ExtractedField> fields){
        Pattern pattern=Pattern.compile("(?i)(date\\s*of\\s*expiry|expiry\\s*date|date\\s*of\\s*expiration|expires)\\s*[:\\-]?\\s*"+DATE_PATTERN);
        Matcher matcher=pattern.matcher(ocrText);
        if(!matcher.find()){
            return;
        }
        String date=extractDateFromMatcher(matcher);
        if(date==null){
            return;
        }
        addField(fields,ExtractedFieldType.EXPIRY_DATE,date,true,true,true);
    }
    private void extractGender(String ocrText,List<ExtractedField> fields){
        Pattern pattern=Pattern.compile("(?i)(gender|sex)\\s*[:\\-]?\\s*(M|F|MALE|FEMALE)");
        Matcher matcher=pattern.matcher(ocrText);
        if(!matcher.find()){
            return;
        }
        String gender=normalizeGender(matcher.group(2));
        addField(fields,ExtractedFieldType.GENDER,gender,true,true,true);
    }
    private void extractGenderFallback(String ocrText,List<ExtractedField> fields){
        if(ocrText==null||ocrText.isBlank()){
            return;
        }
        Pattern pattern=Pattern.compile("\\b(MALE|FEMALE|MASCULIN|FEMININ|M|F)\\b",Pattern.CASE_INSENSITIVE);
        Matcher matcher=pattern.matcher(ocrText);
        if(!matcher.find()){
            return;
        }
        String gender=normalizeGender(matcher.group(1));
        addField(fields,ExtractedFieldType.GENDER,gender,false,true,true);
    }
    private void extractFromOcrWords(List<OcrWord> originalWords,List<ExtractedField> fields){
        List<OcrWord> words=new ArrayList<>(originalWords);
        words.sort(Comparator.comparingInt(OcrWord::getY).thenComparingInt(OcrWord::getX));
        extractPassportNumberFromWords(words,fields);
        if(!hasField(fields,ExtractedFieldType.LAST_NAME)){
            String surname=findValueNearLabel(words,"SURNAME","FAMILY","LAST");
            if(isValidName(surname)){
                addField(fields,ExtractedFieldType.LAST_NAME,cleanName(surname),true,true,true);
            }
        }
        if(!hasField(fields,ExtractedFieldType.FIRST_NAME)){
            String givenName=findMultiWordValueNearLabel(words,"GIVEN","FIRST");
            if(isValidName(givenName)){
                addField(fields,ExtractedFieldType.FIRST_NAME,cleanName(givenName),true,true,true);
            }
        }
        if(!hasField(fields,ExtractedFieldType.NATIONALITY)){
            String nationality=findValueNearLabel(words,"NATIONALITY","NATIONALTY");
            if(isValidName(nationality)){
                addField(fields,ExtractedFieldType.NATIONALITY,cleanName(nationality),true,true,true);
            }
        }
        if(!hasField(fields,ExtractedFieldType.GENDER)){
            String gender=findGenderNearLabel(words);
            if(gender!=null){
                addField(fields,ExtractedFieldType.GENDER,gender,true,true,true);
            }
        }
        extractDateNearLabel(words,fields,ExtractedFieldType.DATE_OF_BIRTH,"BIRTH","BIRTN","DOB");
        extractDateNearLabel(words,fields,ExtractedFieldType.ISSUE_DATE,"ISSUE","ISSUED","LAWUE");
        extractDateNearLabel(words,fields,ExtractedFieldType.EXPIRY_DATE,"EXPIRY","EXPIRATION","EXPIRES");
    }
    private String findValueNearLabel(List<OcrWord> words,String... labels){
        for(int i=0;i<words.size();i++){
            OcrWord label=words.get(i);
            String labelText=cleanToken(label.getText());
            if(!containsAnyLabel(labelText,labels)){
                continue;
            }
            for(int j=i+1;j<Math.min(i+5,words.size());j++){
                OcrWord candidate=words.get(j);
                if(!isNearby(label,candidate)){
                    continue;
                }
                String value=cleanName(candidate.getText());
                if(isValidName(value)){
                    return value;
                }
            }
        }
        return null;
    }
    private String findMultiWordValueNearLabel(List<OcrWord> words,String... labels){
        for(int i=0;i<words.size();i++){
            OcrWord label=words.get(i);
            String labelText=cleanToken(label.getText());
            if(!containsAnyLabel(labelText,labels)){
                continue;
            }
            List<String> values=new ArrayList<>();
            int baseY=label.getY();
            for(int j=i+1;j<words.size()&&j<i+12;j++){
                OcrWord candidate=words.get(j);
                if(candidate.getY()-baseY>90){
                    break;
                }
                if(!isNearby(label,candidate)){
                    continue;
                }
                String value=cleanName(candidate.getText());
                if(!isValidName(value)){
                    continue;
                }
                if(looksLikeLabel(value)){
                    continue;
                }
                values.add(value);
            }
            if(!values.isEmpty()){
                return String.join(" ",values);
            }
        }
        return null;
    }
    private String findGenderNearLabel(List<OcrWord> words){
        for(int i=0;i<words.size();i++){
            OcrWord label=words.get(i);
            String value=cleanToken(label.getText());
            if(!value.equals("GENDER")&&!value.equals("SEX")){
                continue;
            }
            for(int j=i+1;j<Math.min(i+6,words.size());j++){
                OcrWord candidate=words.get(j);
                if(!isNearby(label,candidate)){
                    continue;
                }
                String gender=normalizeGender(candidate.getText());
                if(gender.equals("M")||gender.equals("F")){
                    return gender;
                }
            }
        }
        return null;
    }
    private void extractDateNearLabel(List<OcrWord> words,List<ExtractedField> fields,ExtractedFieldType fieldType,String... labels){
        if(hasField(fields,fieldType)){
            return;
        }
        for(int i=0;i<words.size();i++){
            OcrWord label=words.get(i);
            String labelText=cleanToken(label.getText());
            if(!containsAnyLabel(labelText,labels)){
                continue;
            }
            for(int j=i+1;j<Math.min(i+8,words.size());j++){
                OcrWord candidate=words.get(j);
                if(!isNearby(label,candidate)){
                    continue;
                }
                String date=normalizeDate(candidate.getText());
                if(date==null){
                    continue;
                }
                addField(fields,fieldType,date,true,true,true);
                return;
            }
        }
    }
    private boolean isNearby(OcrWord label,OcrWord value){
        int labelCenterY=label.getY()+label.getHeight()/2;
        int valueCenterY=value.getY()+value.getHeight()/2;
        int verticalDifference=Math.abs(labelCenterY-valueCenterY);
        int horizontalDistance=value.getX()-(label.getX()+label.getWidth());
        boolean sameLine=verticalDifference<=Math.max(30,label.getHeight());
        boolean rightSide=horizontalDistance>=-50&&horizontalDistance<=600;
        boolean below=value.getY()>=label.getY()&&value.getY()-label.getY()<=100;
        return sameLine&&(rightSide||below);
    }
    private boolean isVeryClose(OcrWord label,OcrWord value){
        int labelCenterX=label.getX()+label.getWidth()/2;
        int labelCenterY=label.getY()+label.getHeight()/2;
        int valueCenterX=value.getX()+value.getWidth()/2;
        int valueCenterY=value.getY()+value.getHeight()/2;
        return Math.abs(labelCenterX-valueCenterX)<400&&Math.abs(labelCenterY-valueCenterY)<100;
    }
    private boolean containsAnyLabel(String value,String... labels){
        if(value==null){
            return false;
        }
        String normalized=value.toUpperCase().replaceAll("[^A-Z]","");
        for(String label:labels){
            String normalizedLabel=label.toUpperCase().replaceAll("[^A-Z]","");
            if(normalized.contains(normalizedLabel)){
                return true;
            }
        }
        return false;
    }
    private boolean looksLikeLabel(String value){
        if(value==null){
            return false;
        }
        String text=value.toUpperCase();
        return text.equals("SURNAME")||text.equals("GIVEN")||text.equals("NAMES")||text.equals("NAME")||text.equals("NATIONALITY")||text.equals("NATIONALTY")||text.equals("GENDER")||text.equals("SEX")||text.contains("DATE");
    }
    private String cleanToken(String value){
        if(value==null){
            return "";
        }
        return value.trim().toUpperCase();
    }
    private String normalizeGender(String gender){
        if(gender==null){
            return "";
        }
        String value=gender.trim().toUpperCase();
        if(value.equals("MALE")||value.equals("MASCULIN")||value.equals("M")){
            return "M";
        }
        if(value.equals("FEMALE")||value.equals("FEMININ")||value.equals("F")){
            return "F";
        }
        return value;
    }
    private void extractFullName(List<ExtractedField> fields){
        String firstName=null;
        String lastName=null;
        for(ExtractedField field:fields){
            if(field.getExtractedFieldType()==ExtractedFieldType.FIRST_NAME){
                firstName=field.getValue();
            }
            if(field.getExtractedFieldType()==ExtractedFieldType.LAST_NAME){
                lastName=field.getValue();
            }
        }
        if(firstName==null&&lastName==null){
            return;
        }
        String fullName=((firstName!=null?firstName:"")+" "+(lastName!=null?lastName:"")).trim();
        if(!fullName.isBlank()){
            addField(fields,ExtractedFieldType.FULL_NAME,fullName,true,true,true);
        }
    }
    private boolean hasField(List<ExtractedField> fields,ExtractedFieldType type){
        return fields.stream().anyMatch(field->field.getExtractedFieldType()==type);
    }
    private void addField(List<ExtractedField> fields,ExtractedFieldType fieldType,String value,boolean labelFound,boolean patternValid,boolean valueValid){
        if(value==null||value.isBlank()){
            return;
        }
        if(hasField(fields,fieldType)){
            return;
        }
        double confidence=FieldConfidenceCanculator.calculate(labelFound,patternValid,valueValid);
        fields.add(ExtractedField.builder()
                .extractedFieldType(fieldType)
                .value(value.trim())
                .confidencce(confidence)
                .source(ExtractedSource.OCR)
                .build());
    }
    private String extractDateFromMatcher(Matcher matcher){
        for(int i=matcher.groupCount();i>=1;i--){
            String group=matcher.group(i);
            if(group!=null&&group.matches("(?i).*\\d{4}.*")){
                String normalized=normalizeDate(group);
                if(normalized!=null){
                    return normalized;
                }
            }
        }
        return null;
    }
    private String normalizeDate(String date){
        if(date==null||date.isBlank()){
            return null;
        }
        String value=date.trim().replace(",","").replaceAll("\\s+"," ");
        String normalized=DataNomalizer.normalize(value);
        if(normalized!=null&&!normalized.isBlank()){
            return normalized;
        }
        String[] numericFormats={"dd/MM/yyyy","dd-MM-yyyy","d/M/yyyy","d-M-yyyy"};
        for(String format:numericFormats){
            try{
                LocalDate parsed=LocalDate.parse(value,DateTimeFormatter.ofPattern(format));
                return parsed.toString();
            }catch(DateTimeParseException ignored){
            }
        }
        String[] textFormats={"d MMM yyyy","dd MMM yyyy","d MMMM yyyy","dd MMMM yyyy"};
        for(String format:textFormats){
            try{
                LocalDate parsed=LocalDate.parse(value,DateTimeFormatter.ofPattern(format,Locale.ENGLISH));
                return parsed.toString();
            }catch(DateTimeParseException ignored){
            }
        }
        return null;
    }
    private String cleanName(String value){
        if(value==null){
            return null;
        }
        return value.replaceAll("[^A-Za-zÀ-ÿ ]","").replaceAll("\\s+"," ").trim().toUpperCase();
    }
    private String cleanAlphaNumeric(String value){
        if(value==null){
            return null;
        }
        return value.replaceAll("[^A-Za-z0-9]","").toUpperCase();
    }
    private boolean isValidName(String value){
        return value!=null&&value.length()>=2&&value.matches("[A-ZÀ-Ÿ ]+");
    }
} 