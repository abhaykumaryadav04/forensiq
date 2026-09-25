package com.forensiq.identity_scanning.ocr.config;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import nu.pattern.OpenCV;

@Configuration
public class OpenCVConfig {

    @PostConstruct
    public void loadOpenCv() {
        OpenCV.loadLocally();
    }
}