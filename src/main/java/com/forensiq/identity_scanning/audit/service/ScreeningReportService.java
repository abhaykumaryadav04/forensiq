package com.forensiq.identity_scanning.audit.service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import com.forensiq.identity_scanning.audit.entity.ScreeningAudit;
@Service
public class ScreeningReportService {
    public byte[] generate(ScreeningAudit audit) throws IOException{
        try(PDDocument document=new PDDocument()){
            PDPage page=new PDPage(PDRectangle.A4);
            document.addPage(page);
            try(PDPageContentStream stream=new PDPageContentStream(document,page)){
                float y=770;
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD),18);
                stream.newLineAtOffset(50,y);
                stream.showText("ForensiQ Document Screening Report");
                stream.endText();
                y-=35;
                y=writeLine(stream,"Document ID: "+audit.getDocumentId(),y,false);
                y=writeLine(stream,"Request ID: "+audit.getRequestId(),y,false);
                y=writeLine(stream,"Document Type: "+audit.getDocumentType(),y,false);
                y=writeLine(stream,"Document Hash: "+audit.getDocumentHash(),y,false);
                y=writeLine(stream,"Risk Score: "+audit.getRiskScore(),y,false);
                y=writeLine(stream,"Verdict: "+audit.getVerdict(),y,true);
                y=writeLine(stream,"Decision Confidence: "+audit.getDecisionConfidence(),y,false);
                y=writeLine(stream,"Checks Performed: "+audit.getChecksPerformed(),y,false);
                y=writeLine(stream,"Checks Available: "+audit.getChecksAvailable(),y,false);
                y-=10;
                y=writeLine(stream,"Reasons:",y,true);
                y=writeText(stream,audit.getReasons(),y);
                y-=10;
                y=writeLine(stream,"Warnings:",y,true);
                y=writeText(stream,audit.getWarnings(),y);
                y-=10;
                y=writeLine(stream,"Created At: "+audit.getCreatedAt(),y,false);
                y=writeLine(stream,"Audit Hash: "+audit.getAuditHash(),y,false);
                y=writeLine(stream,"Previous Audit Hash: "+(audit.getPreviousAuditHash()!=null?audit.getPreviousAuditHash():"NONE"),y,false);
                y-=15;
                writeLine(stream,"This report is an automated screening result and should be reviewed by an authorized investigator where required.",y,false);
            }
            ByteArrayOutputStream output=new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        }
    }
    private float writeLine(PDPageContentStream stream,String text,float y,boolean bold) throws IOException{
        stream.beginText();
        stream.setFont(new PDType1Font(bold?Standard14Fonts.FontName.HELVETICA_BOLD:Standard14Fonts.FontName.HELVETICA),10);
        stream.newLineAtOffset(50,y);
        stream.showText(safeText(text));
        stream.endText();
        return y-18;
    }
    private float writeText(PDPageContentStream stream,String text,float y) throws IOException{
        if(text==null||text.isBlank()){
            return writeLine(stream,"None",y,false);
        }
        String[] parts=text.split("\\|");
        for(String part:parts){
            String value=part.trim();
            if(!value.isBlank()){
                y=writeLine(stream,"- "+value,y,false);
            }
        }
        return y;
    }
    private String safeText(String value){
        if(value==null){
            return "";
        }
        return value.replaceAll("[^\\x20-\\x7E]"," ");
    }
}