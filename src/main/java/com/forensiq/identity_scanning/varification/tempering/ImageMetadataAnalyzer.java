package com.forensiq.identity_scanning.varification.tempering;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.forensiq.identity_scanning.varification.tempering.dto.MetadataAnalysisResult;
@Component
public class ImageMetadataAnalyzer {
    public MetadataAnalysisResult analyze(File imageFile){
        if(imageFile==null||!imageFile.exists()||!imageFile.isFile()){
            return MetadataAnalysisResult.builder()
                    .analyzed(false)
                    .suspicious(false)
                    .suspicionScore(0)
                    .findings(List.of())
                    .message("Image file does not exist")
                    .build();
        }
        try{
            Metadata metadata=ImageMetadataReader.readMetadata(imageFile);
            List<String> findings=new ArrayList<>();
            double suspicionScore=0;
            boolean editingSoftwareDetected=false;
            for(Directory directory:metadata.getDirectories()){
                for(Tag tag:directory.getTags()){
                    String tagName=tag.getTagName();
                    String description=tag.getDescription();
                    if(tagName==null||description==null||description.isBlank()){
                        continue;
                    }
                    String lowerTag=tagName.toLowerCase();
                    String lowerDescription=description.toLowerCase();
                    if(lowerTag.contains("software")||lowerTag.contains("creator tool")){
                        findings.add("Software metadata: "+description);
                        if(isEditingSoftware(lowerDescription)&&!editingSoftwareDetected){
                            findings.add("Possible image editing software detected: "+description);
                            suspicionScore+=30;
                            editingSoftwareDetected=true;
                        }
                    }
                    if(containsEditingSoftware(lowerDescription)&&!editingSoftwareDetected){
                        findings.add("Possible editing tool reference: "+description);
                        suspicionScore+=20;
                        editingSoftwareDetected=true;
                    }
                }
                if(directory.hasErrors()){
                    for(String error:directory.getErrors()){
                        findings.add("Metadata error: "+error);
                        suspicionScore+=5;
                    }
                }
            }
            suspicionScore=Math.min(suspicionScore,100);
            boolean suspicious=suspicionScore>=50;
            String message;
            if(suspicious){
                message="Potential metadata anomalies detected";
            }else if(findings.isEmpty()){
                message="No significant metadata indicators found";
            }else{
                message="Metadata analyzed with minor indicators";
            }
            return MetadataAnalysisResult.builder()
                    .analyzed(true)
                    .suspicious(suspicious)
                    .suspicionScore(suspicionScore)
                    .findings(findings)
                    .message(message)
                    .build();
        }catch(Exception exception){
            return MetadataAnalysisResult.builder()
                    .analyzed(false)
                    .suspicious(false)
                    .suspicionScore(0)
                    .findings(List.of())
                    .message("Metadata analysis failed: "+exception.getMessage())
                    .build();
        }
    }
    private boolean isEditingSoftware(String software){
        return software.contains("photoshop")
                ||software.contains("gimp")
                ||software.contains("lightroom")
                ||software.contains("canva")
                ||software.contains("paint.net")
                ||software.contains("pixelmator");
    }
    private boolean containsEditingSoftware(String value){
        return isEditingSoftware(value);
    }
}