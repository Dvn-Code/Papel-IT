package com.example.papelit.model;

public class UserPreferences {
    private String theme;
    private int autoSaveInterval;

    public UserPreferences() {
        this.theme = "light";
        this.autoSaveInterval = 30;
    }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public int getAutoSaveInterval() { return autoSaveInterval; }
    public void setAutoSaveInterval(int autoSaveInterval) { this.autoSaveInterval = autoSaveInterval; }
}
