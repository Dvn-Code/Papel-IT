package com.example.papelit.model;

public enum NoteColor {
    PINK("#FFC9C9"),
    PEACH("#FFD8A8"),
    GREEN("#B2F2BB"),
    BLUE("#D0EBFF"),
    PURPLE("#E5DBFF"),
    YELLOW("#FFF3BF"),
    GOLD("#FFD166"),
    CYAN("#A5F3FC");

    private final String hex;

    NoteColor(String hex) {
        this.hex = hex;
    }

    public String getHex() {
        return hex;
    }

    public String getCssClassName() {
        return "note-color-" + name().toLowerCase();
    }
    
    public static NoteColor fromHex(String hex) {
        for (NoteColor c : values()) {
            if (c.getHex().equalsIgnoreCase(hex)) return c;
        }
        return PEACH; // Default
    }
}
