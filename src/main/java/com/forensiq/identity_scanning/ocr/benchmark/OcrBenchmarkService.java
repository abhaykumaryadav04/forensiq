package com.forensiq.identity_scanning.ocr.benchmark;

import com.forensiq.identity_scanning.ocr.dto.OcrBenchmarkResponse;
import com.forensiq.identity_scanning.ocr.dto.OcrResponse;
import com.forensiq.identity_scanning.ocr.service.OcrService;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class OcrBenchmarkService {

    private final OcrService ocrService;
    private final OcrMetrics metricsService;

    public OcrBenchmarkService(
            OcrService ocrService,
            OcrMetrics metricsService) {

        this.ocrService = ocrService;
        this.metricsService = metricsService;
    }

    public OcrBenchmarkResponse benchmark(
            Path imagePath,
            String groundTruth)
            throws Exception {

        // RAW OCR
        OcrResponse raw =
                ocrService.rawOcr(imagePath);

        // PREPROCESSED OCR
        OcrResponse processed =
                ocrService.ocrResponse(imagePath);

        double rawCER =
                metricsService.calculateCER(
                        groundTruth,
                        raw.getText()
                );

        double processedCER =
                metricsService.calculateCER(
                        groundTruth,
                        processed.getText()
                );

        double rawWER =
                metricsService.calculateWER(
                        groundTruth,
                        raw.getText()
                );

        double processedWER =
                metricsService.calculateWER(
                        groundTruth,
                        processed.getText()
                );

        return OcrBenchmarkResponse.builder()
                .imageName(
                        imagePath.getFileName().toString()
                )
                .rawCER(rawCER)
                .processedCER(processedCER)
                .rawWER(rawWER)
                .processedWER(processedWER)
                .rawConfidence(
                        raw.getConfidence()
                )
                .processedConfidence(
                        processed.getConfidence()
                )
                .rawProcessingTimeMs(
                        raw.getProcessingTimeMs()
                )
                .processedProcessingTimeMs(
                        processed.getProcessingTimeMs()
                )
                .build();
    }
}