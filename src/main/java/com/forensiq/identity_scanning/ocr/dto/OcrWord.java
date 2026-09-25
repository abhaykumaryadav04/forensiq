package com.forensiq.identity_scanning.ocr.dto;

import lombok.Builder;


import lombok.Data;

@Data
@Builder
public class OcrWord {
    
  private String text;

    private Float confidence;

    private Integer x;

    private Integer y;

    private Integer width;

    private Integer height;

}
