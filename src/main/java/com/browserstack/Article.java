package com.browserstack;

public class Article {
    private String title;
    private String content;
    private String imageUrl;
    private String savedImagePath;

    public Article(String title, String content, String imageUrl, String savedImagePath) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.savedImagePath = savedImagePath;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSavedImagePath() {
        return savedImagePath;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Article{title='" + title + "'}";
    }
}
