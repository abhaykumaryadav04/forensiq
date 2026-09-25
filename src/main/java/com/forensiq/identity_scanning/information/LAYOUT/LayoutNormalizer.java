package com.forensiq.identity_scanning.information.LAYOUT;



import com.forensiq.identity_scanning.ocr.dto.OcrWord;

public class LayoutNormalizer {

    private LayoutNormalizer() {
    }

    public static NormalizedWord normalize(
            OcrWord word,
            int imageWidth,
            int imageHeight) {

        double x =
                (double) word.getX() / imageWidth;

        double y =
                (double) word.getY() / imageHeight;

        double width =
                (double) word.getWidth() / imageWidth;

        double height =
                (double) word.getHeight() / imageHeight;

        return new NormalizedWord(
                word.getText(),
                word.getConfidence(),
                x,
                y,
                width,
                height
        );
    }
}
