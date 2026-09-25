package com.forensiq.identity_scanning.ocr.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

@Service
public class ImagePreprocessingService {

    public List<Path> generateCandidates(Path imagePath)
            throws IOException {

        BufferedImage bufferedImage =
                ImageIO.read(imagePath.toFile());

        if (bufferedImage == null) {
            throw new IllegalArgumentException(
                    "Unable to read image"
            );
        }

        List<Path> candidates = new ArrayList<>();

        Mat original =
                bufferedImageToMat(bufferedImage);


        /*
         * CANDIDATE 1
         * Original image
         *
         * Important:
         * Sometimes the original image gives
         * the best OCR result.
         */
        candidates.add(
                saveMat(original)
        );


        /*
         * CANDIDATE 2
         * Grayscale only
         */
        Mat gray = new Mat();

        Imgproc.cvtColor(
                original,
                gray,
                Imgproc.COLOR_BGR2GRAY
        );

        candidates.add(
                saveMat(gray)
        );


        /*
         * CANDIDATE 3
         * Grayscale + Upscale
         *
         * Good for small text.
         */
        Mat resized = new Mat();

        Imgproc.resize(
                gray,
                resized,
                new Size(),
                getScale(gray),
                getScale(gray),
                Imgproc.INTER_CUBIC
        );

        candidates.add(
                saveMat(resized)
        );


        /*
         * CANDIDATE 4
         * CLAHE + Upscale
         *
         * Mild local contrast enhancement.
         * No aggressive thresholding.
         */
        Mat claheResult = new Mat();

        org.opencv.imgproc.CLAHE clahe =
                Imgproc.createCLAHE(
                        1.5,
                        new Size(8, 8)
                );

        clahe.apply(
                gray,
                claheResult
        );

        Mat claheResized = new Mat();

        Imgproc.resize(
                claheResult,
                claheResized,
                new Size(),
                getScale(claheResult),
                getScale(claheResult),
                Imgproc.INTER_CUBIC
        );

        candidates.add(
                saveMat(claheResized)
        );


        return candidates;
    }


    private double getScale(Mat image) {

        int maxDimension =
                Math.max(
                        image.width(),
                        image.height()
                );

        if (maxDimension < 1000) {
            return 2.0;
        }

        if (maxDimension < 1800) {
            return 1.5;
        }

        return 1.0;
    }


    private Path saveMat(Mat mat)
            throws IOException {

        BufferedImage image =
                matToBufferedImage(mat);

        Path output =
                Files.createTempFile(
                        "ocr-candidate-",
                        ".png"
                );

        ImageIO.write(
                image,
                "png",
                output.toFile()
        );

        return output;
    }


    private Mat bufferedImageToMat(
            BufferedImage image
    ) {

        BufferedImage converted =
                new BufferedImage(
                        image.getWidth(),
                        image.getHeight(),
                        BufferedImage.TYPE_3BYTE_BGR
                );

        Graphics2D graphics =
                converted.createGraphics();

        graphics.drawImage(
                image,
                0,
                0,
                null
        );

        graphics.dispose();

        byte[] pixels =
                ((DataBufferByte)
                        converted
                                .getRaster()
                                .getDataBuffer())
                        .getData();

        Mat mat =
                new Mat(
                        converted.getHeight(),
                        converted.getWidth(),
                        CvType.CV_8UC3
                );

        mat.put(
                0,
                0,
                pixels
        );

        return mat;
    }


    private BufferedImage matToBufferedImage(
            Mat mat
    ) {

        int type;

        if (mat.channels() == 1) {

            type =
                    BufferedImage.TYPE_BYTE_GRAY;

        } else {

            type =
                    BufferedImage.TYPE_3BYTE_BGR;
        }


        BufferedImage image =
                new BufferedImage(
                        mat.width(),
                        mat.height(),
                        type
                );


        byte[] data =
                ((DataBufferByte)
                        image
                                .getRaster()
                                .getDataBuffer())
                        .getData();


        mat.get(
                0,
                0,
                data
        );

        return image;
    }
}