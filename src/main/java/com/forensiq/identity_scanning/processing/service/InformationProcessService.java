package com.forensiq.identity_scanning.processing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.document.entity.DocumentType;
import com.forensiq.identity_scanning.information.dto.ExtractionResponse;
import com.forensiq.identity_scanning.information.service.ExtractedFieldPersistenceService;
import com.forensiq.identity_scanning.information.service.InformationExtractionService;
import com.forensiq.identity_scanning.ocr.dto.OcrWord;
import com.forensiq.identity_scanning.processing.dto.InformationProcessingDto;

@Service
public class InformationProcessService {
    @Autowired
    private InformationExtractionService informationExtractionService;
    @Autowired
    private ExtractedFieldPersistenceService extractedFieldPersistenceService;

    public InformationProcessingDto process(
            DocumentType documentType,
            String ocrText,
            Document document, List<OcrWord> ocrWords) {

        if (documentType == null) {
            return InformationProcessingDto.builder()
                    .successful(false)
                    .message("Document type cannot be null")
                    .extractionResponse(null)
                    .build();
        }

        if (ocrText == null || ocrText.isBlank()) {
            return InformationProcessingDto.builder()
                    .successful(false)
                    .message("OCR text is empty")
                    .extractionResponse(null)
                    .build();
        }

        if (document == null) {
            return InformationProcessingDto.builder()
                    .successful(false)
                    .message("Document cannot be null")
                    .extractionResponse(null)
                    .build();
        }

        if (document.getId() == null) {
            return InformationProcessingDto.builder()
                    .successful(false)
                    .message("Document must be saved before information extraction")
                    .extractionResponse(null)
                    .build();
        }

        try {

            ExtractionResponse response =
                    informationExtractionService.extract(
                            documentType,
                            ocrText,ocrWords
                    );

            if (response == null) {
                return InformationProcessingDto.builder()
                        .successful(false)
                        .message("Information extraction returned null")
                        .extractionResponse(null)
                        .build();
            }

            extractedFieldPersistenceService.saveExtractedFields(
                    response,
                    document
            );

            return InformationProcessingDto.builder()
                    .extractionResponse(response)
                    .message("Information extraction successful")
                    .successful(true)
                    .build();

        } catch (Exception e) {

            e.printStackTrace();

            return InformationProcessingDto.builder()
                    .successful(false)
                    .message(
                            "Information extraction failed: "
                                    + e.getClass().getSimpleName()
                                    + " - "
                                    + e.getMessage()
                    )
                    .extractionResponse(null)
                    .build();
        }
    }

}
