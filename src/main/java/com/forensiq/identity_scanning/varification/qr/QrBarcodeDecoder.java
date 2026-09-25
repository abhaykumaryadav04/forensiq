package com.forensiq.identity_scanning.varification.qr;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.forensiq.identity_scanning.varification.qr.dto.DecodedCodeData;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

@Component
public class QrBarcodeDecoder {

    private static final List<BarcodeFormat> SUPPORTED_FORMATS = List.of(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.CODE_128,
            BarcodeFormat.CODE_39,
            BarcodeFormat.PDF_417,
            BarcodeFormat.DATA_MATRIX
    );

    public DecodedCodeData decode(BufferedImage image) throws IllegalAccessException {

        if (image == null) {
            throw new IllegalAccessException("Image cannot be null");
        }

        try {
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

            Result result = reader.decode(bitmap, hints);

            if (result == null ||
                    result.getText() == null ||
                    result.getText().isBlank()) {

                return DecodedCodeData.builder()
                        .format(null)
                        .rawData(null)
                        .decoded(false)
                        .build();
            }

            return DecodedCodeData.builder()
                    .format(result.getBarcodeFormat())
                    .rawData(result.getText())
                    .decoded(true)
                    .build();

        } catch (Exception e) {
            return DecodedCodeData.builder()
                    .format(null)
                    .rawData(null)
                    .decoded(false)
                    .build();
        }
    }
}