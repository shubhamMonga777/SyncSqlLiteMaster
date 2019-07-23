package com.example.syncmaster;

public class NewsModel {

    String newsSource, title, image, Description,time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public NewsModel() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNewsSource(String newsSource) {
        this.newsSource = newsSource;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setDescription(String description) {
        Description = description;
    }

    int id;

    public String getNewsSource() {
        return newsSource;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return Description;
    }

    public NewsModel(String newsSource, String title, String image, String description, String time) {
        this.newsSource = newsSource;
        this.title = title;
        this.image = image;
        Description = description;
        this.time = time;
    }
}
