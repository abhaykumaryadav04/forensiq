package com.forensiq.identity_scanning.varification.qr;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

@Component
public class QrBarcodeDetector {

    private static final List<BarcodeFormat> SUPPORTED_FORMATS = List.of(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.CODE_128,
            BarcodeFormat.CODE_39,
            BarcodeFormat.PDF_417,
            BarcodeFormat.DATA_MATRIX
    );

    public boolean detect(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        try {
            Result result = decode(image);
            return result != null && result.getText() != null && !result.getText().isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    private Result decode(BufferedImage image) throws NotFoundException {
        LuminanceSource luminanceSource =
                new BufferedImageLuminanceSource(image);

        BinaryBitmap bitmap =
                new BinaryBitmap(
                        new HybridBinarizer(luminanceSource)
                );

        Map<DecodeHintType, Object> hints =
                new EnumMap<>(DecodeHintType.class);

        hints.put(
                DecodeHintType.POSSIBLE_FORMATS,
                SUPPORTED_FORMATS
        );

        hints.put(
                DecodeHintType.TRY_HARDER,
                Boolean.TRUE
        );

        MultiFormatReader reader =
                new MultiFormatReader();

        return reader.decode(bitmap, hints);
    }
}