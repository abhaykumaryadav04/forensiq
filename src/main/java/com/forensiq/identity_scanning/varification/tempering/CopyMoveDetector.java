package com.forensiq.identity_scanning.varification.tempering;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import com.forensiq.identity_scanning.varification.tempering.dto.CopyMoveAnalysisResult;
@Component
public class CopyMoveDetector {
    private static final int BLOCK_SIZE=16;
    private static final int MIN_DISTANCE=64;
    private static final double SIMILARITY_THRESHOLD=5.0;
    public CopyMoveAnalysisResult analyze(BufferedImage image){
        if(image==null){
            return CopyMoveAnalysisResult.builder()
                    .analyzed(false)
                    .suspicious(false)
                    .suspicionScore(0)
                    .suspiciousMatches(0)
                    .message("Image cannot be null")
                    .build();
        }
        try{
            int width=image.getWidth();
            int height=image.getHeight();
            if(width<=0||height<=0){
                return CopyMoveAnalysisResult.builder()
                        .analyzed(false)
                        .suspicious(false)
                        .suspicionScore(0)
                        .suspiciousMatches(0)
                        .message("Image dimensions are invalid")
                        .build();
            }
            if(width<BLOCK_SIZE*2||height<BLOCK_SIZE*2){
                return CopyMoveAnalysisResult.builder()
                        .analyzed(false)
                        .suspicious(false)
                        .suspicionScore(0)
                        .suspiciousMatches(0)
                        .message("Image is too small for copy-move analysis")
                        .build();
            }
            List<ImageBlock> blocks=extractBlocks(image);
            if(blocks.size()<2){
                return CopyMoveAnalysisResult.builder()
                        .analyzed(false)
                        .suspicious(false)
                        .suspicionScore(0)
                        .suspiciousMatches(0)
                        .message("Not enough image blocks for copy-move analysis")
                        .build();
            }
            int suspiciousMatches=findSuspiciousMatches(blocks);
            double suspicionScore=calculateSuspicionScore(suspiciousMatches,blocks.size());
            boolean suspicious=suspicionScore>=50;
            return CopyMoveAnalysisResult.builder()
                    .analyzed(true)
                    .suspiciousMatches(suspiciousMatches)
                    .suspicionScore(suspicionScore)
                    .suspicious(suspicious)
                    .message(suspicious?"Possible copy-move manipulation detected":"No significant copy-move pattern detected")
                    .build();
        }catch(Exception exception){
            return CopyMoveAnalysisResult.builder()
                    .analyzed(false)
                    .suspicious(false)
                    .suspicionScore(0)
                    .suspiciousMatches(0)
                    .message("Copy-move analysis failed: "+exception.getMessage())
                    .build();
        }
    }
    private List<ImageBlock> extractBlocks(BufferedImage image){
        List<ImageBlock> blocks=new ArrayList<>();
        int width=image.getWidth();
        int height=image.getHeight();
        for(int y=0;y+BLOCK_SIZE<=height;y+=BLOCK_SIZE){
            for(int x=0;x+BLOCK_SIZE<=width;x+=BLOCK_SIZE){
                double[] signature=calculateBlockSignature(image,x,y);
                blocks.add(new ImageBlock(x,y,signature));
            }
        }
        return blocks;
    }
    private double[] calculateBlockSignature(BufferedImage image,int startX,int startY){
        int pixelCount=BLOCK_SIZE*BLOCK_SIZE;
        double sum=0;
        double sumSquared=0;
        double horizontalGradient=0;
        double verticalGradient=0;
        for(int y=startY;y<startY+BLOCK_SIZE;y++){
            for(int x=startX;x<startX+BLOCK_SIZE;x++){
                double gray=getGrayValue(image.getRGB(x,y));
                sum+=gray;
                sumSquared+=gray*gray;
                if(x>startX){
                    double left=getGrayValue(image.getRGB(x-1,y));
                    horizontalGradient+=Math.abs(gray-left);
                }
                if(y>startY){
                    double top=getGrayValue(image.getRGB(x,y-1));
                    verticalGradient+=Math.abs(gray-top);
                }
            }
        }
        double mean=sum/pixelCount;
        double variance=(sumSquared/pixelCount)-(mean*mean);
        variance=Math.max(variance,0);
        double standardDeviation=Math.sqrt(variance);
        horizontalGradient/=pixelCount;
        verticalGradient/=pixelCount;
        return new double[]{
                mean,
                standardDeviation,
                horizontalGradient,
                verticalGradient
        };
    }
    private int findSuspiciousMatches(List<ImageBlock> blocks){
        int matches=0;
        for(int i=0;i<blocks.size();i++){
            ImageBlock first=blocks.get(i);
            for(int j=i+1;j<blocks.size();j++){
                ImageBlock second=blocks.get(j);
                double distance=calculateDistance(first,second);
                if(distance<MIN_DISTANCE){
                    continue;
                }
                double signatureDifference=calculateSignatureDifference(first.signature,second.signature);
                if(signatureDifference<=SIMILARITY_THRESHOLD){
                    matches++;
                }
            }
        }
        return matches;
    }
    private double calculateSignatureDifference(double[] first,double[] second){
        double difference=0;
        for(int i=0;i<first.length;i++){
            difference+=Math.abs(first[i]-second[i]);
        }
        return difference/first.length;
    }
    private double calculateDistance(ImageBlock first,ImageBlock second){
        int xDifference=first.x-second.x;
        int yDifference=first.y-second.y;
        return Math.sqrt((double)xDifference*xDifference+(double)yDifference*yDifference);
    }
    private double calculateSuspicionScore(int suspiciousMatches,int totalBlocks){
        if(totalBlocks<=0||suspiciousMatches<=0){
            return 0;
        }
        double possiblePairs=(double)totalBlocks*(totalBlocks-1)/2;
        if(possiblePairs<=0){
            return 0;
        }
        double matchRatio=suspiciousMatches/possiblePairs;
        return Math.min(matchRatio*10000,100);
    }
    private double getGrayValue(int rgb){
        int red=(rgb>>16)&0xff;
        int green=(rgb>>8)&0xff;
        int blue=rgb&0xff;
        return 0.299*red+0.587*green+0.114*blue;
    }
    private static class ImageBlock{
        private final int x;
        private final int y;
        private final double[] signature;
        public ImageBlock(int x,int y,double[] signature){
            this.x=x;
            this.y=y;
            this.signature=signature;
        }
    }
}