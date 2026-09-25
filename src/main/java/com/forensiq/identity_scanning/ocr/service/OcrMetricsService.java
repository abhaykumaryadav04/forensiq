package com.forensiq.identity_scanning.ocr.service;


import org.springframework.stereotype.Service;

@Service
public class OcrMetricsService {

    public double cer( String groundTruth, String prediction) {

        groundTruth =  normalize(groundTruth);
        prediction =normalize(prediction);
        if (groundTruth.isEmpty()) {
            return prediction.isEmpty() ? 0.0  : 1.0;
        }
        int distance =levenshtein( groundTruth,  prediction );
        return (double) distance / groundTruth.length();
    }

    private String normalize( String text) {

     return text.replaceAll("\\s+", " ").trim().toUpperCase();
    }

    private int levenshtein( String a,String b) {
        int[][] dp =new int[ a.length() + 1][  b.length() + 1 ];
        for (int i = 0;i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1;i <= a.length();i++) {
            for (int j = 1;j <= b.length();j++) {
                int cost =  a.charAt(i - 1)  == b.charAt(j - 1)? 0: 1;
 dp[i][j] =Math.min( Math.min(  dp[i - 1][j] + 1, dp[i][j - 1] + 1 ),  dp[i - 1][j - 1]+ cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    public double wer( String groundTruth,String prediction) {

    String[] reference = normalize(groundTruth).split(" ");

    String[] hypothesis = normalize(prediction).split(" ");

    if (reference.length == 1&& reference[0].isEmpty()) {
        return hypothesis.length == 1
                && hypothesis[0].isEmpty()
                ? 0.0
                : 1.0;
    }

    int[][] dp = new int[   reference.length + 1][ hypothesis.length + 1 ];
    for (int i = 0;i <= reference.length; i++) {
        dp[i][0] = i;
    }
    for (int j = 0; j <= hypothesis.length;j++) {
        dp[0][j] = j;
    }
    for (int i = 1; i <= reference.length; i++) {
        for (int j = 1; j <= hypothesis.length;j++) {
            int cost =reference[i - 1].equals(hypothesis[j - 1])? 0: 1;

            dp[i][j] = Math.min(  Math.min(  dp[i - 1][j] + 1, dp[i][j - 1] + 1),dp[i - 1][j - 1]  + cost  );
        }
    }

    return (double) dp[ reference.length ][  hypothesis.length ] / reference.length;
}

}
