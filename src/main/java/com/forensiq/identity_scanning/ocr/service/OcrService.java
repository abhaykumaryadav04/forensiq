package com.forensiq.identity_scanning.ocr.service;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.forensiq.identity_scanning.ocr.dto.OcrResponse;
import com.forensiq.identity_scanning.ocr.dto.OcrWord;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class OcrService {
    
    private final ITesseract tesseract;
    @Autowired
    private ImagePreprocessingService imagePreprocessingService;

   public OcrService(ITesseract tesseract) {
        this.tesseract = tesseract;

      tesseract.setLanguage("eng");

tesseract.setPageSegMode(6);

tesseract.setVariable(
        "user_defined_dpi",
        "300"
);
    }
   public OcrResponse ocrResponse(Path imagePath)
        throws Exception {

   
    List<Path> candidates =
            imagePreprocessingService
                    .generateCandidates(imagePath);

    OcrResponse bestResponse = null;

    try {

        for (Path candidate : candidates) {

            OcrResponse response =
                    processSingleImage(candidate);

            if (bestResponse == null ||
                    response.getConfidence()
                            > bestResponse.getConfidence()) {

                bestResponse = response;
            }
        }

        return bestResponse;

    } finally {

        for (Path candidate : candidates) {

            Files.deleteIfExists(candidate);
        }
    }
    }
  private OcrResponse processSingleImage(Path imagePath) throws Exception {
       File image =
            imagePath.toFile();

    String text =
            tesseract.doOCR(image);


    BufferedImage bufferedImage =
            ImageIO.read(image);


    List<OcrWord> words =
            tesseract.getWords(
                    bufferedImage,
                    ITessAPI.TessPageIteratorLevel.RIL_WORD
            )
            .stream()

            .filter(word ->
                    word.getText() != null &&
                    !word.getText().isBlank()
            )

            .map(word -> {

                Rectangle box =
                        word.getBoundingBox();

                return OcrWord.builder()
                        .text(word.getText())
                        .confidence(word.getConfidence())
                        .x(box.x)
                        .y(box.y)
                        .width(box.width)
                        .height(box.height)
                        .build();
            })

            .toList();


    double confidence =
            words.stream()
                    .filter(word ->
                            word.getConfidence() >= 0
                    )
                    .mapToDouble(
                            OcrWord::getConfidence
                    )
                    .average()
                    .orElse(0.0);


    return OcrResponse.builder()
            .text(text)
            .confidence(confidence)
            .ocrWords(words)
            .build();
}
  public OcrResponse rawOcr(Path imagePath)throws TesseractException, IOException {
    long start =System.currentTimeMillis();
    java.io.File image =imagePath.toFile();
    String text =tesseract.doOCR(image);
     BufferedImage bufferedImage = ImageIO.read(image);
    List<OcrWord> words = tesseract.getWords( bufferedImage,ITessAPI.TessPageIteratorLevel.RIL_WORD)
            .stream()
            .map(word -> {
                Rectangle box = word.getBoundingBox();
                return OcrWord.builder()
                        .text(word.getText())
                        .confidence( word.getConfidence())
                        .x(box.x)
                        .y(box.y)
                        .width(box.width)
                        .height(box.height)
                        .build();})
                        .toList();

    double confidence =words.stream() .mapToDouble(
                            OcrWord::getConfidence )
                    .average()
                    .orElse(0.0);

    long end = System.currentTimeMillis();
    return OcrResponse.builder()
            .text(text)
            .confidence(confidence)
            .processingTimeMs(end - start)
            .ocrWords(words)
            .build();
}
}
