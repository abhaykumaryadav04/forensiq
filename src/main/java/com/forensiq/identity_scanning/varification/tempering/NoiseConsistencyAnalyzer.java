package com.forensiq.identity_scanning.varification.tempering;
import java.awt.image.BufferedImage;
import org.springframework.stereotype.Component;
import com.forensiq.identity_scanning.varification.tempering.dto.NoiseAnalysisResult;
@Component
public class NoiseConsistencyAnalyzer {
    private static final int BLOCK_SIZE=32;
    public NoiseAnalysisResult analyze(BufferedImage image){
        if(image==null){
            return NoiseAnalysisResult.builder()
                    .analyzed(false)
                    .suspicious(false)
                    .suspicionScore(0)
                    .message("Image cannot be null")
                    .build();
        }
        try{
            int width=image.getWidth();
            int height=image.getHeight();
            if(width<=0||height<=0){
                return NoiseAnalysisResult.builder()
                        .analyzed(false)
                        .suspicious(false)
                        .suspicionScore(0)
                        .message("Image dimensions are invalid")
                        .build();
            }
            if(width<BLOCK_SIZE||height<BLOCK_SIZE){
                return NoiseAnalysisResult.builder()
                        .analyzed(false)
                        .suspicious(false)
                        .suspicionScore(0)
                        .message("Image is too small for noise analysis")
                        .build();
            }
            double totalVariance=0;
            double totalVarianceSquared=0;
            int blockCount=0;
            for(int y=0;y+BLOCK_SIZE<=height;y+=BLOCK_SIZE){
                for(int x=0;x+BLOCK_SIZE<=width;x+=BLOCK_SIZE){
                    double variance=calculateBlockVariance(image,x,y,BLOCK_SIZE,BLOCK_SIZE);
                    totalVariance+=variance;
                    totalVarianceSquared+=variance*variance;
                    blockCount++;
                }
            }
            if(blockCount==0){
                return NoiseAnalysisResult.builder()
                        .analyzed(false)
                        .suspicious(false)
                        .suspicionScore(0)
                        .message("Unable to analyze image blocks")
                        .build();
            }
            double averageNoise=totalVariance/blockCount;
            double noiseVariation=calculateStandardDeviation(totalVariance,totalVarianceSquared,blockCount);
            double suspicionScore=calculateSuspicionScore(averageNoise,noiseVariation);
            boolean suspicious=suspicionScore>=50;
            return NoiseAnalysisResult.builder()
                    .analyzed(true)
                    .averageNoise(averageNoise)
                    .noiseVariation(noiseVariation)
                    .suspicionScore(suspicionScore)
                    .suspicious(suspicious)
                    .message(suspicious?"Significant noise inconsistency detected":"Noise pattern appears reasonably consistent")
                    .build();
        }catch(Exception exception){
            return NoiseAnalysisResult.builder()
                    .analyzed(false)
                    .suspicious(false)
                    .suspicionScore(0)
                    .message("Noise analysis failed: "+exception.getMessage())
                    .build();
        }
    }
    private double calculateBlockVariance(BufferedImage image,int startX,int startY,int blockWidth,int blockHeight){
        int pixelCount=blockWidth*blockHeight;
        double sum=0;
        for(int y=startY;y<startY+blockHeight;y++){
            for(int x=startX;x<startX+blockWidth;x++){
                int gray=getGrayValue(image.getRGB(x,y));
                sum+=gray;
            }
        }
        double mean=sum/pixelCount;
        double varianceSum=0;
        for(int y=startY;y<startY+blockHeight;y++){
            for(int x=startX;x<startX+blockWidth;x++){
                int gray=getGrayValue(image.getRGB(x,y));
                double difference=gray-mean;
                varianceSum+=difference*difference;
            }
        }
        return varianceSum/pixelCount;
    }
    private int getGrayValue(int rgb){
        int red=(rgb>>16)&0xff;
        int green=(rgb>>8)&0xff;
        int blue=rgb&0xff;
        return (int)(0.299*red+0.587*green+0.114*blue);
    }
    private double calculateStandardDeviation(double sum,double sumSquared,int count){
        double mean=sum/count;
        double variance=(sumSquared/count)-(mean*mean);
        return Math.sqrt(Math.max(variance,0));
    }
    private double calculateSuspicionScore(double averageNoise,double noiseVariation){
        if(averageNoise<=0){
            return 0;
        }
        double coefficientOfVariation=noiseVariation/averageNoise;
        return Math.min(coefficientOfVariation*100,100);
    }
}