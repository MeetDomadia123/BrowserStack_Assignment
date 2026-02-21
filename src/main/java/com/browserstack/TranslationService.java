package com.browserstack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TranslationService {

    private static final String API_URL = "https://api.mymemory.translated.net/get";

    /**
     * Translates a single text from Spanish to English using MyMemory Translation API.
     */
    public static String translate(String text, String sourceLang, String targetLang) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            String langPair = sourceLang + "|" + targetLang;
            String urlStr = API_URL + "?q=" + encodedText + "&langpair=" + URLEncoder.encode(langPair, StandardCharsets.UTF_8.toString());

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Translation API returned error code: " + responseCode);
                return text;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();

            // Parse JSON response
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonObject responseData = jsonResponse.getAsJsonObject("responseData");
            String translatedText = responseData.get("translatedText").getAsString();

            return translatedText;

        } catch (Exception e) {
            System.out.println("Translation failed for: " + text);
            e.printStackTrace();
            return text;
        }
    }

    /**
     * Translates a list of article titles from Spanish to English.
     */
    public static List<String> translateTitles(List<Article> articles) {
        List<String> translatedTitles = new ArrayList<>();

        System.out.println("\n========================================");
        System.out.println("TRANSLATING ARTICLE TITLES (Spanish -> English)");
        System.out.println("========================================\n");

        for (int i = 0; i < articles.size(); i++) {
            String originalTitle = articles.get(i).getTitle();
            String translated = translate(originalTitle, "es", "en");

            translatedTitles.add(translated);

            System.out.println("Article " + (i + 1) + ":");
            System.out.println("  Spanish:  " + originalTitle);
            System.out.println("  English:  " + translated);
            System.out.println();

            // Small delay to avoid rate limiting
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return translatedTitles;
    }
}
