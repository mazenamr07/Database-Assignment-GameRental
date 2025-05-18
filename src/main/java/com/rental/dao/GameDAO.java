package com.rental.dao;

import com.rental.db.Database;
import com.rental.model.Game;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {
    public static void createGame(Game game) throws SQLException {
        String sql = "INSERT INTO games (title, release_year, category, vendor, available) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, game.getTitle());
            pstmt.setInt(2, game.getReleaseYear());
            pstmt.setString(3, game.getCategory());
            pstmt.setString(4, game.getVendor());
            pstmt.setBoolean(5, true);
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    game.setId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    public static List<Game> getAllGames() throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Game game = new Game(
                    rs.getString("title"),
                    rs.getInt("release_year"),
                    rs.getString("category"),
                    rs.getString("vendor")
                );
                game.setId(rs.getInt("id"));
                game.setAvailable(rs.getBoolean("available"));
                games.add(game);
            }
        }
        return games;
    }
    
    public static List<Game> getAvailableGames() throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games WHERE available = true";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Game game = new Game(
                    rs.getString("title"),
                    rs.getInt("release_year"),
                    rs.getString("category"),
                    rs.getString("vendor")
                );
                game.setId(rs.getInt("id"));
                game.setAvailable(true);
                games.add(game);
            }
        }
        return games;
    }
    
    public static void updateGameAvailability(int gameId, boolean available) throws SQLException {
        String sql = "UPDATE games SET available = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, available);
            pstmt.setInt(2, gameId);
            pstmt.executeUpdate();
        }
    }
    
    public static Game getGameById(int id) throws SQLException {
        String sql = "SELECT * FROM games WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Game game = new Game(
                        rs.getString("title"),
                        rs.getInt("release_year"),
                        rs.getString("category"),
                        rs.getString("vendor")
                    );
                    game.setId(rs.getInt("id"));
                    game.setAvailable(rs.getBoolean("available"));
                    return game;
                }
            }
        }
        return null;
    }
} 