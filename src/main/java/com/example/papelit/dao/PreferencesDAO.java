package com.example.papelit.dao;

import com.example.papelit.model.UserPreferences;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PreferencesDAO {
    
    public UserPreferences getPreferences() {
        UserPreferences prefs = new UserPreferences();
        String sql = "SELECT * FROM preferences WHERE id = 1";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            if (rs.next()) {
                prefs.setTheme(rs.getString("theme"));
                prefs.setAutoSaveInterval(rs.getInt("auto_save_interval"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prefs;
    }
    
    public void savePreferences(UserPreferences prefs) {
        String sql = "UPDATE preferences SET theme = ?, auto_save_interval = ? WHERE id = 1";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, prefs.getTheme());
            pstmt.setInt(2, prefs.getAutoSaveInterval());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
