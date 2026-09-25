package com.forensiq.identity_scanning.information.LAYOUT;

import java.util.List;

public class LayoutFieldDetector {
 public static String findRegion(
            NormalizedWord word,
            List<LayoutRegion> regions) {

        for (LayoutRegion region : regions) {

            if (region.contains(
                    word.getX(),
                    word.getY(),
                    word.getWidth(),
                    word.getHeight())) {

                return region.getName();
            }
        }

        return null;
    }
}
