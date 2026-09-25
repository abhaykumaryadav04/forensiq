package com.forensiq.identity_scanning.classification.ml;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import com.forensiq.identity_scanning.ai.dto.OllamaDocumentAnalysisResult;
import com.forensiq.identity_scanning.ai.dto.service.OllamaDocumentAnalysisService;
import com.forensiq.identity_scanning.classification.dto.ClassificationPrediction;
import com.forensiq.identity_scanning.document.entity.DocumentType;
@Component
public class ClassificationPredictorMl {
    private final OllamaDocumentAnalysisService ollamaDocumentAnalysisService;
    public ClassificationPredictorMl(OllamaDocumentAnalysisService ollamaDocumentAnalysisService){
        this.ollamaDocumentAnalysisService=ollamaDocumentAnalysisService;
    }
    public List<ClassificationPrediction> predict(Path imagePath) throws Exception{
        OllamaDocumentAnalysisResult result=ollamaDocumentAnalysisService.analyze(imagePath,"");
        DocumentType predictedType=mapDocumentType(result.getDocumentType());
        double confidence=result.getConfidence();
        List<ClassificationPrediction> predictions=new ArrayList<>();
        for(DocumentType documentType:List.of(
                DocumentType.PASSPORT,
                DocumentType.DRIVING_LICENSE,
                DocumentType.ID_CARD,
                DocumentType.GOVERNMENT_ID,
                DocumentType.UNKOWN)){
            predictions.add(ClassificationPrediction.builder()
                    .documentType(documentType)
                    .probability(documentType==predictedType?confidence:0.0)
                    .build());
        }
        return predictions;
    }
    public String getModel(){
        return ollamaDocumentAnalysisService.getModel();
    }
    private DocumentType mapDocumentType(String value){
        if(value==null){
            return DocumentType.UNKOWN;
        }
        String normalized=value.trim().toUpperCase();
        return switch(normalized){
            case "PASSPORT" -> DocumentType.PASSPORT;
            case "DRIVING_LICENSE","DRIVER_LICENSE","DRIVING LICENCE","DRIVERING_LICENSE" -> DocumentType.DRIVING_LICENSE;
            case "ID_CARD","ID CARD","AADHAAR","AADHAAR_CARD" -> DocumentType.ID_CARD;
            case "GOVERNMENT_ID","GOVERNMENT ID","PAN","PAN_CARD","VOTER_ID" -> DocumentType.GOVERNMENT_ID;
            default -> DocumentType.UNKOWN;
        };
    }
}