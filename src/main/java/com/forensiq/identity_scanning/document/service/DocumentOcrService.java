package com.forensiq.identity_scanning.document.service;
import java.nio.file.Path;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.document.entity.DocumentPage;
import com.forensiq.identity_scanning.document.entity.DocumentStatus;
import com.forensiq.identity_scanning.document.entity.PageStatus;
import com.forensiq.identity_scanning.document.repo.DocumentPageRepo;
import com.forensiq.identity_scanning.document.repo.DocumentRepo;
import com.forensiq.identity_scanning.ocr.dto.OcrResponse;
import com.forensiq.identity_scanning.ocr.dto.OcrWord;
import com.forensiq.identity_scanning.ocr.entity.OcrWordEntity;
import com.forensiq.identity_scanning.ocr.repo.OcrWordRepo;
import com.forensiq.identity_scanning.ocr.service.OcrService;
import jakarta.transaction.Transactional;
@Service
public class DocumentOcrService {
    @Autowired
    private OcrService ocrService;
    @Autowired
    private DocumentPageRepo documentPageRepo;
    @Autowired
    private OcrWordRepo ocrWordRepo;
    @Autowired
    private DocumentRepo documentRepo;
    @Transactional
    public void documentService(Long documentId) throws Exception{
        Document document=documentRepo.findById(documentId).orElseThrow(()->new RuntimeException("Document cannot be found"));
        document.setStatus(DocumentStatus.PROCESSING);
        documentRepo.save(document);
        List<DocumentPage> pages=documentPageRepo.findByDocumentIdOrderByPageNumber(documentId);
        if(pages.isEmpty()){
            document.setStatus(DocumentStatus.FAILED);
            documentRepo.save(document);
            throw new RuntimeException("No document pages found");
        }
        for(DocumentPage page:pages){
            processPage(page);
        }
        document.setStatus(DocumentStatus.COMPLETED);
        documentRepo.save(document);
    }
    private void processPage(DocumentPage page) throws Exception{
        page.setStatus(PageStatus.OCR_PROCESSING);
        documentPageRepo.save(page);
        OcrResponse ocrResponse=ocrService.ocrResponse(Path.of(page.getImagepath()));
        if(ocrResponse==null){
            page.setStatus(PageStatus.FAILED);
            documentPageRepo.save(page);
            throw new RuntimeException("OCR returned null response");
        }
        page.setOcrText(ocrResponse.getText());
        page.setOcrConfidence(ocrResponse.getConfidence());
        if(ocrResponse.getOcrWords()!=null){
            for(OcrWord word:ocrResponse.getOcrWords()){
                OcrWordEntity entity=OcrWordEntity.builder()
                        .confidence(word.getConfidence())
                        .documentPage(page)
                        .height(word.getHeight())
                        .text(word.getText())
                        .width(word.getWidth())
                        .x(word.getX())
                        .build();
                ocrWordRepo.save(entity);
            }
        }
        page.setStatus(PageStatus.OCR_COMPLETED);
        page.setOcrProcessingTimeMs(ocrResponse.getProcessingTimeMs());
        documentPageRepo.save(page);
    }
}