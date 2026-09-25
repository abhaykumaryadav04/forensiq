package com.forensiq.identity_scanning.classification.service;

import java.nio.file.Path;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forensiq.identity_scanning.classification.dto.ClassificationPrediction;
import com.forensiq.identity_scanning.classification.dto.ClassificationResponse;
import com.forensiq.identity_scanning.classification.entity.DocumentClassification;
import com.forensiq.identity_scanning.classification.ml.ClassificationPredictorMl;
import com.forensiq.identity_scanning.classification.repo.DocumentClassificationRepo;
import com.forensiq.identity_scanning.document.entity.DocumentPage;
import com.forensiq.identity_scanning.document.entity.DocumentType;
import com.forensiq.identity_scanning.document.repo.DocumentPageRepo;

@Service
public class ClassificationService {
    @Autowired
    private ClassificationPredictorMl predictorMl;
    @Autowired
    private DocumentPageRepo documentPageRepo;
    @Autowired
    private DocumentClassificationRepo documentClassificationRepo;

    public ClassificationResponse clasify(Path imagePath) throws Exception{
           long start =System.currentTimeMillis();
List<ClassificationPrediction> list=predictorMl.predict(imagePath);
ClassificationPrediction bestPrediction = list.stream()
        .max(Comparator.comparingDouble(ClassificationPrediction::getProbability))
        .orElseGet(() -> ClassificationPrediction.builder()
                .probability(0.0)
                .documentType(DocumentType.UNKOWN)
                .build());
     
long end=System.currentTimeMillis();
        return ClassificationResponse.builder()
                                      .confidence(bestPrediction.getProbability())
                                      .documentType(bestPrediction.getDocumentType())
                                      .modelVersioString("No-Model-define")
                                      .processingTimeMs(end-start)
                                      .predictions(list)
                                      .modelVersioString(predictorMl.getModel())
                                      .build();
    }
   public ClassificationResponse documentPageClassify(Long documentPageId) throws Exception {

    DocumentPage page = documentPageRepo
            .findById(documentPageId)
            .orElseThrow(() ->
                    new RuntimeException("Page cannot be found"));

    Path pathImage = Path.of(page.getImagepath());

    ClassificationResponse response = clasify(pathImage);
 DocumentClassification last = documentClassificationRepo
                .findByDocumentPageId(documentPageId)
                .orElseGet(() -> DocumentClassification.builder()
                        .confidence(response.getConfidence())
                        .documentPage(page)
                        .documentType(response.getDocumentType())
                        .modelVersion(response.getModelVersioString())
                        .processTime(response.getProcessingTimeMs())
                        .build());
            

    documentClassificationRepo.save(last);

    return response;
}
}
