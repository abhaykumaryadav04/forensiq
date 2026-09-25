package com.forensiq.identity_scanning.information.LAYOUT;



import java.util.List;

public class PassportLayout {

    private PassportLayout() {
    }

    public static List<LayoutRegion> getRegions() {

        return List.of(

                new LayoutRegion(
                        "PHOTO",
                        0.05,
                        0.15,
                        0.30,
                        0.40
                ),
                new LayoutRegion(
                        "NAME",
                        0.40,
                        0.20,
                        0.45,
                        0.15
                ),
                new LayoutRegion(
                        "DATE_OF_BIRTH",
                        0.40,
                        0.35,
                        0.30,
                        0.10
                ),
                new LayoutRegion(
                        "NATIONALITY",
                        0.40,
                        0.45,
                        0.30,
                        0.10
                ),
                new LayoutRegion(
                        "GENDER",
                        0.40,
                        0.55,
                        0.20,
                        0.10
                ),
                new LayoutRegion(
                        "ISSUE_DATE",
                        0.40,
                        0.60,
                        0.30,
                        0.10
                ),
                new LayoutRegion(
                        "EXPIRY_DATE",
                        0.40,
                        0.70,
                        0.30,
                        0.10
                ),
                new LayoutRegion(
                        "MRZ",
                        0.05,
                        0.80,
                        0.90,
                        0.15
                )
        );
    }
}