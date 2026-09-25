package com.forensiq.identity_scanning.processing.service;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.processing.dto.CrossFieldEvaluation;
import com.forensiq.identity_scanning.varification.dto.VarificationResult;
import com.forensiq.identity_scanning.varification.enumeration.VarificationStatus;
import com.forensiq.identity_scanning.varification.mrz.dto.MrzValidationResult;
import com.forensiq.identity_scanning.varification.qr.dto.QrValidationResult;
import com.forensiq.identity_scanning.varification.tempering.dto.TamperingDetectionResult;
import com.forensiq.identity_scanning.varification.verifirer.CrossFieldConsistencyVarifier;
@Service
public class CrossFieldVerificationService {
    private final CrossFieldConsistencyVarifier crossFieldConsistencyVarifier;
    public CrossFieldVerificationService(CrossFieldConsistencyVarifier crossFieldConsistencyVarifier){
        this.crossFieldConsistencyVarifier=crossFieldConsistencyVarifier;
    }
    public CrossFieldEvaluation evaluate(Document document,MrzValidationResult mrzResult,QrValidationResult qrResult,TamperingDetectionResult tamperingResult){
        if(document==null){
            throw new IllegalArgumentException("Document cannot be null");
        }
        List<String> issues=new ArrayList<>();
        List<String> warnings=new ArrayList<>();
        List<String> evidence=new ArrayList<>();
        int checksPerformed=0;
        int checksPassed=0;
        double weightedRisk=0;
        double totalWeight=0;
        VarificationResult internalResult=crossFieldConsistencyVarifier.varify(document);
        if(internalResult!=null&&internalResult.getVarificationStatus()!=VarificationStatus.NOT_AVAILABLE){
            checksPerformed++;
            totalWeight+=0.40;
            double risk=normalize(internalResult.getRiskScore());
            weightedRisk+=risk*0.40;
            if(internalResult.getVarificationStatus()==VarificationStatus.PASSED){
                checksPassed++;
                evidence.add("Internal document fields are mutually consistent");
            }else{
                addMessage(internalResult.getMessage(),issues);
                if(internalResult.getVarificationStatus()==VarificationStatus.WARNING){
                    warnings.add("Internal field consistency produced a warning");
                }else{
                    issues.add("Internal field consistency failed");
                }
            }
        }else{
            warnings.add("Internal field consistency was not available");
        }
        if(mrzResult!=null){
            checksPerformed++;
            totalWeight+=0.35;
            if(mrzResult.isCrossFieldValid()){
                checksPassed++;
                evidence.add("MRZ fields are consistent with extracted document fields");
            }else{
                weightedRisk+=70*0.35;
                issues.add("MRZ and extracted document fields are inconsistent");
                if(mrzResult.getIssues()!=null){
                    for(String issue:mrzResult.getIssues()){
                        if(issue!=null&&!issue.isBlank()&&!issues.contains(issue)){
                            issues.add("MRZ: "+issue);
                        }
                    }
                }
            }
            if(mrzResult.isCheckDigitsValid()){
                evidence.add("MRZ check digits are valid");
            }else{
                warnings.add("MRZ check digits are not valid");
            }
        }else{
            warnings.add("MRZ cross-field verification was not applicable or available");
        }
        if(qrResult!=null&&qrResult.isDetected()){
            if(qrResult.isDecoded()&&qrResult.isDataValid()){
                checksPerformed++;
                totalWeight+=0.25;
                if(qrResult.isCrossFieldCheck()&&qrResult.isValid()){
                    checksPassed++;
                    evidence.add("QR/barcode data is consistent with extracted document fields");
                }else{
                    weightedRisk+=70*0.25;
                    issues.add("QR/barcode data is inconsistent with extracted document fields");
                    if(qrResult.getIssues()!=null){
                        for(String issue:qrResult.getIssues()){
                            if(issue!=null&&!issue.isBlank()&&!issues.contains(issue)){
                                issues.add("QR/Barcode: "+issue);
                            }
                        }
                    }
                }
            }else{
                warnings.add("QR/barcode was detected but could not be fully validated");
            }
        }else{
            warnings.add("No QR/barcode was available for cross-field verification");
        }
        if(tamperingResult!=null&&tamperingResult.isAnalyzed()&&tamperingResult.getAiAnalysis()!=null){
            if(tamperingResult.getAiAnalysis().getSuspiciousFields()!=null&&!tamperingResult.getAiAnalysis().getSuspiciousFields().isEmpty()){
                for(String field:tamperingResult.getAiAnalysis().getSuspiciousFields()){
                    if(field!=null&&!field.isBlank()){
                        evidence.add("Ollama identified a visually suspicious field: "+field);
                    }
                }
            }
            if(tamperingResult.getAiAnalysis().getObservations()!=null){
                for(String observation:tamperingResult.getAiAnalysis().getObservations()){
                    if(observation!=null&&!observation.isBlank()){
                        evidence.add("Ollama observation: "+observation);
                    }
                }
            }
        }
        double riskScore=totalWeight>0?weightedRisk/totalWeight:0;
        VarificationStatus status;
        if(checksPerformed==0){
            status=VarificationStatus.NOT_AVAILABLE;
        }else if(!issues.isEmpty()){
            status=VarificationStatus.FAILED;
        }else if(checksPassed<checksPerformed){
            status=VarificationStatus.WARNING;
        }else{
            status=VarificationStatus.PASSED;
        }
        double confidence=checksPerformed==0?0:(double)checksPassed/checksPerformed;
        String message;
        if(status==VarificationStatus.PASSED){
            message="All available cross-field checks passed";
        }else if(status==VarificationStatus.NOT_AVAILABLE){
            message="No cross-field checks were available";
        }else if(!issues.isEmpty()){
            message=String.join("; ",issues);
        }else{
            message="Cross-field verification completed with warnings";
        }
        VarificationResult combinedResult=VarificationResult.builder()
                .varifierName("Combined Cross Field Verification")
                .varificationStatus(status)
                .confidence(confidence)
                .riskScore(round(riskScore))
                .message(message)
                .varificationType(com.forensiq.identity_scanning.varification.enumeration.VarificationType.CROSS_FIELD)
                .build();
        return CrossFieldEvaluation.builder()
                .result(combinedResult)
                .status(status)
                .riskScore(round(riskScore))
                .checksPerformed(checksPerformed)
                .checksPassed(checksPassed)
                .issues(issues)
                .warnings(warnings)
                .evidence(evidence)
                .build();
    }
    private void addMessage(String message,List<String> target){
        if(message!=null&&!message.isBlank()&&!target.contains(message)){
            target.add(message);
        }
    }
    private double normalize(Double value){
        if(value==null){
            return 0;
        }
        return Math.max(0,Math.min(100,value));
    }
    private double round(double value){
        return Math.round(value*100.0)/100.0;
    }
}