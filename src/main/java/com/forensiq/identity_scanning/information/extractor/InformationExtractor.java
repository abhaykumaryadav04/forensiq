package com.forensiq.identity_scanning.information.extractor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.forensiq.identity_scanning.document.entity.DocumentType;
import com.forensiq.identity_scanning.information.dto.ExtractionResponse;
import com.forensiq.identity_scanning.ocr.dto.OcrWord;

@Component
public interface InformationExtractor {
     DocumentType getSupportedDocumentType();
     ExtractionResponse extract(String ocrText,List<OcrWord> ocrWords);

}
