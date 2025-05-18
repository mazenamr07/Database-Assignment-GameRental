package com.rental.dao;

import com.rental.db.Database;
import com.rental.model.Rental;
import com.rental.model.Game;
import com.rental.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class RentalDAO {
    public static void createRental(Rental rental) throws SQLException {
        String sql = "INSERT INTO rentals (user_id, game_id, rental_date, return_date, returned) VALUES (?, ?, CURRENT_TIMESTAMP, NULL, false)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, rental.getUser().getId());
            pstmt.setInt(2, rental.getGame().getId());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    rental.setId(generatedKeys.getInt(1));
                }
            }
            
            // Update game availability
            GameDAO.updateGameAvailability(rental.getGame().getId(), false);
        }
    }
    
    public static List<Rental> getAllRentals() throws SQLException {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT r.*, u.name as user_name, u.email, u.is_admin, " +
                    "g.title, g.release_year, g.category, g.vendor " +
                    "FROM rentals r " +
                    "JOIN users u ON r.user_id = u.id " +
                    "JOIN games g ON r.game_id = g.id " +
                    "ORDER BY r.rental_date DESC";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                rentals.add(createRentalFromResultSet(rs));
            }
        }
        return rentals;
    }
    
    public static List<Rental> getUserRentals(int userId) throws SQLException {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT r.*, u.name as user_name, u.email, u.is_admin, " +
                    "g.title, g.release_year, g.category, g.vendor " +
                    "FROM rentals r " +
                    "JOIN users u ON r.user_id = u.id " +
                    "JOIN games g ON r.game_id = g.id " +
                    "WHERE r.user_id = ? " +
                    "ORDER BY r.rental_date DESC";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rentals.add(createRentalFromResultSet(rs));
                }
            }
        }
        return rentals;
    }
    
    public static void returnGame(int rentalId) throws SQLException {
        Connection conn = Database.getConnection();
        try {
            conn.setAutoCommit(false);
            
            // First check if the rental exists and hasn't been returned
            String checkRentalSql = "SELECT game_id, returned, return_date FROM rentals WHERE id = ?";
            int gameId;
            try (PreparedStatement pstmt = conn.prepareStatement(checkRentalSql)) {
                pstmt.setInt(1, rentalId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Rental not found");
                    }
                    if (rs.getBoolean("returned") || rs.getTimestamp("return_date") != null) {
                        throw new SQLException("Rental has already been returned");
                    }
                    gameId = rs.getInt("game_id");
                }
            }
            
            // Update the rental record with both returned flag and return_date
            String updateRentalSql = "UPDATE rentals SET returned = true, return_date = CURRENT_TIMESTAMP WHERE id = ? AND returned = false AND return_date IS NULL";
            try (PreparedStatement pstmt = conn.prepareStatement(updateRentalSql)) {
                pstmt.setInt(1, rentalId);
                int updatedRows = pstmt.executeUpdate();
                if (updatedRows == 0) {
                    throw new SQLException("Failed to update rental status");
                }
            }
            
            // Update game availability
            GameDAO.updateGameAvailability(gameId, true);
            
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
    
    public static void validateRentalStatuses() throws SQLException {
        String sql = "UPDATE rentals " +
                    "SET returned = CASE " +
                    "    WHEN return_date IS NOT NULL THEN true " +
                    "    WHEN return_date IS NULL THEN false " +
                    "END " +
                    "WHERE (returned = true AND return_date IS NULL) " +
                    "   OR (returned = false AND return_date IS NOT NULL)";
                    
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            int updatedRows = stmt.executeUpdate(sql);
            if (updatedRows > 0) {
                System.out.println("Fixed " + updatedRows + " inconsistent rental status records.");
            }
        }
    }
    
    private static Rental createRentalFromResultSet(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getString("user_name"),
            rs.getString("email"),
            "",  // We don't retrieve password
            rs.getBoolean("is_admin")
        );
        user.setId(rs.getInt("user_id"));
        
        Game game = new Game(
            rs.getString("title"),
            rs.getInt("release_year"),
            rs.getString("category"),
            rs.getString("vendor")
        );
        game.setId(rs.getInt("game_id"));
        
        Rental rental = new Rental(
            user,
            game,
            rs.getTimestamp("rental_date").toLocalDateTime()
        );
        rental.setId(rs.getInt("id"));
        rental.setReturned(rs.getBoolean("returned"));
        
        Timestamp returnDate = rs.getTimestamp("return_date");
        if (returnDate != null) {
            rental.setReturnDate(returnDate.toLocalDateTime());
        }
        
        return rental;
    }
} 