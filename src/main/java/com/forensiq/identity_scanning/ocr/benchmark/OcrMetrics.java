package com.forensiq.identity_scanning.ocr.benchmark;

import org.springframework.stereotype.Service;

@Service
public class OcrMetrics {

    public double calculateCER(
            String reference,
            String prediction) {

        reference = normalize(reference);
        prediction = normalize(prediction);

        if (reference.isEmpty()) {
            return prediction.isEmpty() ? 0.0 : 1.0;
        }

        int distance =
                levenshtein(reference, prediction);

        return (double) distance / reference.length();
    }

    public double calculateWER(
            String reference,
            String prediction) {

        String[] ref =
                normalize(reference).split(" ");

        String[] pred =
                normalize(prediction).split(" ");

        if (reference.trim().isEmpty()) {
            return prediction.trim().isEmpty()
                    ? 0.0
                    : 1.0;
        }

        int distance =
                levenshtein(ref, pred);

        return (double) distance / ref.length;
    }

    private String normalize(String text) {

        return text
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase();
    }

    private int levenshtein(
            String a,
            String b) {

        int[][] dp =
                new int[a.length() + 1]
                        [b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {

            for (int j = 1; j <= b.length(); j++) {

                int cost =
                        a.charAt(i - 1)
                                == b.charAt(j - 1)
                                ? 0
                                : 1;

                dp[i][j] =
                        Math.min(
                                Math.min(
                                        dp[i - 1][j] + 1,
                                        dp[i][j - 1] + 1
                                ),
                                dp[i - 1][j - 1] + cost
                        );
            }
        }

        return dp[a.length()][b.length()];
    }

    private int levenshtein(
            String[] a,
            String[] b) {

        int[][] dp =
                new int[a.length + 1]
                        [b.length + 1];

        for (int i = 0; i <= a.length; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= b.length; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length; i++) {

            for (int j = 1; j <= b.length; j++) {

                int cost =
                        a[i - 1].equals(b[j - 1])
                                ? 0
                                : 1;

                dp[i][j] =
                        Math.min(
                                Math.min(
                                        dp[i - 1][j] + 1,
                                        dp[i][j - 1] + 1
                                ),
                                dp[i - 1][j - 1] + cost
                        );
            }
        }

        return dp[a.length][b.length];
    }
}