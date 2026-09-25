package com.forensiq.identity_scanning.ocr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

@Configuration
public class TesseractConfig {


@Value("${app.tesseract.data-path}")
 private String dataPath;
@Value("${app.tesseract.language:eng}")
private String language;
    @Bean
    public ITesseract tesseract(){
        Tesseract tesseract=new Tesseract();
        tesseract.setDatapath(dataPath);
        tesseract.setLanguage(language);
        return tesseract;
    }
}
