package com.forensiq.identity_scanning.document.service;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import com.forensiq.identity_scanning.document.dto.ImageQualityResult;
@Service
public class ImageQualityService {
    public ImageQualityResult analyze(java.nio.file.Path imagePath) throws Exception {
        BufferedImage image=ImageIO.read(imagePath.toFile());
        if(image==null){
            throw new IllegalArgumentException("Unable to read camera image");
        }
        int width=image.getWidth();
        int height=image.getHeight();
        List<String> issues=new ArrayList<>();
        Mat mat=bufferedImageToMat(image);
        Mat gray=new Mat();
        Imgproc.cvtColor(mat,gray,Imgproc.COLOR_BGR2GRAY);
        Mat laplacian=new Mat();
        Imgproc.Laplacian(gray,laplacian,CvType.CV_64F);
        MatOfDouble mean=new MatOfDouble();
        MatOfDouble stddev=new MatOfDouble();
        Core.meanStdDev(laplacian,mean,stddev);
        double blurScore=Math.pow(stddev.get(0,0)[0],2);
        double brightness=Core.mean(gray).val[0];
        MatOfDouble grayMean=new MatOfDouble();
        MatOfDouble grayStddev=new MatOfDouble();
        Core.meanStdDev(gray,grayMean,grayStddev);
        double contrast=grayStddev.get(0,0)[0];
        double resolutionScore=calculateResolutionScore(width,height);
        double blurQuality=calculateBlurScore(blurScore);
        double brightnessQuality=calculateBrightnessScore(brightness);
        double contrastQuality=calculateContrastScore(contrast);
        double qualityScore=(resolutionScore*0.25)+(blurQuality*0.35)+(brightnessQuality*0.20)+(contrastQuality*0.20);
        if(Math.min(width,height)<600){
            issues.add("Image resolution is too low");
        }
        if(blurScore<40){
            issues.add("Image is too blurry");
        }
        if(brightness<35){
            issues.add("Image is too dark");
        }
        if(brightness>230){
            issues.add("Image is too bright");
        }
        if(contrast<15){
            issues.add("Image contrast is too low");
        }
        boolean acceptable=qualityScore>=65&&!issues.contains("Image resolution is too low")&&!issues.contains("Image is too blurry");
        return ImageQualityResult.builder()
                .acceptable(acceptable)
                .qualityScore(round(qualityScore))
                .width(width)
                .height(height)
                .blurScore(round(blurScore))
                .brightness(round(brightness))
                .contrast(round(contrast))
                .issues(issues)
                .build();
    }
    private double calculateResolutionScore(int width,int height){
        int minDimension=Math.min(width,height);
        if(minDimension>=1400){
            return 100;
        }
        if(minDimension>=1000){
            return 90;
        }
        if(minDimension>=800){
            return 80;
        }
        if(minDimension>=600){
            return 65;
        }
        return 35;
    }
    private double calculateBlurScore(double blurScore){
        if(blurScore>=300){
            return 100;
        }
        if(blurScore>=150){
            return 90;
        }
        if(blurScore>=80){
            return 80;
        }
        if(blurScore>=40){
            return 65;
        }
        return 25;
    }
    private double calculateBrightnessScore(double brightness){
        if(brightness>=60&&brightness<=200){
            return 100;
        }
        if(brightness>=45&&brightness<60){
            return 75;
        }
        if(brightness>200&&brightness<=220){
            return 75;
        }
        if(brightness>=35&&brightness<45){
            return 55;
        }
        if(brightness>220&&brightness<=230){
            return 55;
        }
        return 25;
    }
    private double calculateContrastScore(double contrast){
        if(contrast>=45){
            return 100;
        }
        if(contrast>=30){
            return 85;
        }
        if(contrast>=20){
            return 70;
        }
        if(contrast>=15){
            return 55;
        }
        return 25;
    }
    private Mat bufferedImageToMat(BufferedImage image){
        BufferedImage converted=new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
        java.awt.Graphics2D graphics=converted.createGraphics();
        graphics.drawImage(image,0,0,null);
        graphics.dispose();
        byte[] pixels=((java.awt.image.DataBufferByte)converted.getRaster().getDataBuffer()).getData();
        Mat mat=new Mat(converted.getHeight(),converted.getWidth(),CvType.CV_8UC3);
        mat.put(0,0,pixels);
        return mat;
    }
    private double round(double value){
        return Math.round(value*100.0)/100.0;
    }
}