package com.forensiq.identity_scanning.varification.mrz;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.forensiq.identity_scanning.ocr.dto.OcrWord;

@Component
public class MrzExtractor {

    private static final int MRZ_LINE_LENGTH = 44;

    private static final double BOTTOM_REGION_RATIO = 0.40;


 
    public String extract(List<OcrWord> words) {

        if (words == null || words.isEmpty()) {
            return null;
        }


        List<OcrWord> validWords = words.stream()

                .filter(Objects::nonNull)

                .filter(word ->
                        word.getText() != null)

                .filter(word ->
                        !word.getText().isBlank())

                .collect(Collectors.toList());


        if (validWords.isEmpty()) {
            return null;
        }


        // ==================================================
        // STEP 2: FOCUS ON BOTTOM REGION
        // ==================================================

        List<OcrWord> bottomWords =
                getBottomRegionWords(validWords);


        if (bottomWords.isEmpty()) {
            return null;
        }


        // ==================================================
        // STEP 3: FIND MRZ CANDIDATE WORDS
        // ==================================================

        List<OcrWord> candidates =
                bottomWords.stream()

                        .filter(this::looksLikeMrzWord)

                        .collect(Collectors.toList());


        if (candidates.isEmpty()) {
            return null;
        }


        // ==================================================
        // STEP 4: GROUP WORDS INTO LINES
        // ==================================================

        List<List<OcrWord>> groups =
                groupIntoLines(candidates);


        if (groups.size() < 2) {
            return null;
        }


        // ==================================================
        // STEP 5: SORT LINES FROM TOP TO BOTTOM
        // ==================================================

        groups.sort(
                Comparator.comparingDouble(
                        this::averageY
                )
        );


        // ==================================================
        // STEP 6: FIND BEST MRZ LINE PAIR
        // ==================================================

        MrzCandidate bestCandidate =
                findBestMrzPair(groups);


        if (bestCandidate == null) {
            return null;
        }


        // ==================================================
        // STEP 7: RETURN RECONSTRUCTED MRZ
        // ==================================================

        return bestCandidate.line1
                + "\n"
                + bestCandidate.line2;
    }


    // ======================================================
    // GET BOTTOM REGION
    // ======================================================

    private List<OcrWord> getBottomRegionWords(
            List<OcrWord> words) {

        double minY =
                words.stream()

                        .mapToDouble(
                                OcrWord::getY
                        )

                        .min()

                        .orElse(0);


        double maxY =
                words.stream()

                        .mapToDouble(
                                OcrWord::getY
                        )

                        .max()

                        .orElse(0);


        double documentHeight =
                maxY - minY;


        /*
         * If the OCR coordinates do not provide
         * enough vertical information, use all words.
         */

        if (documentHeight <= 0) {

            return new ArrayList<>(words);
        }


        /*
         * Calculate where the bottom 40% starts.
         */

        double bottomStart =
                maxY -
                (documentHeight *
                        BOTTOM_REGION_RATIO);


        return words.stream()

                .filter(word ->
                        word.getY() >= bottomStart)

                .collect(Collectors.toList());
    }


    // ======================================================
    // DETECT MRZ-LIKE OCR WORD
    // ======================================================

    private boolean looksLikeMrzWord(
            OcrWord word) {

        String text =
                normalize(
                        word.getText()
                );


        if (text.isBlank()) {
            return false;
        }


        /*
         * MRZ mainly contains:
         *
         * A-Z
         * 0-9
         * <
         */

        long validCharacters =
                text.chars()

                        .filter(c ->
                                Character.isLetterOrDigit(c)
                                        || c == '<'
                        )

                        .count();


        double ratio =
                (double) validCharacters
                        / text.length();


        /*
         * At least 70% of characters should
         * be MRZ-compatible.
         */

        return text.length() >= 3
                && ratio >= 0.70;
    }


    // ======================================================
    // GROUP OCR WORDS INTO HORIZONTAL LINES
    // ======================================================

    private List<List<OcrWord>> groupIntoLines(
            List<OcrWord> words) {

        List<OcrWord> sorted =
                new ArrayList<>(words);


        /*
         * Sort:
         *
         * 1. Y position
         * 2. X position
         */

        sorted.sort(
                Comparator

                        .comparingDouble(
                                OcrWord::getY
                        )

                        .thenComparingDouble(
                                OcrWord::getX
                        )
        );


        List<List<OcrWord>> groups =
                new ArrayList<>();


        for (OcrWord word : sorted) {

            boolean added = false;


            for (List<OcrWord> group : groups) {

                if (isSameLine(
                        word,
                        group)) {

                    group.add(word);

                    added = true;

                    break;
                }
            }


            if (!added) {

                List<OcrWord> newGroup =
                        new ArrayList<>();

                newGroup.add(word);

                groups.add(newGroup);
            }
        }


        return groups;
    }


