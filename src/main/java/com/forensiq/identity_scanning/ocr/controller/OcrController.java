package com.forensiq.identity_scanning.ocr.controller;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.forensiq.identity_scanning.ocr.dto.OcrResponse;
import com.forensiq.identity_scanning.ocr.service.OcrService;

@RestController
@RequestMapping("/api/v1/ocr")
public class OcrController {

    @Autowired
    private OcrService ocrService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OcrResponse> process(
            @RequestParam("file") MultipartFile file) throws Exception {

        String originalFileName = file.getOriginalFilename();

        String extension = ".png";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(
                    originalFileName.lastIndexOf(".")
            );
        }

        Path tempFile = Files.createTempFile("ocr-", extension);

        try {
            file.transferTo(tempFile.toFile());

            OcrResponse response = ocrService.ocrResponse(tempFile);

            return ResponseEntity.ok(response);

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}