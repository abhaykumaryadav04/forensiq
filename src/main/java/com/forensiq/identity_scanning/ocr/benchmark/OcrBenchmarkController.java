package com.forensiq.identity_scanning.ocr.benchmark;


import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.forensiq.identity_scanning.ocr.dto.OcrBenchmarkResponse;

@RestController
@RequestMapping("/api/v1/ocr")
public class OcrBenchmarkController {

    private final OcrBenchmarkService benchmarkService;

    public OcrBenchmarkController(
            OcrBenchmarkService benchmarkService) {

        this.benchmarkService =
                benchmarkService;
    }

    @PostMapping("/benchmark")
    public OcrBenchmarkResponse benchmark(
            @RequestParam("file")
            MultipartFile file,

            @RequestParam("groundTruth")
            String groundTruth)
            throws Exception {

        Path tempFile =
                Files.createTempFile(
                        "ocr-test-",
                        ".png"
                );

        try {

            file.transferTo(tempFile);

            return benchmarkService.benchmark(
                    tempFile,
                    groundTruth
            );

        } finally {

            Files.deleteIfExists(tempFile);
        }
    }
}