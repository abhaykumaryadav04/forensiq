package com.forensiq.identity_scanning.information.LAYOUT;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NormalizedWord {
private String text;

    private float confidence;

    private double x;
    private double y;

    private double width;
    private double height;
}
