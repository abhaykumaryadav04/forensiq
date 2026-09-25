package com.forensiq.identity_scanning.information.LAYOUT;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LayoutRegion {

    private String name;

    private double x;
    private double y;

    private double width;
    private double height;

    public boolean contains(
            double wordX,
            double wordY,
            double wordWidth,
            double wordHeight) {

        double wordCenterX = wordX + wordWidth / 2.0;
        double wordCenterY = wordY + wordHeight / 2.0;

        return wordCenterX >= x
                && wordCenterX <= x + width
                && wordCenterY >= y
                && wordCenterY <= y + height;
    }
}