package com.forensiq.identity_scanning.varification.qr.dto;

import com.google.zxing.BarcodeFormat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DecodedCodeData {

    private String rawData;
    private BarcodeFormat format;
    private boolean decoded;
}
