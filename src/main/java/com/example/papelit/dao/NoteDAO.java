package com.example.papelit.dao;

import com.example.papelit.model.Note;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO {
    public void insertNote(Note note) {
        String sql = "INSERT INTO notes (title, content, last_modified, x, y, width, height, is_open, always_on_top, color, tag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, note.getTitle());
            pstmt.setString(2, note.getContent());
            pstmt.setString(3, note.getLastModified().toString());
            pstmt.setDouble(4, note.getX());
            pstmt.setDouble(5, note.getY());
            pstmt.setDouble(6, note.getWidth());
            pstmt.setDouble(7, note.getHeight());
            pstmt.setBoolean(8, note.isOpen());
            pstmt.setBoolean(9, note.isAlwaysOnTop());
            pstmt.setString(10, note.getColor());
            pstmt.setString(11, note.getTag());
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) note.setId(rs.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateNote(Note note) {
        String sql = "UPDATE notes SET title=?, content=?, last_modified=?, x=?, y=?, width=?, height=?, is_open=?, always_on_top=?, color=?, tag=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, note.getTitle());
            pstmt.setString(2, note.getContent());
            pstmt.setString(3, note.getLastModified().toString());
            pstmt.setDouble(4, note.getX());
            pstmt.setDouble(5, note.getY());
            pstmt.setDouble(6, note.getWidth());
            pstmt.setDouble(7, note.getHeight());
            pstmt.setBoolean(8, note.isOpen());
            pstmt.setBoolean(9, note.isAlwaysOnTop());
            pstmt.setString(10, note.getColor());
            pstmt.setString(11, note.getTag());
            pstmt.setInt(12, note.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteNote(int id) {
        String sql = "DELETE FROM notes WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes ORDER BY last_modified DESC";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Note note = new Note();
                note.setId(rs.getInt("id"));
                note.setTitle(rs.getString("title"));
                note.setContent(rs.getString("content"));
                note.setLastModified(LocalDateTime.parse(rs.getString("last_modified")));
                note.setX(rs.getDouble("x"));
                note.setY(rs.getDouble("y"));
                note.setWidth(rs.getDouble("width"));
                note.setHeight(rs.getDouble("height"));
                note.setOpen(rs.getBoolean("is_open"));
                note.setAlwaysOnTop(rs.getBoolean("always_on_top"));
                note.setColor(rs.getString("color"));
                note.setTag(rs.getString("tag") != null ? rs.getString("tag") : "Home");
                notes.add(note);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return notes;
    }
}
