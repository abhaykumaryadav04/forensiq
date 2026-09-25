package com.forensiq.identity_scanning.common.util;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.web.multipart.MultipartFile;

public final class  FileHashUtil {
    private FileHashUtil(){

    }
    public static String sha256(MultipartFile file) throws NoSuchAlgorithmException, IOException{
   MessageDigest messageDigest = MessageDigest.getInstance("SHA-256") ;
   byte[] hash=messageDigest.digest(file.getBytes());
   StringBuilder sb=new StringBuilder();
   for(byte b:hash){
    sb.append(String.format("%02x", b));
   }
   return sb.toString();

    }

}
