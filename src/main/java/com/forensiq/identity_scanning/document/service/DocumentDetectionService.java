package com.forensiq.identity_scanning.document.service;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;

import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import com.forensiq.identity_scanning.document.dto.DocumentDetectionResult;
@Service
public class DocumentDetectionService {
    public DocumentDetectionResult detect(java.nio.file.Path imagePath) throws Exception {
        BufferedImage image=ImageIO.read(imagePath.toFile());
        if(image==null){
            throw new IllegalArgumentException("Unable to read image");
        }
        Mat source=bufferedImageToMat(image);
        Mat gray=new Mat();
        Mat blurred=new Mat();
        Mat edges=new Mat();
        Imgproc.cvtColor(source,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray,blurred,new Size(5,5),0);
        Imgproc.Canny(blurred,edges,50,150);
        Mat kernel=Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(5,5));
        Imgproc.morphologyEx(edges,edges,Imgproc.MORPH_CLOSE,kernel);
        List<MatOfPoint> contours=new ArrayList<>();
        Imgproc.findContours(edges,contours,new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
        if(contours.isEmpty()){
            return notDetected("No document-like contour found");
        }
     contours.sort(Comparator.comparingDouble((MatOfPoint contour)->Imgproc.contourArea(contour)).reversed());
        double imageArea=(double)source.width()*source.height();
        for(MatOfPoint contour:contours){
            double contourArea=Imgproc.contourArea(contour);
            double areaRatio=contourArea/imageArea;
            if(areaRatio<0.15){
                break;
            }
            MatOfPoint2f contour2f=new MatOfPoint2f(contour.toArray());
            double perimeter=Imgproc.arcLength(contour2f,true);
            MatOfPoint2f approx=new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f,approx,0.02*perimeter,true);
            int points=approx.toArray().length;
            Rect rect=Imgproc.boundingRect(contour);
            double boundingArea=(double)rect.width*rect.height;
            double rectangularity=boundingArea==0?0:contourArea/boundingArea;
            double aspectRatio=(double)Math.max(rect.width,rect.height)/Math.max(1,Math.min(rect.width,rect.height));
            boolean validPoints=points>=4&&points<=6;
            boolean validArea=areaRatio>=0.15&&areaRatio<=0.98;
            boolean validShape=rectangularity>=0.60;
            boolean validAspect=aspectRatio>=1.15&&aspectRatio<=2.50;
            if(validPoints&&validArea&&validShape&&validAspect){
                double areaConfidence=Math.min(100,areaRatio/0.60*40);
                double shapeConfidence=Math.min(100,rectangularity/0.90*35);
                double aspectConfidence=calculateAspectConfidence(aspectRatio);
                double confidence=Math.min(100,(areaConfidence*0.45)+(shapeConfidence*0.35)+(aspectConfidence*0.20));
                if(confidence>=60){
                    return DocumentDetectionResult.builder()
                            .detected(true)
                            .confidence(round(confidence))
                            .areaRatio(round(areaRatio))
                            .rectangularity(round(rectangularity))
                            .aspectRatio(round(aspectRatio))
                            .x(rect.x)
                            .y(rect.y)
                            .width(rect.width)
                            .height(rect.height)
                            .message("Document detected")
                            .build();
                }
            }
        }
        return notDetected("Document was not detected clearly");
    }
    private double calculateAspectConfidence(double aspectRatio){
        if(aspectRatio>=1.30&&aspectRatio<=2.00){
            return 100;
        }
        if(aspectRatio>=1.15&&aspectRatio<=2.50){
            return 75;
        }
        return 30;
    }
    private DocumentDetectionResult notDetected(String message){
        return DocumentDetectionResult.builder()
                .detected(false)
                .confidence(0)
                .areaRatio(0)
                .rectangularity(0)
                .aspectRatio(0)
                .x(0)
                .y(0)
                .width(0)
                .height(0)
                .message(message)
                .build();
    }
    private Mat bufferedImageToMat(BufferedImage image){
        BufferedImage converted=new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
        java.awt.Graphics2D graphics=converted.createGraphics();
        graphics.drawImage(image,0,0,null);
        graphics.dispose();
        byte[] pixels=((java.awt.image.DataBufferByte)converted.getRaster().getDataBuffer()).getData();
        Mat mat=new Mat(converted.getHeight(),converted.getWidth(),org.opencv.core.CvType.CV_8UC3);
        mat.put(0,0,pixels);
        return mat;
    }
    private double round(double value){
        return Math.round(value*100.0)/100.0;
    }
}