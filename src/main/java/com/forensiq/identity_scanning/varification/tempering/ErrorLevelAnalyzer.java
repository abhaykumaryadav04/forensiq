package com.forensiq.identity_scanning.varification.tempering;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.stereotype.Component;
import com.forensiq.identity_scanning.varification.tempering.dto.ElaAnalysisResult;
@Component
public class ErrorLevelAnalyzer {
    private static final float JPEG_QUALITY=0.90f;
    public ElaAnalysisResult analyze(BufferedImage image){
        if(image==null){
            return ElaAnalysisResult.builder()
                    .analyzed(false)
                    .suspicious(false)
                    .suspicionScore(0)
                    .message("Image cannot be null")
                    .build();
        }
        if(image.getWidth()<=0||image.getHeight()<=0){
            return ElaAnalysisResult.builder()
                    .analyzed(false)
                    .suspicious(false)
                    .suspicionScore(0)
                    .message("Image dimensions are invalid")
                    .build();
        }
        try{
            BufferedImage rgb=convertToRgb(image);
            BufferedImage compressedImage=recompressImage(rgb);
            if(compressedImage==null){
                throw new IllegalStateException("Unable to create recompressed image");
            }
            double totalError=0;
            double maximumError=0;
            int width=rgb.getWidth();
            int height=rgb.getHeight();
            long totalPixels=(long)width*height;
            for(int y=0;y<height;y++){
                for(int x=0;x<width;x++){
                    int originalRgb=rgb.getRGB(x,y);
                    int compressedRgb=compressedImage.getRGB(x,y);
                    int redError=Math.abs(((originalRgb>>16)&0xff)-((compressedRgb>>16)&0xff));
                    int greenError=Math.abs(((originalRgb>>8)&0xff)-((compressedRgb>>8)&0xff));
                    int blueError=Math.abs((originalRgb&0xff)-(compressedRgb&0xff));
                    double pixelError=(redError+greenError+blueError)/3.0;
                    totalError+=pixelError;
                    if(pixelError>maximumError){
                        maximumError=pixelError;
                    }
                }
            }
            double averageError=totalError/totalPixels;
            double suspicionScore=calculateSuspicionScore(averageError,maximumError);
            boolean suspicious=suspicionScore>=50;
            return ElaAnalysisResult.builder()
                    .analyzed(true)
                    .averageErrorLevel(averageError)
                    .maximumErrorLevel(maximumError)
                    .suspicionScore(suspicionScore)
                    .suspicious(suspicious)
                    .message(suspicious?"High compression inconsistency detected":"No major compression inconsistency detected")
                    .build();
        }catch(Exception e){
            return ElaAnalysisResult.builder()
                    .analyzed(false)
                    .suspicious(false)
                    .suspicionScore(0)
                    .message("ELA analysis failed: "+e.getMessage())
                    .build();
        }
    }
    private BufferedImage convertToRgb(BufferedImage image){
        BufferedImage rgbImage=new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics=rgbImage.createGraphics();
        graphics.drawImage(image,0,0,null);
        graphics.dispose();
        return rgbImage;
    }
    private double calculateSuspicionScore(double averageError,double maximumError){
        double averageScore=Math.min(averageError*10,70);
        double maximumScore=Math.min(maximumError/255.0*30,30);
        return Math.min(averageScore+maximumScore,100);
    }
    private BufferedImage recompressImage(BufferedImage image) throws IOException{
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        ImageWriter writer=null;
        ImageOutputStream imageOutputStream=null;
        try{
            var writers=ImageIO.getImageWritersByFormatName("jpg");
            if(!writers.hasNext()){
                throw new IllegalStateException("JPEG writer not available");
            }
            writer=writers.next();
            imageOutputStream=ImageIO.createImageOutputStream(output);
            writer.setOutput(imageOutputStream);
            ImageWriteParam writeParam=writer.getDefaultWriteParam();
            if(writeParam.canWriteCompressed()){
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(JPEG_QUALITY);
            }
            writer.write(null,new IIOImage(image,null,null),writeParam);
            imageOutputStream.flush();
            ByteArrayInputStream input=new ByteArrayInputStream(output.toByteArray());
            return ImageIO.read(input);
        }finally{
            if(writer!=null){
                writer.dispose();
            }
            if(imageOutputStream!=null){
                imageOutputStream.close();
            }
            output.close();
        }
    }
}