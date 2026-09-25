package com.forensiq.identity_scanning.varification.qr;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.forensiq.identity_scanning.varification.qr.dto.DecodedCodeData;
import com.forensiq.identity_scanning.varification.qr.dto.ParsedCodeData;
import com.forensiq.identity_scanning.varification.qr.dto.QrCrossFieldValidationResult;
import com.forensiq.identity_scanning.varification.qr.dto.QrValidationResult;


@Service
public class QrValidationService {
    
    private final QrBarcodeDetector detector;
    private final QrBarcodeDecoder decoder;
    private final QrDataParser parser;
    private final QrDataValidator dataValidator;
    private final QrCrossFieldValidator crossFieldValidator;
    public QrValidationService(QrBarcodeDetector detector,QrBarcodeDecoder decoder,QrDataParser parser,QrDataValidator dataValidator,QrCrossFieldValidator crossFieldValidator){
        this.detector=detector;
        this.decoder=decoder;
        this.parser=parser;
        this.dataValidator=dataValidator;
        this.crossFieldValidator=crossFieldValidator;
    }
    public QrValidationResult validate(BufferedImage image,Map<String,String> ocrFields) throws IllegalAccessException{
        List<String> issues=new ArrayList<>();
        if(image==null){
            issues.add("Image cannot be null");
            return buildFailure(false,false,false,false,null,null,issues);
        }
        boolean detected=detector.detect(image);
        if(!detected){
            issues.add("QR or Barcode cannot be detected");
            return buildFailure(false,false,false,false,null,null,issues);
        }
        DecodedCodeData data=decoder.decode(image);
        if(data==null||!data.isDecoded()){
            issues.add("QR or Barcode could not be decoded");
            return buildFailure(true,false,false,false,data,null,issues);
        }
        ParsedCodeData parsedCodeData=parser.parse(data.getRawData());
        if(parsedCodeData==null||!parsedCodeData.isParsed()){
            issues.add("Unable to parse QR or Barcode data");
            return buildFailure(true,true,false,false,data,parsedCodeData,issues);
        }
        boolean dataValid=dataValidator.isValid(parsedCodeData);
        if(!dataValid){
            issues.add("Data extracted from QR or Barcode is not valid");
            return buildFailure(true,true,false,false,data,parsedCodeData,issues);
        }
        if(ocrFields==null||ocrFields.isEmpty()){
            issues.add("OCR fields are not available for cross-field validation");
            return buildFailure(true,true,true,dataValid,data,parsedCodeData,issues);
        }
        QrCrossFieldValidationResult qrCrossFieldValidationResult;
        try{
            qrCrossFieldValidationResult=crossFieldValidator.validate(parsedCodeData.getFields(),ocrFields);
        }catch(Exception e){
            issues.add("QR cross-field validation could not be completed");
            return buildFailure(true,true,true,dataValid,data,parsedCodeData,issues);
        }
        if(!qrCrossFieldValidationResult.isValid()){
            issues.addAll(qrCrossFieldValidationResult.getMismatchs());
            return buildFailure(true,true,true,dataValid,data,parsedCodeData,issues);
        }
        return QrValidationResult.builder()
                .crossFieldCheck(true)
                .dataValid(dataValid)
                .ddecodedCodeData(data)
                .decoded(data.isDecoded())
                .detected(detected)
                .issues(issues)
                .parsedCodeData(parsedCodeData)
                .valid(detected&&data.isDecoded()&&dataValid&&qrCrossFieldValidationResult.isValid())
                .build();
    }
    private QrValidationResult buildFailure(boolean detected,boolean decoded,boolean crossFieldCheck,boolean dataValid,DecodedCodeData data,ParsedCodeData parsedCodeData,List<String> issues){
        return QrValidationResult.builder()
                .valid(false)
                .detected(detected)
                .decoded(decoded)
                .dataValid(dataValid)
                .crossFieldCheck(crossFieldCheck)
                .ddecodedCodeData(data)
                .parsedCodeData(parsedCodeData)
                .issues(issues)
                .build();
    }
}