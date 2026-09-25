package com.forensiq.identity_scanning.result.service;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import com.forensiq.identity_scanning.document.entity.Document;
import com.forensiq.identity_scanning.processing.dto.FinalVerdictResponse;
import com.forensiq.identity_scanning.result.model.ScreeningResult;
import com.forensiq.identity_scanning.result.repo.ScreeningResultRepo;
@Service
public class ScreeningResultCacheService {
    private final ScreeningResultRepo screeningResultRepo;
    private final ObjectMapper objectMapper;
    public ScreeningResultCacheService(ScreeningResultRepo screeningResultRepo,ObjectMapper objectMapper){
        this.screeningResultRepo=screeningResultRepo;
        this.objectMapper=objectMapper;
    }
    public FinalVerdictResponse findCachedResult(Document document){
        if(document==null||document.getId()==null){
            return null;
        }
        return screeningResultRepo.findByDocumentId(document.getId())
                .map(this::deserialize)
                .map(this::markCached)
                .orElse(null);
    }
    public FinalVerdictResponse findCachedResultByHash(String documentHash){
        if(documentHash==null||documentHash.isBlank()){
            return null;
        }
        return screeningResultRepo.findByDocumentHash(documentHash)
                .map(this::deserialize)
                .map(this::markCached)
                .orElse(null);
    }
    public ScreeningResult save(Document document,FinalVerdictResponse response){
        try{
            ScreeningResult result=screeningResultRepo.findByDocumentId(document.getId()).orElse(null);
            if(result==null){
                result=ScreeningResult.builder()
                        .document(document)
                        .documentHash(document.getFileHash())
                        .createdAt(LocalDateTime.now())
                        .build();
            }
            result.setDocumentHash(document.getFileHash());
            result.setRiskScore(response.getRiskScore());
            result.setVerdict(response.getVerdict());
            result.setDecisionConfidence(response.getDecisionConfidence());
            result.setChecksPerformed(response.getChecksPerformed());
            result.setChecksAvailable(response.getChecksAvailable());
            result.setResultJson(objectMapper.writeValueAsString(response));
            result.setUpdatedAt(LocalDateTime.now());
            if(result.getCreatedAt()==null){
                result.setCreatedAt(LocalDateTime.now());
            }
            return screeningResultRepo.save(result);
        }catch(Exception exception){
            throw new IllegalStateException("Unable to save screening result",exception);
        }
    }
    private FinalVerdictResponse deserialize(ScreeningResult result){
        try{
            return objectMapper.readValue(result.getResultJson(),FinalVerdictResponse.class);
        }catch(Exception exception){
            throw new IllegalStateException("Unable to read cached screening result",exception);
        }
    }
    private FinalVerdictResponse markCached(FinalVerdictResponse response){
        response.setCachedResult(true);
        return response;
    }
}