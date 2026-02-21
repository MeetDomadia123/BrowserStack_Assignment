package com.browserstack;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public class ElPaisScraper {

    private WebDriver driver;
    private WebDriverWait wait;
    private List<Article> articles = new ArrayList<>();

    public static void main(String[] args) {
        ElPaisScraper scraper = new ElPaisScraper();
        try {
            scraper.setUp();
            scraper.scrapeOpinionArticles();
            scraper.printArticles();

            // Step 2: Translate article titles
            List<String> translatedTitles = TranslationService.translateTitles(scraper.getArticles());

            // Step 3: Analyze translated headers for repeated words
            HeaderAnalyzer.analyzeHeaders(translatedTitles);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scraper.tearDown();
        }
    }

    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--lang=es");
        options.addArguments("--accept-lang=es");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void setUp(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public List<Article> scrapeOpinionArticles() {
        // Navigate to El Pais
        driver.get("https://elpais.com/");

        // Accept cookies if the dialog appears
        acceptCookies();

        // Verify the page is in Spanish
        String pageLanguage = driver.findElement(By.tagName("html")).getAttribute("lang");
        System.out.println("Page language: " + pageLanguage);
        if (pageLanguage != null && pageLanguage.startsWith("es")) {
            System.out.println("Website is displayed in Spanish. Confirmed!");
        } else {
            System.out.println("Warning: Website may not be in Spanish. Detected language: " + pageLanguage);
        }

        // Navigate to Opinion section
        navigateToOpinionSection();

        // Fetch the first 5 articles
        fetchFirstFiveArticles();

        return articles;
    }

    private void acceptCookies() {
        try {
            Thread.sleep(3000);
            List<WebElement> acceptButtons = driver.findElements(By.id("didomi-notice-agree-button"));
            if (!acceptButtons.isEmpty()) {
                acceptButtons.get(0).click();
                System.out.println("Cookies accepted.");
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.out.println("No cookie dialog found or already accepted.");
        }
    }

    private void navigateToOpinionSection() {
        // Navigate directly to the Opinion section URL for reliability
        driver.get("https://elpais.com/opinion/");
        System.out.println("Navigated to Opinion section.");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        acceptCookies();
    }

    private void fetchFirstFiveArticles() {
        System.out.println("\n========================================");
        System.out.println("Fetching first 5 Opinion articles...");
        System.out.println("========================================\n");

        // Scroll down to ensure all articles are loaded
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, 1000)");
        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        // Find all article links with h2 headings
        int count = 0;
        List<String> articleUrls = new ArrayList<>();
        List<String> articleTitles = new ArrayList<>();
        List<String> articleImageUrls = new ArrayList<>();

        // Try finding articles with h2 > a pattern first
        List<WebElement> articleElements = driver.findElements(By.cssSelector("article"));

        for (WebElement articleEl : articleElements) {
            if (count >= 5) break;

            try {
                // Look for h2 inside the article
                WebElement headlineEl = articleEl.findElement(By.cssSelector("h2 a"));
                String articleUrl = headlineEl.getAttribute("href");
                String headlineText = headlineEl.getText().trim();

                if (articleUrl == null || articleUrl.isEmpty() || articleUrls.contains(articleUrl)) continue;
                if (headlineText.isEmpty()) continue;

                articleUrls.add(articleUrl);
                articleTitles.add(headlineText);

                // Try to get image
                String imageUrl = null;
                try {
                    WebElement imgEl = articleEl.findElement(By.cssSelector("img"));
                    imageUrl = imgEl.getAttribute("src");
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        imageUrl = imgEl.getAttribute("data-src");
                    }
                } catch (Exception imgEx) {}
                articleImageUrls.add(imageUrl);
                count++;

                System.out.println("Found article " + count + ": " + headlineText);
            } catch (Exception e) {
                // Not an article with h2
            }
        }

        // If we didn't find 5 articles, scroll more and retry
        if (count < 5) {
            js.executeScript("window.scrollTo(0, 3000)");
            try { Thread.sleep(2000); } catch (InterruptedException e) {}

            articleElements = driver.findElements(By.cssSelector("article"));
            for (WebElement articleEl : articleElements) {
                if (count >= 5) break;

                try {
                    WebElement headlineEl = articleEl.findElement(By.cssSelector("h2 a"));
                    String articleUrl = headlineEl.getAttribute("href");
                    String headlineText = headlineEl.getText().trim();

                    if (articleUrl == null || articleUrl.isEmpty() || articleUrls.contains(articleUrl)) continue;
                    if (headlineText.isEmpty()) continue;

                    articleUrls.add(articleUrl);
                    articleTitles.add(headlineText);

                    String imageUrl = null;
                    try {
                        WebElement imgEl = articleEl.findElement(By.cssSelector("img"));
                        imageUrl = imgEl.getAttribute("src");
                        if (imageUrl == null || imageUrl.isEmpty()) {
                            imageUrl = imgEl.getAttribute("data-src");
                        }
                    } catch (Exception imgEx) {}
                    articleImageUrls.add(imageUrl);
                    count++;

                    System.out.println("Found article " + count + ": " + headlineText);
                } catch (Exception e) {}
            }
        }

        // Final fallback: if still less than 5, try broader selectors
        if (count < 5) {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            try { Thread.sleep(3000); } catch (InterruptedException e) {}

            List<WebElement> allLinks = driver.findElements(By.cssSelector("h2 a[href*='/opinion/']"));
            for (WebElement link : allLinks) {
                if (count >= 5) break;
                try {
                    String articleUrl = link.getAttribute("href");
                    String headlineText = link.getText().trim();

                    if (articleUrl == null || articleUrl.isEmpty() || articleUrls.contains(articleUrl)) continue;
                    if (headlineText.isEmpty()) continue;

                    articleUrls.add(articleUrl);
                    articleTitles.add(headlineText);
                    articleImageUrls.add(null);
                    count++;

                    System.out.println("Found article " + count + ": " + headlineText);
                } catch (Exception e) {}
            }
        }

        System.out.println("\nTotal articles found: " + articleUrls.size());
        System.out.println("Visiting each article for full content...\n");

        // Visit each article to get full content
        for (int i = 0; i < articleUrls.size(); i++) {
            String url = articleUrls.get(i);
            String fallbackTitle = articleTitles.get(i);
            String listingImageUrl = articleImageUrls.get(i);

            try {
                driver.get(url);
                Thread.sleep(3000);

                acceptCookies();

                // Get article title from the article page
                String title = fallbackTitle;
                try {
                    WebElement titleEl = driver.findElement(By.cssSelector("article header h1"));
                    String pageTitle = titleEl.getText().trim();
                    if (!pageTitle.isEmpty()) {
                        title = pageTitle;
                    }
                } catch (Exception e) {
                    try {
                        WebElement titleEl = driver.findElement(By.tagName("h1"));
                        String pageTitle = titleEl.getText().trim();
                        if (!pageTitle.isEmpty() && !pageTitle.equalsIgnoreCase("Opinión")) {
                            title = pageTitle;
                        }
                    } catch (Exception e2) {}
                }

                // Get article content
                StringBuilder content = new StringBuilder();
                try {
                    List<WebElement> paragraphs = driver.findElements(By.cssSelector("article .a_c p"));
                    if (paragraphs.isEmpty()) {
                        paragraphs = driver.findElements(By.cssSelector("article p"));
                    }
                    for (WebElement p : paragraphs) {
                        String text = p.getText().trim();
                        if (!text.isEmpty()) {
                            content.append(text).append("\n");
                        }
                    }
                } catch (Exception e) {
                    content.append("Content not available.");
                }

                // Get cover image
                String imageUrl = null;
                try {
                    WebElement imgEl = driver.findElement(By.cssSelector("article figure img"));
                    imageUrl = imgEl.getAttribute("src");
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        imageUrl = imgEl.getAttribute("data-src");
                    }
                } catch (Exception e) {
                    imageUrl = listingImageUrl;
                }

                // Download the image if available
                String savedImagePath = null;
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    savedImagePath = downloadImage(imageUrl, i + 1);
                }

                Article article = new Article(title, content.toString().trim(), imageUrl, savedImagePath);
                articles.add(article);

            } catch (Exception e) {
                System.out.println("Error fetching article at: " + url);
                e.printStackTrace();
            }
        }
    }

    private String downloadImage(String imageUrl, int articleNumber) {
        try {
            String imagesDir = "images";
            Files.createDirectories(Paths.get(imagesDir));

            String extension = ".jpg";
            if (imageUrl.contains(".png")) extension = ".png";
            else if (imageUrl.contains(".webp")) extension = ".webp";

            String fileName = imagesDir + "/article_" + articleNumber + "_cover" + extension;

            URL url = new URL(imageUrl);
            try (InputStream in = url.openStream()) {
                Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image saved: " + fileName);
                return fileName;
            }
        } catch (Exception e) {
            System.out.println("Failed to download image: " + e.getMessage());
            return null;
        }
    }

    public void printArticles() {
        System.out.println("\n========================================");
        System.out.println("SCRAPED ARTICLES (in Spanish)");
        System.out.println("========================================\n");

        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            System.out.println("--- Article " + (i + 1) + " ---");
            System.out.println("Title: " + article.getTitle());
            System.out.println("Content: " + article.getContent().substring(0, Math.min(article.getContent().length(), 500)) + "...");
            if (article.getSavedImagePath() != null) {
                System.out.println("Cover Image saved at: " + article.getSavedImagePath());
            } else {
                System.out.println("Cover Image: Not available");
            }
            System.out.println();
        }
    }

    public List<Article> getArticles() {
        return articles;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
