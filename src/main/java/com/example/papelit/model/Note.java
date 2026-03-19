package com.example.papelit.model;

import java.time.LocalDateTime;

public class Note {
    private int id;
    private String title;
    private String content;
    private LocalDateTime lastModified;
    private double x;
    private double y;
    private double width;
    private double height;
    private boolean isOpen;
    private boolean alwaysOnTop;
    private String color;
    private String tag; // e.g., "Home", "Work", "Personal", "Ideas"

    public Note() {
        this.title = "New Note";
        this.content = "";
        this.lastModified = LocalDateTime.now();
        this.x = 100;
        this.y = 100;
        this.width = 300;
        this.height = 300;
        this.isOpen = true;
        this.alwaysOnTop = false;
        this.color = "#FFD166";
        this.tag = "Home"; // Default tag
    }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }
    public boolean isAlwaysOnTop() { return alwaysOnTop; }
    public void setAlwaysOnTop(boolean alwaysOnTop) { this.alwaysOnTop = alwaysOnTop; }
}
