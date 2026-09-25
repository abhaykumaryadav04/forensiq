package com.forensiq.identity_scanning.ai.dto.service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import tools.jackson.databind.ObjectMapper;
import com.forensiq.identity_scanning.ai.dto.OllamaTamperingAnalysisResult;
import com.forensiq.identity_scanning.information.dto.ExtractionResponse;
@Service
public class OllamaTamperingAnalysisService {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final double temperature;
    public OllamaTamperingAnalysisService(ChatClient.Builder chatClientBuilder,ObjectMapper objectMapper,@Value("${app.ollama.model:gemma3:4b}") String model,@Value("${app.ollama.temperature:0}") double temperature){
        this.chatClient=chatClientBuilder.build();
        this.objectMapper=objectMapper;
        this.model=model;
        this.temperature=temperature;
    }
    public OllamaTamperingAnalysisResult analyze(Path imagePath,String ocrText,ExtractionResponse extractionResponse){
        if(imagePath==null){
            throw new IllegalArgumentException("Image path cannot be null");
        }
        if(!Files.exists(imagePath)){
            throw new IllegalArgumentException("Image does not exist: "+imagePath);
        }
        String prompt=buildPrompt(ocrText,extractionResponse);
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
            throw new IllegalStateException("Ollama returned an empty tampering analysis");
        }
        return parseResponse(response);
    }
    public String getModel(){
        return model;
    }
    private String buildPrompt(String ocrText,ExtractionResponse extractionResponse){
        String safeOcr=ocrText==null?"":ocrText.trim();
        if(safeOcr.length()>10000){
            safeOcr=safeOcr.substring(0,10000);
        }
        String extractedFields=buildExtractedFields(extractionResponse);
        return """
                You are the visual analysis component of the ForensiQ identity-document screening system.
                Analyze the supplied identity document image for possible digital manipulation.
                Look for evidence such as text replacement, copy-move duplication, splicing or inserted content, inconsistent typography, abnormal alignment, suspicious image regions, inconsistent rendering, unnatural edges, inconsistent background texture and other visible editing artifacts.
                Do not claim legal authenticity.
                Do not claim that a document is conclusively forged.
                Return only valid JSON with exactly these fields:
                suspicious: boolean
                tamperingScore: number from 0 to 100
                attackType: one of TEXT_REPLACEMENT, COPY_MOVE, SPLICING, OTHER, UNKNOWN
                attackConfidence: number from 0 to 100
                suspiciousFields: array of field names that appear visually suspicious
                observations: array of concise observations
                reasoning: concise explanation of the evidence
                The tamperingScore represents visual suspicion, not a calibrated probability.
                OCR may contain mistakes and must not be treated as ground truth.
                OCR text:
                %s
                Extracted fields:
                %s
                """.formatted(safeOcr,extractedFields);
    }
    private String buildExtractedFields(ExtractionResponse extractionResponse){
        if(extractionResponse==null||extractionResponse.getFields()==null||extractionResponse.getFields().isEmpty()){
            return "No extracted fields available";
        }
        StringBuilder builder=new StringBuilder();
        extractionResponse.getFields().forEach(field->{
            if(field!=null&&field.getExtractedFieldType()!=null&&field.getValue()!=null){
                builder.append(field.getExtractedFieldType())
                        .append("=")
                        .append(field.getValue())
                        .append("\n");
            }
        });
        return builder.length()==0?"No extracted fields available":builder.toString();
    }
    private OllamaTamperingAnalysisResult parseResponse(String response){
        String json=cleanJson(response);
        try{
            OllamaTamperingAnalysisResult result=objectMapper.readValue(json,OllamaTamperingAnalysisResult.class);
            return normalize(result);
        }catch(Exception exception){
            throw new IllegalStateException("Unable to parse Ollama tampering response: "+exception.getMessage());
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
    private OllamaTamperingAnalysisResult normalize(OllamaTamperingAnalysisResult result){
        if(result==null){
            throw new IllegalStateException("Ollama tampering response is null");
        }
        result.setTamperingScore(clamp(result.getTamperingScore(),0,100));
        result.setAttackConfidence(clamp(result.getAttackConfidence(),0,100));
        if(result.getAttackType()==null||result.getAttackType().isBlank()){
            result.setAttackType("UNKNOWN");
        }else{
            result.setAttackType(result.getAttackType().trim().toUpperCase());
        }
        if(result.getSuspiciousFields()==null){
            result.setSuspiciousFields(List.of());
        }
        if(result.getObservations()==null){
            result.setObservations(List.of());
        }
        result.setSuspicious(result.isSuspicious()||result.getTamperingScore()>=50);
        return result;
    }
    private double clamp(double value,double min,double max){
        return Math.max(min,Math.min(max,value));
    }
    private MimeType resolveMimeType(Path imagePath){
        String name=imagePath.getFileName().toString().toLowerCase();
        if(name.endsWith(".png")){
            return MimeTypeUtils.IMAGE_PNG;
        }
        return MimeTypeUtils.IMAGE_JPEG;
    }
}