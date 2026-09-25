package com.forensiq.identity_scanning.processing.service;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.document.entity.DocumentType;
import com.forensiq.identity_scanning.information.dto.ExtractedField;
import com.forensiq.identity_scanning.information.dto.ExtractionResponse;
import com.forensiq.identity_scanning.information.model.ExtractedFieldType;
import com.forensiq.identity_scanning.ocr.dto.OcrWord;
import com.forensiq.identity_scanning.processing.dto.CrossFieldEvaluation;
import com.forensiq.identity_scanning.processing.dto.VarificationProcessingResult;
import com.forensiq.identity_scanning.varification.mrz.MrzExtractor;
import com.forensiq.identity_scanning.varification.mrz.MrzvalidationSevice;
import com.forensiq.identity_scanning.varification.mrz.dto.MrzValidationResult;
import com.forensiq.identity_scanning.varification.qr.QrValidationService;
import com.forensiq.identity_scanning.varification.qr.dto.QrValidationResult;
import com.forensiq.identity_scanning.varification.tempering.dto.TamperingDetectionResult;
@Service
public class VerificationProcessingService {
    private final MrzvalidationSevice mrzvalidationSevice;
    private final QrValidationService qrValidationService;
    private final MrzExtractor mrzExtractor;
    private final CrossFieldVerificationService crossFieldVerificationService;
    public VerificationProcessingService(MrzvalidationSevice mrzvalidationSevice,QrValidationService qrValidationService,MrzExtractor mrzExtractor,CrossFieldVerificationService crossFieldVerificationService){
        this.mrzvalidationSevice=mrzvalidationSevice;
        this.qrValidationService=qrValidationService;
        this.mrzExtractor=mrzExtractor;
        this.crossFieldVerificationService=crossFieldVerificationService;
    }
    public VarificationProcessingResult process(List<OcrWord> ocrWords,Document document,ExtractionResponse extractionResponse,String ocrText,BufferedImage image,TamperingDetectionResult tamperingResult) throws IllegalAccessException {
        if(document==null){
            throw new IllegalArgumentException("Document cannot be null");
        }
        if(document.getDocumentType()==null){
            throw new IllegalArgumentException("Document type cannot be null");
        }
        if(extractionResponse==null){
            throw new IllegalArgumentException("Extraction response cannot be null");
        }
        if(ocrText==null||ocrText.isBlank()){
            throw new IllegalArgumentException("OCR text cannot be empty");
        }
        if(ocrWords==null||ocrWords.isEmpty()){
            throw new IllegalArgumentException("OCR words cannot be empty");
        }
        if(image==null){
            throw new IllegalArgumentException("Image cannot be null");
        }
        MrzValidationResult mrzValidationResult=null;
        if(document.getDocumentType()==DocumentType.PASSPORT){
            String extractedName=getFieldValue(extractionResponse,ExtractedFieldType.FULL_NAME);
            String extractedPassportNumber=getFieldValue(extractionResponse,ExtractedFieldType.PASSPORT_NUMBER);
            String extractedDateOfBirth=getFieldValue(extractionResponse,ExtractedFieldType.DATE_OF_BIRTH);
            String extractedExpiryDate=getFieldValue(extractionResponse,ExtractedFieldType.EXPIRY_DATE);
            String mrzText=mrzExtractor.extract(ocrWords);
            mrzValidationResult=mrzvalidationSevice.validate(mrzText,extractedName,extractedPassportNumber,extractedDateOfBirth,extractedExpiryDate);
        }
        Map<String,String> ocrFields=buildOcrFields(extractionResponse);
        QrValidationResult qrValidationResult=qrValidationService.validate(image,ocrFields);
        CrossFieldEvaluation evaluation=crossFieldVerificationService.evaluate(document,mrzValidationResult,qrValidationResult,tamperingResult);
        return VarificationProcessingResult.builder()
                .crossfieldCheck(evaluation.getStatus())
                .mrzValidatinResult(mrzValidationResult)
                .qrvalidationResult(qrValidationResult)
                .varificationResult(evaluation.getResult())
                .crossFieldRiskScore(evaluation.getRiskScore())
                .crossFieldChecksPerformed(evaluation.getChecksPerformed())
                .crossFieldChecksPassed(evaluation.getChecksPassed())
                .crossFieldIssues(evaluation.getIssues())
                .crossFieldWarnings(evaluation.getWarnings())
                .crossFieldEvidence(evaluation.getEvidence())
                .build();
    }
    private String getFieldValue(ExtractionResponse response,ExtractedFieldType fieldType){
        if(response==null||response.getFields()==null){
            return null;
        }
        return response.getFields()
                .stream()
                .filter(field->field.getExtractedFieldType()==fieldType)
                .map(ExtractedField::getValue)
                .filter(value->value!=null&&!value.isBlank())
                .findFirst()
                .orElse(null);
    }
    private Map<String,String> buildOcrFields(ExtractionResponse extractionResponse){
        Map<String,String> ocrFields=new HashMap<>();
        if(extractionResponse==null||extractionResponse.getFields()==null){
            return ocrFields;
        }
        extractionResponse.getFields().forEach(field->{
            if(field.getExtractedFieldType()!=null&&field.getValue()!=null&&!field.getValue().isBlank()){
                ocrFields.put(field.getExtractedFieldType().name(),field.getValue());
            }
        });
        return ocrFields;
    }
}