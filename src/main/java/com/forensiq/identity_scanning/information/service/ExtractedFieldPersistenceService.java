package com.forensiq.identity_scanning.information.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forensiq.identity_scanning.document.entity.Document;

import com.forensiq.identity_scanning.information.dto.ExtractedField;
import com.forensiq.identity_scanning.information.dto.ExtractionResponse;
import com.forensiq.identity_scanning.information.model.ExtractedDocumentField;
import com.forensiq.identity_scanning.information.repo.ExtractedDocumentFieldRepo;


@Service
public class ExtractedFieldPersistenceService {
    @Autowired
    private ExtractedDocumentFieldRepo extractedDocumentFieldRepo;

    public void saveExtractedFields(ExtractionResponse extractionResponse,Document document){
    extractedDocumentFieldRepo.deleteByDocument_Id(document.getId());

        List<ExtractedDocumentField> entities =
                extractionResponse.getFields()
                        .stream()
                        .map(field -> mapToEntity(
                                document,
                                field
                        ))
                        .toList();

        extractedDocumentFieldRepo.saveAll(entities);
    }

    private ExtractedDocumentField mapToEntity( Document document,ExtractedField field) {

        return ExtractedDocumentField.builder()
                .document(document)
                .extractedFieldType( field.getExtractedFieldType())
                .value(field.getValue())
                .confidence(field.getConfidencce())
                .extractedSource(field.getSource())
                .build();
    }

}
