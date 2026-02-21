# El Pais Opinion Scraper - BrowserStack Assignment

A Selenium-based Java project that scrapes articles from the **Opinion** section of [El País](https://elpais.com/), translates article titles from Spanish to English, analyzes word frequency across translated headers, and executes cross-browser testing on **BrowserStack**.

## Features

1. **Web Scraping** — Navigates to El País, verifies the site is in Spanish, and scrapes the first 5 Opinion articles (title, content, and cover image).
2. **Translation** — Translates article titles from Spanish to English using the MyMemory Translation API.
3. **Header Analysis** — Identifies words repeated more than twice across all translated headers.
4. **Cross-Browser Testing** — Runs the solution on BrowserStack with 5 parallel threads across desktop and mobile browsers.

## Tech Stack

- **Language**: Java 17
- **Build Tool**: Maven
- **Web Automation**: Selenium WebDriver 4.27
- **Driver Management**: WebDriverManager
- **Translation API**: MyMemory Translation API
- **Test Framework**: TestNG (parallel execution)
- **Cross-Browser**: BrowserStack Automate

## Project Structure

```
ElPaisScraper/
├── pom.xml                                    # Maven configuration
├── testng.xml                                 # TestNG suite config (5 parallel threads)
├── README.md
├── .gitignore
└── src/
    ├── main/java/com/browserstack/
    │   ├── ElPaisScraper.java                 # Main scraper - scrapes El Pais Opinion articles
    │   ├── Article.java                       # Article data model
    │   ├── TranslationService.java            # Spanish to English translation via API
    │   └── HeaderAnalyzer.java                # Word frequency analysis on translated headers
    └── test/java/com/browserstack/
        └── BrowserStackTest.java              # BrowserStack cross-browser test (5 parallel)
```

## Browser Configurations (BrowserStack)

| # | Type    | Browser  | OS / Device            |
|---|---------|----------|------------------------|
| 1 | Desktop | Chrome   | Windows 11             |
| 2 | Desktop | Firefox  | macOS Sonoma           |
| 3 | Desktop | Edge     | Windows 10             |
| 4 | Mobile  | Safari   | iPhone 15 (iOS 17)     |
| 5 | Mobile  | Samsung  | Galaxy S23 (Android 13)|

## Prerequisites

- Java 17+
- Maven 3.9+
- Google Chrome (for local execution)
- BrowserStack account (for cross-browser testing)

## How to Run

### Run Locally
```bash
mvn compile exec:java -Dexec.mainClass="com.browserstack.ElPaisScraper"
```

### Run on BrowserStack (5 Parallel Threads)
Update your BrowserStack credentials in `BrowserStackTest.java`, then:
```bash
mvn test
```

## Output

- Article titles and content printed in Spanish
- Translated titles printed in English
- Repeated words (if any) printed with occurrence counts
- Cover images saved to `images/` directory
