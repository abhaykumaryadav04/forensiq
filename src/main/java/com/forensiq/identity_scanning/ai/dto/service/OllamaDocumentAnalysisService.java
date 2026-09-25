package com.forensiq.identity_scanning.ai.dto.service;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import tools.jackson.databind.ObjectMapper;
import com.forensiq.identity_scanning.ai.dto.OllamaDocumentAnalysisResult;
@Service
public class OllamaDocumentAnalysisService {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final double temperature;
    public OllamaDocumentAnalysisService(OllamaChatModel ollamaChatModel,ObjectMapper objectMapper,@Value("${app.ollama.model}") String model,@Value("${app.ollama.temperature:}") double temperature){
        this.chatClient=ChatClient.create(ollamaChatModel);
        this.objectMapper=objectMapper;
        this.model=model;
        this.temperature=temperature;
    }
    public OllamaDocumentAnalysisResult analyze(Path imagePath,String ocrText){
         long start=System.currentTimeMillis();
    System.out.println("=== OLLAMA DOCUMENT ANALYSIS START ===");
    System.out.println("Model: "+model);
    System.out.println("Image: "+imagePath);
    System.out.println("Image exists: "+Files.exists(imagePath));
        if(imagePath==null){
            throw new IllegalArgumentException("Image path cannot be null");
        }
        if(!Files.exists(imagePath)){
            throw new IllegalArgumentException("Image does not exist: "+imagePath);
        }
        String prompt=buildPrompt(ocrText);
        MimeType mimeType=resolveMimeType(imagePath);
    String response=chatClient.prompt()
        .options(OllamaChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .disableThinking()
                .format("json"))
        .user(user->user
                .text(prompt)
                .media(mimeType,new FileSystemResource(imagePath)))
        .call()
        .content();
        if(response==null||response.isBlank()){
            throw new IllegalStateException("Ollama returned an empty analysis");
        }
        return parseResponse(response);
    }
    public String getModel(){
        return model;
    }
    private String buildPrompt(String ocrText){
        String safeOcr=ocrText==null?"":ocrText.trim();
        if(safeOcr.length()>12000){
            safeOcr=safeOcr.substring(0,12000);
        }
        return """
                Analyze the supplied identity document image for ForensiQ.
                Supported document types are PASSPORT, DRIVING_LICENSE, ID_CARD, GOVERNMENT_ID and UNKNOWN.
                Identify the most likely document type.
                Check whether a document-like object is visible.
                Examine the visual layout, text placement, alignment, typography, photo area, borders and obvious digital editing artifacts.
                Do not claim legal authenticity.
                Do not claim that a document is conclusively forged.
                Return ONLY valid JSON.
                Use exactly these fields:
                {
                  "documentType":"PASSPORT",
                  "confidence":0,
                  "documentDetected":true,
                  "observations":[],
                  "suspiciousAreas":[],
                  "summary":""
                }
                OCR text is additional context and may contain mistakes:
                %s
                """.formatted(safeOcr);
    }
    private OllamaDocumentAnalysisResult parseResponse(String response){
        String json=cleanJson(response);
        try{
            OllamaDocumentAnalysisResult result=objectMapper.readValue(json,OllamaDocumentAnalysisResult.class);
            return normalizeResult(result);
        }catch(Exception exception){
            throw new IllegalStateException("Unable to parse Ollama response: "+exception.getMessage());
        }
    }
    private String cleanJson(String response){
        String cleaned=response.trim();
        if(cleaned.startsWith("```")){
            int firstNewLine=cleaned.indexOf('\n');
            int lastFence=cleaned.lastIndexOf("```");
            if(firstNewLine>0&&lastFence>firstNewLine){
                cleaned=cleaned.substring(firstNewLine+1,lastFence).trim();
            }
        }
        int firstBrace=cleaned.indexOf('{');
        int lastBrace=cleaned.lastIndexOf('}');
        if(firstBrace>=0&&lastBrace>firstBrace){
            cleaned=cleaned.substring(firstBrace,lastBrace+1);
        }
        return cleaned;
    }
    private OllamaDocumentAnalysisResult normalizeResult(OllamaDocumentAnalysisResult result){
        if(result==null){
            throw new IllegalStateException("Ollama response is null");
        }
        if(result.getDocumentType()==null||result.getDocumentType().isBlank()){
            result.setDocumentType("UNKNOWN");
        }
        result.setDocumentType(result.getDocumentType().trim().toUpperCase());
        result.setConfidence(Math.max(0,Math.min(100,result.getConfidence())));
        if(result.getObservations()==null){
            result.setObservations(java.util.List.of());
        }
        if(result.getSuspiciousAreas()==null){
            result.setSuspiciousAreas(java.util.List.of());
        }
        if(result.getSummary()==null){
            result.setSummary("");
        }
        return result;
    }
    private MimeType resolveMimeType(Path imagePath){
        String fileName=imagePath.getFileName().toString().toLowerCase();
        if(fileName.endsWith(".png")){
            return MimeTypeUtils.IMAGE_PNG;
        }
        return MimeTypeUtils.IMAGE_JPEG;
    }
}