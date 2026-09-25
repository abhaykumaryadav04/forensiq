package com.forensiq.identity_scanning.processing.service;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.forensiq.identity_scanning.ai.dto.OllamaTamperingAnalysisResult;
import com.forensiq.identity_scanning.ml.dto.FieldTamperingRisk;
import com.forensiq.identity_scanning.processing.dto.CentralRiskResult;
import com.forensiq.identity_scanning.processing.dto.VarificationProcessingResult;
import com.forensiq.identity_scanning.varification.enumeration.VarificationStatus;
import com.forensiq.identity_scanning.varification.mrz.dto.MrzValidationResult;
import com.forensiq.identity_scanning.varification.qr.dto.QrValidationResult;
import com.forensiq.identity_scanning.varification.tempering.dto.CopyMoveAnalysisResult;
import com.forensiq.identity_scanning.varification.tempering.dto.ElaAnalysisResult;
import com.forensiq.identity_scanning.varification.tempering.dto.MetadataAnalysisResult;
import com.forensiq.identity_scanning.varification.tempering.dto.NoiseAnalysisResult;
import com.forensiq.identity_scanning.varification.tempering.dto.TamperingDetectionResult;
// MRZ              → 20%
// QR               → 10%
// Cross-field      → 20%   Σ(component risk × weight)
//Risk Score = ─────────────────────────────────
       //          Σ(available weights)
// Additional       → 10%
// Classical        → 15%
// Ollama           → 15%
// Field risk       → 10%
 @Service