    // ======================================================
    // CHECK WHETHER OCR WORDS ARE ON SAME LINE
    // ======================================================

    private boolean isSameLine(
            OcrWord word,
            List<OcrWord> group) {

        double averageY =
                averageY(group);


        double averageHeight =
                group.stream()

                        .mapToDouble(
                                OcrWord::getHeight
                        )

                        .average()

                        .orElse(20);


        double difference =
                Math.abs(
                        word.getY()
                                - averageY
                );


        /*
         * Allow small OCR coordinate differences.
         */

        double tolerance =
                Math.max(
                        12,
                        averageHeight * 0.8
                );


        return difference <= tolerance;
    }


    // ======================================================
    // CALCULATE AVERAGE Y
    // ======================================================

    private double averageY(
            List<OcrWord> words) {

        return words.stream()

                .mapToDouble(
                        OcrWord::getY
                )

                .average()

                .orElse(0);
    }


    // ======================================================
    // FIND BEST MRZ LINE PAIR
    // ======================================================

    private MrzCandidate findBestMrzPair(
            List<List<OcrWord>> groups) {

        MrzCandidate best = null;


        /*
         * Try every possible line as the
         * first MRZ line.
         */

        for (int i = 0;
                i < groups.size();
                i++) {


            String firstLine =
                    joinLine(
                            groups.get(i)
                    );


            /*
             * First TD3 passport line should
             * normally begin with P<
             */

            if (!looksLikeMrzFirstLine(
                    firstLine)) {

                continue;
            }


            /*
             * Search for the second line
             * below the first line.
             */

            for (int j = i + 1;
                    j < groups.size();
                    j++) {


                String secondLine =
                        joinLine(
                                groups.get(j)
                        );


                /*
                 * Check whether this looks like
                 * TD3 second line.
                 */

                if (!looksLikeMrzSecondLine(
                        secondLine)) {

                    continue;
                }


                /*
                 * Make sure both lines contain
                 * exactly 44 characters.
                 */

                String normalizedLine1 =
                        completeLine(
                                firstLine
                        );


                String normalizedLine2 =
                        completeLine(
                                secondLine
                        );


                if (normalizedLine1 == null
                        || normalizedLine2 == null) {

                    continue;
                }


                /*
                 * Calculate confidence score.
                 */

                int score =
                        calculateTd3Score(
                                normalizedLine1,
                                normalizedLine2
                        );


                if (score <= 0) {
                    continue;
                }


                /*
                 * Keep the best candidate.
                 */

                if (best == null
                        || score > best.score) {

                    best =
                            new MrzCandidate(
                                    normalizedLine1,
                                    normalizedLine2,
                                    score
                            );
                }


                /*
                 * We found a valid second line
                 * for this first line.
                 */

                break;
            }
        }


        return best;
    }


    // ======================================================
    // CHECK FIRST MRZ LINE
    // ======================================================

    private boolean looksLikeMrzFirstLine(
            String line) {

        if (line == null ||
                line.isBlank()) {

            return false;
        }


        /*
         * Passport TD3 starts with P<
         */

        if (!line.startsWith("P<")) {
            return false;
        }


        /*
         * Need enough characters to be
         * considered a real MRZ line.
         */

        if (line.length() < 20) {
            return false;
        }


        /*
         * Characters 2-4 represent
         * issuing country.
         */

        if (line.length() >= 5) {

            String issuingCountry =
                    line.substring(2, 5);


            if (!issuingCountry.matches(
                    "[A-Z]{3}")) {

                return false;
            }
        }


        /*
         * Name area normally contains
         * '<' separators.
         */

        String nameArea =
                line.substring(5);


        return nameArea.contains("<");
    }


    // ======================================================
    // CHECK SECOND MRZ LINE
    // ======================================================

