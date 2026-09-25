package com.forensiq.identity_scanning.classification.controller;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.forensiq.identity_scanning.classification.dto.ClassificationResponse;
import com.forensiq.identity_scanning.classification.service.ClassificationService;

@RestController
@RequestMapping("/api/classification/v1")
public class ClassificationController {
    @Autowired
    private ClassificationService classificationService;
    @PostMapping( value = "/classify",consumes =MediaType.MULTIPART_FORM_DATA_VALUE)
    public ClassificationResponse classify(@RequestParam("file")MultipartFile file) throws Exception{
        if(file.isEmpty()){
            throw new RuntimeException("FIle is Empty");
        }
        Path tempFile=Files.createTempFile("Classification-",".temp");
          try {
             file.transferTo(tempFile);
            return classificationService.clasify( tempFile);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
    @PostMapping("/document-page/{pageId}")
     public ClassificationResponse classifyDocumentPage(@PathVariable Long pageId)throws Exception {

    return classificationService.documentPageClassify(pageId);
}
}
