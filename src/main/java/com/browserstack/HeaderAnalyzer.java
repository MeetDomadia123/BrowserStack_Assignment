package com.browserstack;

import java.util.*;

public class HeaderAnalyzer {

    /**
     * Analyzes translated headers to find words repeated more than twice across all titles.
     */
    public static Map<String, Integer> analyzeHeaders(List<String> translatedTitles) {
        System.out.println("\n========================================");
        System.out.println("ANALYZING TRANSLATED HEADERS");
        System.out.println("========================================\n");

        // Count word occurrences across all titles
        Map<String, Integer> wordCount = new HashMap<>();

        for (String title : translatedTitles) {
            // Remove punctuation and convert to lowercase
            String cleaned = title.replaceAll("[^a-zA-Z\\s]", "").toLowerCase().trim();
            String[] words = cleaned.split("\\s+");

            for (String word : words) {
                if (!word.isEmpty()) {
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        }

        // Filter words that appear more than twice
        Map<String, Integer> repeatedWords = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() > 2) {
                repeatedWords.put(entry.getKey(), entry.getValue());
            }
        }

        // Print results
        if (repeatedWords.isEmpty()) {
            System.out.println("No words found that are repeated more than twice across all headers.");
        } else {
            System.out.println("Words repeated more than twice across all headers:");
            System.out.println("--------------------------------------------------");
            for (Map.Entry<String, Integer> entry : repeatedWords.entrySet()) {
                System.out.println("  \"" + entry.getKey() + "\" -> " + entry.getValue() + " times");
            }
        }

        // Also print all word counts for reference
        System.out.println("\nFull word frequency (all words):");
        System.out.println("--------------------------------------------------");
        wordCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(entry -> System.out.println("  \"" + entry.getKey() + "\" -> " + entry.getValue() + " times"));

        return repeatedWords;
    }
}