    private boolean looksLikeMrzSecondLine(
            String line) {

        if (line == null ||
                line.isBlank()) {

            return false;
        }


        /*
         * We need at least the fixed
         * fields up to expiry date.
         */

        if (line.length() < 28) {
            return false;
        }


        // --------------------------------------------------
        // PASSPORT NUMBER
        // --------------------------------------------------

        String passportNumber =
                line.substring(0, 9);


        if (!passportNumber.matches(
                "[A-Z0-9<]{9}")) {

            return false;
        }


        // --------------------------------------------------
        // PASSPORT NUMBER CHECK DIGIT
        // --------------------------------------------------

        if (!Character.isDigit(
                line.charAt(9))) {

            return false;
        }


        // --------------------------------------------------
        // NATIONALITY
        // --------------------------------------------------

        String nationality =
                line.substring(10, 13);


        if (!nationality.matches(
                "[A-Z<]{3}")) {

            return false;
        }


        // --------------------------------------------------
        // DATE OF BIRTH
        // --------------------------------------------------

        String dob =
                line.substring(13, 19);


        if (!dob.matches("\\d{6}")) {
            return false;
        }


        // --------------------------------------------------
        // DOB CHECK DIGIT
        // --------------------------------------------------

        if (!Character.isDigit(
                line.charAt(19))) {

            return false;
        }


        // --------------------------------------------------
        // GENDER
        // --------------------------------------------------

        char gender =
                line.charAt(20);


        if (gender != 'M'
                && gender != 'F'
                && gender != '<') {

            return false;
        }


        // --------------------------------------------------
        // EXPIRY DATE
        // --------------------------------------------------

        String expiry =
                line.substring(21, 27);


        return expiry.matches(
                "\\d{6}"
        );
    }


    // ======================================================
    // ENSURE EXACTLY 44 CHARACTERS
    // ======================================================

    private String completeLine(
            String line) {

        if (line == null) {
            return null;
        }


        line = normalize(line);


        /*
         * We NEVER invent missing MRZ characters.
         */

        if (line.length() < MRZ_LINE_LENGTH) {
            return null;
        }


        /*
         * If OCR has extra characters,
         * keep the first 44.
         */

        if (line.length() > MRZ_LINE_LENGTH) {

            line =
                    line.substring(
                            0,
                            MRZ_LINE_LENGTH
                    );
        }


        return line;
    }


    // ======================================================
    // JOIN OCR WORDS
    // ======================================================

    private String joinLine(
            List<OcrWord> line) {

        List<OcrWord> sorted =
                new ArrayList<>(line);


        /*
         * MRZ must be reconstructed
         * from left to right.
         */

        sorted.sort(
                Comparator.comparingDouble(
                        OcrWord::getX
                )
        );


        return sorted.stream()

                .map(
                        OcrWord::getText
                )

                .map(
                        this::normalize
                )

                .collect(
                        Collectors.joining()
                );
    }


    // ======================================================
    // NORMALIZE OCR TEXT
    // ======================================================

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }


        return value

                .replace(" ", "")

                /*
                 * OCR may recognize MRZ '<'
                 * as another similar symbol.
                 */

                .replace("«", "<")

                .replace(">", "<")

                .toUpperCase();
    }


    // ======================================================
    // TD3 SCORE
    // ======================================================

    private int calculateTd3Score(
            String line1,
            String line2) {

        int score = 0;


        // ==================================================
        // LINE 1
        // ==================================================

        /*
         * Strong passport indicator.
         */

        if (line1.startsWith("P<")) {

            score += 50;

        } else if (line1.startsWith("P")) {

            score += 25;
        }


        /*
         * Issuing country.
         */

        String issuingCountry =
                line1.substring(2, 5);


        if (issuingCountry.matches(
                "[A-Z]{3}")) {

            score += 20;
        }



        String nameArea =
                line1.substring(5, 44);


        if (nameArea.contains("<")) {

            score += 20;
        }


        String passportNumber =
                line2.substring(0, 9);


        if (passportNumber.matches(
                "[A-Z0-9<]{9}")) {

            score += 20;
        }

        if (Character.isDigit(
                line2.charAt(9))) {

            score += 10;
        }


      
        String nationality =
                line2.substring(10, 13);


        if (nationality.matches(
                "[A-Z<]{3}")) {

            score += 15;
        }


   
        String dob =
                line2.substring(13, 19);


        if (dob.matches("\\d{6}")) {

            score += 20;
        }


        if (Character.isDigit(
                line2.charAt(19))) {

            score += 10;
        }



        char gender =
                line2.charAt(20);


        if (gender == 'M'
                || gender == 'F'
                || gender == '<') {

            score += 10;
        }


        String expiry =
                line2.substring(21, 27);


        if (expiry.matches("\\d{6}")) {

            score += 20;
        }
        if (Character.isDigit(
                line2.charAt(27))) {

            score += 10;
        }

        return score;
    }

    private static class MrzCandidate {

        private final String line1;

        private final String line2;

        private final int score;


        private MrzCandidate(
                String line1,
                String line2,
                int score) {

            this.line1 = line1;

            this.line2 = line2;

            this.score = score;
        }
    }
}