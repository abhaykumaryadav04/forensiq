package com.forensiq.identity_scanning.information.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.forensiq.identity_scanning.document.entity.DocumentType;
import com.forensiq.identity_scanning.information.dto.ExtractionResponse;
import com.forensiq.identity_scanning.information.extractor.InformationExtractor;
import com.forensiq.identity_scanning.ocr.dto.OcrWord;

@Service
public class InformationExtractionService {
    private final List<InformationExtractor> extractors;

    public InformationExtractionService(List<InformationExtractor> extractors){
        this.extractors=extractors;
    }
    public ExtractionResponse extract(DocumentType documentType,String ocrText,List<OcrWord> ocrWords) throws Exception{
          if (ocrText == null || ocrText.isBlank()) {
            throw new IllegalArgumentException("OCR text cannot be empty");
        }

          if (documentType == null) {
            throw new IllegalArgumentException("Document type cannot be null" );
        }

        InformationExtractor extractor=extractors.stream().filter(item->item.getSupportedDocumentType()==documentType)
                                                  .findFirst().orElseThrow(()->new Exception("No extractor found"));
           return extractor.extract(ocrText,ocrWords);                                       
    }

}
