package com.forensiq.identity_scanning.document.service;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Value;

@Service
public class FileStorageService {
  private  final Path path;
  public FileStorageService( @Value("${app.storage.base-path}") String pathString){
    this.path=Paths.get(pathString).toAbsolutePath().normalize();
  }

  public Path store(String fileId,MultipartFile file) throws Exception{
    Path documentDirectory=path.resolve(String.valueOf(fileId));
    Files.createDirectories(documentDirectory);
    Path target=documentDirectory.resolve("original"+getExtension(file.getOriginalFilename()));
    file.transferTo(target);
    return target;
  }
  public String getExtension(String fileName){
  if(fileName==null||!fileName.contains(".")){
    return "";
  }
  return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
  }
}
