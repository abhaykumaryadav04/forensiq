package com.forensiq.identity_scanning.varification.tempering;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import com.forensiq.identity_scanning.ai.dto.OllamaTamperingAnalysisResult;
import com.forensiq.identity_scanning.ai.dto.service.OllamaTamperingAnalysisService;
import com.forensiq.identity_scanning.information.dto.ExtractedField;
import com.forensiq.identity_scanning.information.dto.ExtractionResponse;
import com.forensiq.identity_scanning.ml.dto.FieldTamperingRisk;
import com.forensiq.identity_scanning.ocr.dto.OcrWord;
import com.forensiq.identity_scanning.varification.tempering.dto.CopyMoveAnalysisResult;
import com.forensiq.identity_scanning.varification.tempering.dto.ElaAnalysisResult;
import com.forensiq.identity_scanning.varification.tempering.dto.MetadataAnalysisResult;
import com.forensiq.identity_scanning.varification.tempering.dto.NoiseAnalysisResult;
import com.forensiq.identity_scanning.varification.tempering.dto.TamperingDetectionResult;
import com.forensiq.identity_scanning.varification.tempering.dto.TemperingIndicator;
@Service
public class TamperingDetectionService {
    private final ErrorLevelAnalyzer errorLevelAnalyzer;
    private final NoiseConsistencyAnalyzer noiseConsistencyAnalyzer;
    private final CopyMoveDetector copyMoveDetector;
    private final ImageMetadataAnalyzer imageMetadataAnalyzer;
    private final OllamaTamperingAnalysisService ollamaTamperingAnalysisService;
    public TamperingDetectionService(ErrorLevelAnalyzer errorLevelAnalyzer,NoiseConsistencyAnalyzer noiseConsistencyAnalyzer,CopyMoveDetector copyMoveDetector,ImageMetadataAnalyzer imageMetadataAnalyzer,OllamaTamperingAnalysisService ollamaTamperingAnalysisService){
        this.errorLevelAnalyzer=errorLevelAnalyzer;
        this.noiseConsistencyAnalyzer=noiseConsistencyAnalyzer;
        this.copyMoveDetector=copyMoveDetector;
        this.imageMetadataAnalyzer=imageMetadataAnalyzer;
        this.ollamaTamperingAnalysisService=ollamaTamperingAnalysisService;
    }
    public TamperingDetectionResult analyze(File imagePath,List<OcrWord> ocrWords,ExtractionResponse extractionResponse,String ocrText){
        if(imagePath==null||!imagePath.exists()){
            return TamperingDetectionResult.builder()
                    .analyzed(false)
                    .suspicious(false)
                    .tamperingScore(0)
                    .indicators(List.of())
                    .issues(List.of("Image file does not exist"))
                    .fieldTamperingRisks(List.of())
                    .build();
        }
        try{
            BufferedImage image=ImageIO.read(imagePath);
            if(image==null){
                return TamperingDetectionResult.builder()
                        .analyzed(false)
                        .suspicious(false)
                        .tamperingScore(0)
                        .indicators(List.of())
                        .issues(List.of("Unsupported or unreadable image"))
                        .fieldTamperingRisks(List.of())
                        .build();
            }
            ElaAnalysisResult elaResult=errorLevelAnalyzer.analyze(image);
            NoiseAnalysisResult noiseResult=noiseConsistencyAnalyzer.analyze(image);
            CopyMoveAnalysisResult copyMoveResult=copyMoveDetector.analyze(image);
            MetadataAnalysisResult metadataResult=imageMetadataAnalyzer.analyze(imagePath);
            double classicalScore=calculateClassicalScore(elaResult,noiseResult,copyMoveResult,metadataResult);
            OllamaTamperingAnalysisResult aiResult=ollamaTamperingAnalysisService.analyze(imagePath.toPath(),ocrText,extractionResponse);
            double aiScore=clamp(aiResult.getTamperingScore(),0,100);
            double finalScore=combineScores(classicalScore,aiScore);
            List<FieldTamperingRisk> fieldRisks=buildFieldTamperingRisks(aiResult,extractionResponse);
            List<TemperingIndicator> indicators=buildIndicators(elaResult,noiseResult,copyMoveResult,metadataResult,aiResult);
            List<String> issues=buildIssues(indicators,aiResult,fieldRisks);
            boolean suspicious=finalScore>=50||aiResult.isSuspicious();
            return TamperingDetectionResult.builder()
                    .analyzed(true)
                    .suspicious(suspicious)
                    .tamperingScore(finalScore)
                    .elaResult(elaResult)
                    .noiseResult(noiseResult)
                    .copyMoveResult(copyMoveResult)
                    .metadataResult(metadataResult)
                    .aiAnalysis(aiResult)
                    .indicators(indicators)
                    .issues(issues)
                    .fieldTamperingRisks(fieldRisks)
                    .build();
        }catch(Exception exception){
            return TamperingDetectionResult.builder()
                    .analyzed(false)
                    .suspicious(false)
                    .tamperingScore(0)
                    .indicators(List.of())
                    .issues(List.of("Tampering analysis failed: "+exception.getMessage()))
                    .fieldTamperingRisks(List.of())
                    .build();
        }
    }
    private double calculateClassicalScore(ElaAnalysisResult ela,NoiseAnalysisResult noise,CopyMoveAnalysisResult copyMove,MetadataAnalysisResult metadata){
        double score=(ela.getSuspicionScore()*0.30)
                +(noise.getSuspicionScore()*0.25)
                +(copyMove.getSuspicionScore()*0.30)
                +(metadata.getSuspicionScore()*0.15);
        return clamp(score,0,100);
    }
    private double combineScores(double classicalScore,double aiScore){
        double score=(classicalScore*0.45)+(aiScore*0.55);
        return round(clamp(score,0,100));
    }
    private List<TemperingIndicator> buildIndicators(ElaAnalysisResult ela,NoiseAnalysisResult noise,CopyMoveAnalysisResult copyMove,MetadataAnalysisResult metadata,OllamaTamperingAnalysisResult aiResult){
        List<TemperingIndicator> indicators=new ArrayList<>();
        indicators.add(TemperingIndicator.builder()
                .analyzerName("Error Level Analysis")
                .description(ela.getMessage())
                .suspicionScore(ela.getSuspicionScore())
                .suspicious(ela.isSuspicious())
                .build());
        indicators.add(TemperingIndicator.builder()
                .analyzerName("Noise Consistency Analysis")
                .description(noise.getMessage())
                .suspicionScore(noise.getSuspicionScore())
                .suspicious(noise.isSuspicious())
                .build());
        indicators.add(TemperingIndicator.builder()
                .analyzerName("Copy-Move Detection")
                .description(copyMove.getMessage())
                .suspicionScore(copyMove.getSuspicionScore())
                .suspicious(copyMove.isSuspicious())
                .build());
        indicators.add(TemperingIndicator.builder()
                .analyzerName("Metadata Analysis")
                .description(metadata.getMessage())
                .suspicionScore(metadata.getSuspicionScore())
                .suspicious(metadata.isSuspicious())
                .build());
        StringBuilder aiDescription=new StringBuilder("Ollama visual tampering analysis completed");
        if(aiResult.getAttackType()!=null&&!aiResult.getAttackType().equals("UNKNOWN")){
            aiDescription.append("; possible attack type: ").append(aiResult.getAttackType());
        }
        if(!aiResult.getSuspiciousFields().isEmpty()){
            aiDescription.append("; suspicious fields: ").append(String.join(", ",aiResult.getSuspiciousFields()));
        }
        indicators.add(TemperingIndicator.builder()
                .analyzerName("Ollama Visual Analysis")
                .description(aiDescription.toString())
                .suspicionScore(aiResult.getTamperingScore())
                .suspicious(aiResult.isSuspicious())
                .build());
        return indicators;
    }
    private List<String> buildIssues(List<TemperingIndicator> indicators,OllamaTamperingAnalysisResult aiResult,List<FieldTamperingRisk> fieldRisks){
        List<String> issues=indicators.stream()
                .filter(TemperingIndicator::isSuspicious)
                .map(TemperingIndicator::getDescription)
                .toList();
        List<String> result=new ArrayList<>(issues);
        if(aiResult.getObservations()!=null){
            for(String observation:aiResult.getObservations()){
                if(observation!=null&&!observation.isBlank()&&!result.contains(observation)){
                    result.add(observation);
                }
            }
        }
        if(fieldRisks!=null){
            for(FieldTamperingRisk fieldRisk:fieldRisks){
                if(fieldRisk!=null&&fieldRisk.getReason()!=null&&!fieldRisk.getReason().isBlank()&&!result.contains(fieldRisk.getReason())){
                    result.add(fieldRisk.getReason());
                }
            }
        }
        return result;
    }
    private List<FieldTamperingRisk> buildFieldTamperingRisks(OllamaTamperingAnalysisResult aiResult,ExtractionResponse extractionResponse){
        if(aiResult==null||aiResult.getSuspiciousFields()==null||aiResult.getSuspiciousFields().isEmpty()){
            return List.of();
        }
        List<FieldTamperingRisk> results=new ArrayList<>();
        int regionId=1;
        for(String suspiciousField:aiResult.getSuspiciousFields()){
            if(suspiciousField==null||suspiciousField.isBlank()){
                continue;
            }
            String normalizedField=normalizeField(suspiciousField);
            String affectedText=findFieldValue(normalizedField,extractionResponse);
            double riskScore=calculateFieldRisk(aiResult.getTamperingScore(),aiResult.getAttackConfidence());
            String level=riskScore>=80?"HIGH":riskScore>=60?"MEDIUM":riskScore>=40?"LOW":"MINIMAL";
            String reason="Ollama identified possible visual manipulation associated with field "+normalizedField;
            results.add(FieldTamperingRisk.builder()
                    .regionId(regionId++)
                    .affectedField(normalizedField)
                    .affectedText(affectedText)
                    .fieldRiskScore(round(riskScore))
                    .riskLevel(level)
                    .mappingConfidence(0)
                    .regionConfidence(0)
                    .attackType(aiResult.getAttackType())
                    .attackConfidence(round(aiResult.getAttackConfidence()))
                    .reason(reason)
                    .build());
        }
        return results;
    }
    private double calculateFieldRisk(double tamperingScore,double attackConfidence){
        double score=(clamp(tamperingScore,0,100)*0.65)+(clamp(attackConfidence,0,100)*0.35);
        return clamp(score,0,100);
    }
    private String findFieldValue(String normalizedField,ExtractionResponse extractionResponse){
        if(extractionResponse==null||extractionResponse.getFields()==null){
            return null;
        }
        for(ExtractedField field:extractionResponse.getFields()){
            if(field==null||field.getExtractedFieldType()==null){
                continue;
            }
            String fieldType=normalizeField(field.getExtractedFieldType().name());
            if(fieldType.equals(normalizedField)||fieldType.contains(normalizedField)||normalizedField.contains(fieldType)){
                return field.getValue();
            }
        }
        return null;
    }
    private String normalizeField(String value){
        String normalized=value.trim().toUpperCase(Locale.ROOT)
                .replace("-","_")
                .replace(" ","_");
        if(normalized.contains("DOB")||normalized.contains("BIRTH")){
            return "DATE_OF_BIRTH";
        }
        if(normalized.contains("EXPIRY")||normalized.contains("EXPIRATION")){
            return "DATE_OF_EXPIRY";
        }
        if(normalized.contains("ISSUE")){
            return "DATE_OF_ISSUE";
        }
        if(normalized.contains("PASSPORT")&&normalized.contains("NUMBER")){
            return "PASSPORT_NUMBER";
        }
        if(normalized.contains("NAME")&&normalized.contains("SURNAME")){
            return "SURNAME";
        }
        if(normalized.equals("NAME")||normalized.contains("FULL_NAME")||normalized.contains("GIVEN_NAME")){
            return "NAME";
        }
        if(normalized.contains("NATIONALITY")){
            return "NATIONALITY";
        }
        if(normalized.contains("GENDER")||normalized.equals("SEX")){
            return "SEX";
        }
        if(normalized.contains("ADDRESS")){
            return "ADDRESS";
        }
        if(normalized.contains("PLACE")&&normalized.contains("BIRTH")){
            return "PLACE_OF_BIRTH";
        }
        if(normalized.contains("IDENTITY")&&normalized.contains("NUMBER")){
            return "IDENTITY_NUMBER";
        }
        return normalized;
    }
    private double clamp(double value,double min,double max){
        return Math.max(min,Math.min(max,value));
    }
    private double round(double value){
        return Math.round(value*100.0)/100.0;
    }
}