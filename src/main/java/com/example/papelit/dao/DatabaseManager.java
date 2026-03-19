package com.example.papelit.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String APP_DIR = System.getProperty("user.home") + File.separator + ".papelit";
    private static final String DB_URL = "jdbc:sqlite:" + APP_DIR + File.separator + "papelit.db";

    public static void initializeDatabase() {
        File dir = new File(APP_DIR);
        if (!dir.exists()) dir.mkdirs();

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Notes table
            stmt.execute("CREATE TABLE IF NOT EXISTS notes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT, " +
                    "content TEXT, " +
                    "last_modified DATETIME, " +
                    "x DOUBLE, " +
                    "y DOUBLE, " +
                    "width DOUBLE, " +
                    "height DOUBLE, " +
                    "is_open BOOLEAN, " +
                    "always_on_top BOOLEAN, " +
                    "color TEXT, " +
                    "tag TEXT" + // Add tag column
                    ")");

            // Migration: Ensure 'tag' column exists for existing DBs
            try {
                stmt.execute("ALTER TABLE notes ADD COLUMN tag TEXT DEFAULT 'Home'");
            } catch (SQLException e) {
                // Column likely already exists
            }

            // Preferences table
            stmt.execute("CREATE TABLE IF NOT EXISTS preferences (" +
                    "id INTEGER PRIMARY KEY CHECK (id = 1), " +
                    "theme TEXT, " +
                    "auto_save_interval INTEGER" +
                    ")");

            // Initial preferences
            stmt.execute("INSERT OR IGNORE INTO preferences (id, theme, auto_save_interval) VALUES (1, 'light', 30)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
