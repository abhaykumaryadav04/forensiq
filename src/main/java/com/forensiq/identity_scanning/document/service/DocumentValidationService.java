package com.forensiq.identity_scanning.document.service;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
@Service
public class DocumentValidationService {
    private static final Set<String> ALLOWED=Set.of("image/png","image/jpeg","application/pdf");
    private static final Set<String> CAMERA_ALLOWED=Set.of("image/png","image/jpeg");
    private static final long MAX_FILE_SIZE=10L*1024*1024;
    public void validation(MultipartFile file){
        if(file==null||file.isEmpty()){
            throw new IllegalArgumentException("File is Empty");
        }
        if(file.getSize()>MAX_FILE_SIZE){
            throw new IllegalArgumentException("Maximum Size Only can be 10MB");
        }
        String contentType=file.getContentType();
        if(contentType==null||!ALLOWED.contains(contentType)){
            throw new IllegalArgumentException("content type not allowed");
        }
    }
    public void validateCameraImage(MultipartFile file){
        if(file==null||file.isEmpty()){
            throw new IllegalArgumentException("Camera image is empty");
        }
        if(file.getSize()>MAX_FILE_SIZE){
            throw new IllegalArgumentException("Maximum camera image size is 10MB");
        }
        String contentType=file.getContentType();
        if(contentType==null||!CAMERA_ALLOWED.contains(contentType)){
            throw new IllegalArgumentException("Camera accepts only PNG or JPEG images");
        }
    }
}