public class CentralRiskEngine {
    public CentralRiskResult calculateRisk(VarificationProcessingResult verificationResult,TamperingDetectionResult tamperingResult){
        if(verificationResult==null){
            throw new IllegalArgumentException("Verification result cannot be null");
        }
        double weightedRisk=0;
        double totalWeight=0;
        int checksPerformed=0;
        List<String> reasons=new ArrayList<>();
        List<String> warnings=new ArrayList<>();
        Map<String,Double> componentRiskScores=new LinkedHashMap<>();
        MrzValidationResult mrzResult=verificationResult.getMrzValidatinResult();
        if(mrzResult!=null){
            double risk=calculateMrzRisk(mrzResult);
            weightedRisk+=risk*0.20;
            totalWeight+=0.20;
            checksPerformed++;
            componentRiskScores.put("MRZ",round(risk));
            addMrzEvidence(mrzResult,risk,reasons,warnings);
        }else{
            warnings.add("MRZ verification was not applicable or available");
        }
        QrValidationResult qrResult=verificationResult.getQrvalidationResult();
        if(qrResult!=null&&qrResult.isDetected()){
            double risk=calculateQrRisk(qrResult);
            weightedRisk+=risk*0.10;
            totalWeight+=0.10;
            checksPerformed++;
            componentRiskScores.put("QR_BARCODE",round(risk));
            addQrEvidence(qrResult,risk,reasons,warnings);
        }else{
            warnings.add("No QR or barcode was available for verification");
        }
        VarificationStatus crossFieldStatus=verificationResult.getCrossfieldCheck();
        if(crossFieldStatus!=null&&crossFieldStatus!=VarificationStatus.NOT_AVAILABLE){
            double risk=verificationResult.getCrossFieldRiskScore();
            if(risk<=0){
                risk=calculateCrossFieldRisk(crossFieldStatus);
            }
            weightedRisk+=risk*0.20;
            totalWeight+=0.20;
            checksPerformed++;
            componentRiskScores.put("CROSS_FIELD",round(risk));
            addCrossFieldEvidence(crossFieldStatus,risk,verificationResult,reasons,warnings);
        }else{
            warnings.add("Cross-field verification was not available");
        }
        if(verificationResult.getVarificationResult()!=null&&verificationResult.getVarificationResult().getRiskScore()!=null){
            double risk=clamp(verificationResult.getVarificationResult().getRiskScore());
            weightedRisk+=risk*0.10;
            totalWeight+=0.10;
            checksPerformed++;
            componentRiskScores.put("ADDITIONAL_VERIFICATION",round(risk));
            if(risk>=60){
                reasons.add("Additional verification produced elevated risk");
            }else if(risk>=30){
                warnings.add("Additional verification produced a warning");
            }
        }else{
            warnings.add("Additional verification risk was not available");
        }
        if(tamperingResult!=null&&tamperingResult.isAnalyzed()){
            double classicalRisk=calculateClassicalForensicRisk(tamperingResult);
            weightedRisk+=classicalRisk*0.15;
            totalWeight+=0.15;
            checksPerformed++;
            componentRiskScores.put("CLASSICAL_FORENSICS",round(classicalRisk));
            addClassicalForensicEvidence(tamperingResult,classicalRisk,reasons,warnings);
            OllamaTamperingAnalysisResult aiResult=tamperingResult.getAiAnalysis();
            if(aiResult!=null){
                double aiRisk=clamp(aiResult.getTamperingScore());
                weightedRisk+=aiRisk*0.15;
                totalWeight+=0.15;
                checksPerformed++;
                componentRiskScores.put("OLLAMA_AI",round(aiRisk));
                addAiEvidence(aiResult,aiRisk,reasons,warnings);
            }else{
                warnings.add("Ollama tampering analysis was not available");
            }
            List<FieldTamperingRisk> fieldRisks=tamperingResult.getFieldTamperingRisks();
            if(fieldRisks!=null&&!fieldRisks.isEmpty()){
                double fieldRisk=calculateAggregateFieldRisk(fieldRisks);
                weightedRisk+=fieldRisk*0.10;
                totalWeight+=0.10;
                checksPerformed++;
                componentRiskScores.put("FIELD_TAMPERING",round(fieldRisk));
                addFieldRiskEvidence(fieldRisks,reasons,warnings);
            }else{
                warnings.add("Field-level tampering evidence was not available");
            }
        }else{
            warnings.add("Classical forensic analysis was not available");
            warnings.add("Ollama tampering analysis was not available");
            warnings.add("Field-level tampering evidence was not available");
        }
        double finalRiskScore=totalWeight>0?weightedRisk/totalWeight:0;
        finalRiskScore=round(clamp(finalRiskScore));
        String verdict=determineVerdict(finalRiskScore,reasons,warnings);
        String decisionConfidence=calculateDecisionConfidence(checksPerformed,finalRiskScore);
        return CentralRiskResult.builder()
                .riskScore(finalRiskScore)
                .verdict(verdict)
                .reasons(removeDuplicates(reasons))
                .warnings(removeDuplicates(warnings))
                .checksAvailable(componentRiskScores.size())
                .checksPerformed(checksPerformed)
                .decisionConfidence(decisionConfidence)
                .componentRiskScores(componentRiskScores)
                .build();
    }
    private double calculateMrzRisk(MrzValidationResult result){
        if(result.isValid()){
            return 0;
        }
        double risk=50;
        if(!result.isFormatValid()){
            risk+=20;
        }
        if(!result.isCheckDigitsValid()){
            risk+=20;
        }
        if(!result.isCrossFieldValid()){
            risk+=20;
        }
        return clamp(risk);
    }
    private double calculateQrRisk(QrValidationResult result){
        if(result.isValid()&&result.isDecoded()&&result.isDataValid()&&result.isCrossFieldCheck()){
            return 0;
        }
        double risk=35;
        if(!result.isDecoded()){
            risk+=20;
        }
        if(!result.isDataValid()){
            risk+=20;
        }
        if(!result.isCrossFieldCheck()){
            risk+=25;
        }
        return clamp(risk);
    }
    private double calculateCrossFieldRisk(VarificationStatus status){
        return switch(status){
            case PASSED -> 0;
            case WARNING -> 35;
            case FAILED -> 75;
            case NOT_AVAILABLE -> 0;
        };
    }
    private double calculateClassicalForensicRisk(TamperingDetectionResult result){
        ElaAnalysisResult ela=result.getElaResult();
        NoiseAnalysisResult noise=result.getNoiseResult();
        CopyMoveAnalysisResult copyMove=result.getCopyMoveResult();
        MetadataAnalysisResult metadata=result.getMetadataResult();
        double weighted=0;
        double weight=0;
        if(ela!=null){
            weighted+=clamp(ela.getSuspicionScore())*0.30;
            weight+=0.30;
        }
        if(noise!=null){
            weighted+=clamp(noise.getSuspicionScore())*0.25;
            weight+=0.25;
        }
        if(copyMove!=null){
            weighted+=clamp(copyMove.getSuspicionScore())*0.30;
            weight+=0.30;
        }
        if(metadata!=null){
            weighted+=clamp(metadata.getSuspicionScore())*0.15;
            weight+=0.15;
        }
        return weight>0?weighted/weight:0;
    }
    private double calculateAggregateFieldRisk(List<FieldTamperingRisk> fieldRisks){
        if(fieldRisks==null||fieldRisks.isEmpty()){
            return 0;
        }
        double total=0;
        double highest=0;
        int count=0;
        for(FieldTamperingRisk fieldRisk:fieldRisks){
            if(fieldRisk==null){
                continue;
            }
            double risk=clamp(fieldRisk.getFieldRiskScore());
            total+=risk;
            highest=Math.max(highest,risk);
            count++;
        }
        if(count==0){
            return 0;
        }
        double average=total/count;
        return clamp((highest*0.70)+(average*0.30));
    }
    private void addMrzEvidence(MrzValidationResult result,double risk,List<String> reasons,List<String> warnings){
        if(risk>=60){
            reasons.add("MRZ validation produced a significant inconsistency");
        }else if(risk>0){
            warnings.add("MRZ validation produced a warning");
        }else{
            warnings.add("MRZ validation passed");
        }
        if(!result.isFormatValid()){
            reasons.add("MRZ format validation failed");
        }
        if(!result.isCheckDigitsValid()){
            reasons.add("MRZ check-digit validation failed");
        }
        if(!result.isCrossFieldValid()){
            reasons.add("MRZ fields do not fully agree with extracted document fields");
        }
    }
    private void addQrEvidence(QrValidationResult result,double risk,List<String> reasons,List<String> warnings){
        if(risk>=60){
            reasons.add("QR or barcode verification produced a significant inconsistency");
        }else if(risk>0){
            warnings.add("QR or barcode verification produced a warning");
        }else{
            warnings.add("QR or barcode verification passed");
        }
        if(!result.isDecoded()){
            reasons.add("Detected QR or barcode could not be decoded");
        }
        if(!result.isDataValid()){
            reasons.add("QR or barcode data validation failed");
        }
        if(!result.isCrossFieldCheck()){
            reasons.add("QR or barcode data does not fully agree with extracted fields");
        }
    }
    private void addCrossFieldEvidence(VarificationStatus status,double risk,VarificationProcessingResult result,List<String> reasons,List<String> warnings){
        if(status==VarificationStatus.FAILED){
            reasons.add("Cross-field verification failed");
        }else if(status==VarificationStatus.WARNING){
            warnings.add("Cross-field verification produced a warning");
        }else if(status==VarificationStatus.PASSED){
            warnings.add("Cross-field verification passed");
        }
        if(result.getCrossFieldIssues()!=null){
            reasons.addAll(result.getCrossFieldIssues());
        }
        if(result.getCrossFieldWarnings()!=null){
            warnings.addAll(result.getCrossFieldWarnings());
        }
    }
    private void addClassicalForensicEvidence(TamperingDetectionResult result,double risk,List<String> reasons,List<String> warnings){
        if(result.isSuspicious()&&risk>=60){
            reasons.add("Classical image-forensic analysis detected suspicious characteristics");
        }else if(risk>=30){
            warnings.add("Classical image-forensic analysis produced moderate suspicion");
        }else{
            warnings.add("Classical image-forensic analysis did not produce significant anomalies");
        }
    }
    private void addAiEvidence(OllamaTamperingAnalysisResult aiResult,double risk,List<String> reasons,List<String> warnings){
        if(aiResult.isSuspicious()&&risk>=60){
            reasons.add("Ollama visual analysis identified potentially suspicious manipulation");
        }else if(risk>=30){
            warnings.add("Ollama visual analysis produced moderate suspicion");
        }else{
            warnings.add("Ollama visual analysis did not identify significant manipulation indicators");
        }
        if(aiResult.getSuspiciousFields()!=null){
            for(String field:aiResult.getSuspiciousFields()){
                if(field!=null&&!field.isBlank()&&risk>=50){
                    reasons.add("Ollama identified a potentially suspicious field: "+field);
                }
            }
        }
    }
    private void addFieldRiskEvidence(List<FieldTamperingRisk> fieldRisks,List<String> reasons,List<String> warnings){
        for(FieldTamperingRisk fieldRisk:fieldRisks){
            if(fieldRisk==null){
                continue;
            }
            String field=fieldRisk.getAffectedField();
            if(field==null||field.isBlank()){
                field="UNKNOWN_FIELD";
            }
            double risk=clamp(fieldRisk.getFieldRiskScore());
            if(risk>=80){
                reasons.add("High field-level tampering risk detected for "+field);
            }else if(risk>=60){
                warnings.add("Medium field-level tampering risk detected for "+field);
            }else if(risk>=40){
                warnings.add("Low field-level tampering risk detected for "+field);
            }
        }
    }
    private String determineVerdict(double riskScore,List<String> reasons,List<String> warnings){
        if(riskScore>=80){
            return "HIGH_RISK";
        }
        if(riskScore>=60){
            return "SUSPICIOUS";
        }
        if(riskScore>=40){
            return "REVIEW_REQUIRED";
        }
        if(riskScore>=20){
            return "LOW_RISK";
        }
        if(!reasons.isEmpty()){
            return "REVIEW_REQUIRED";
        }
        return "AUTHENTIC";
    }
    private String calculateDecisionConfidence(int checksPerformed,double riskScore){
        if(checksPerformed<=1){
            return "LOW";
        }
        if(checksPerformed==2){
            return "MEDIUM";
        }
        if(checksPerformed>=4&&(riskScore>=80||riskScore<20)){
            return "HIGH";
        }
        return "MEDIUM";
    }
    private double clamp(double value){
        return Math.max(0,Math.min(100,value));
    }
    private double round(double value){
        return Math.round(value*100.0)/100.0;
    }
    private List<String> removeDuplicates(List<String> values){
        return new ArrayList<>(new java.util.LinkedHashSet<>(values));
    }
}