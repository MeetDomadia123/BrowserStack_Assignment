package com.browserstack;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.*;

import java.net.URL;
import java.util.*;

public class BrowserStackTest {

    private static final String BROWSERSTACK_USERNAME = "meetdomadia_IDcT50";
    private static final String BROWSERSTACK_ACCESS_KEY = "EqQvuZUXztdetCvMsGjy";
    private static final String BROWSERSTACK_URL = "https://" + BROWSERSTACK_USERNAME + ":" + BROWSERSTACK_ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

    private WebDriver driver;

    @DataProvider(name = "browserConfigs", parallel = true)
    public static Object[][] browserConfigs() {
        return new Object[][]{
            // Desktop Browsers (browserName, browserVersion, os, osVersion, deviceName, testLabel)
            {"Chrome", "latest", "Windows", "11", null, "Desktop - Chrome on Windows 11"},
            {"Firefox", "latest", "OS X", "Sonoma", null, "Desktop - Firefox on macOS Sonoma"},
            {"Edge", "latest", "Windows", "10", null, "Desktop - Edge on Windows 10"},
            // Mobile Browsers
            {"safari", "17", "iOS", "17", "iPhone 15", "Mobile - iPhone 15"},
            {"samsung", "latest", "Android", "13.0", "Samsung Galaxy S23", "Mobile - Samsung Galaxy S23"},
        };
    }

    @Test(dataProvider = "browserConfigs")
    public void testElPaisScraper(String browserName, String browserVersion,
                                   String os, String osVersion,
                                   String deviceName, String testLabel) throws Exception {

        System.out.println("\n==========================================================");
        System.out.println("STARTING TEST: " + testLabel);
        System.out.println("==========================================================\n");

        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("browserName", browserName);
        capabilities.setCapability("browserVersion", browserVersion);

        // BrowserStack W3C options
        HashMap<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("userName", BROWSERSTACK_USERNAME);
        bstackOptions.put("accessKey", BROWSERSTACK_ACCESS_KEY);
        bstackOptions.put("projectName", "El Pais Scraper");
        bstackOptions.put("buildName", "El Pais Opinion Articles Test");
        bstackOptions.put("sessionName", testLabel);
        bstackOptions.put("os", os);
        bstackOptions.put("osVersion", osVersion);

        if (deviceName != null) {
            bstackOptions.put("deviceName", deviceName);
        }

        capabilities.setCapability("bstack:options", bstackOptions);

        // Connect to BrowserStack
        driver = new RemoteWebDriver(new URL(BROWSERSTACK_URL), capabilities);

        try {
            // Run the scraper on BrowserStack
            ElPaisScraper scraper = new ElPaisScraper();
            scraper.setUp(driver);
            List<Article> articles = scraper.scrapeOpinionArticles();
            scraper.printArticles();

            // Translate titles
            List<String> translatedTitles = TranslationService.translateTitles(articles);

            // Analyze headers
            HeaderAnalyzer.analyzeHeaders(translatedTitles);

            // Mark test as passed on BrowserStack
            markTestStatus("passed", "Successfully scraped " + articles.size() + " articles");

            System.out.println("\nTEST PASSED: " + testLabel);

        } catch (Exception e) {
            markTestStatus("failed", e.getMessage());
            System.out.println("\nTEST FAILED: " + testLabel + " - " + e.getMessage());
            throw e;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private void markTestStatus(String status, String reason) {
        try {
            String safeReason = reason != null ? reason.replaceAll("[\"\\\\]", " ").substring(0, Math.min(reason.length(), 200)) : "No reason";
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"" + status + "\", \"reason\": \"" + safeReason + "\"}}");
        } catch (Exception e) {
            System.out.println("Could not set BrowserStack test status: " + e.getMessage());
        }
    }
}
