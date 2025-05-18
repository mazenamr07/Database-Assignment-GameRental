package com.rental.db;

import java.sql.*;
import java.io.File;

public class Database {
    private static final String DB_NAME = "gamerentaldb";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static String URL;

    static {
        String userHome = System.getProperty("user.home");
        File dbDir = new File(userHome, "gamerentaldb");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        URL = "jdbc:h2:" + new File(dbDir, DB_NAME).getAbsolutePath() + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
        System.out.println("Database URL: " + URL);
    }

    public static void initializeDatabase() {
        System.out.println("Initializing database...");
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        
        try (Connection conn = getConnection()) {
            System.out.println("Connected to database successfully");
            // Start transaction
            conn.setAutoCommit(false);
            try {
                System.out.println("Creating tables...");
                createTables(conn);
                System.out.println("Inserting sample data...");
                insertSampleData(conn);
                conn.commit();
                System.out.println("Database initialization completed successfully");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error initializing database: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Connection getConnection() throws SQLException {
        // Try to load the H2 driver explicitly
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("H2 JDBC Driver not found.");
            e.printStackTrace();
            System.exit(1);
        }
        
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static void insertSampleData(Connection conn) throws SQLException {
        // Check if we already have games in the database
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM games")) {
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("Sample data already exists");
                return; // Database already has games
            }
        }

        System.out.println("Inserting sample games...");
        // Insert sample games
        String insertGame = "INSERT INTO games (title, release_year, category, vendor, available) VALUES (?, ?, ?, ?, true)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertGame)) {
            // Game 1
            pstmt.setString(1, "The Legend of Zelda: Breath of the Wild");
            pstmt.setInt(2, 2017);
            pstmt.setString(3, "Action-Adventure");
            pstmt.setString(4, "Nintendo");
            pstmt.executeUpdate();

            // Game 2
            pstmt.setString(1, "Red Dead Redemption 2");
            pstmt.setInt(2, 2018);
            pstmt.setString(3, "Action");
            pstmt.setString(4, "Rockstar Games");
            pstmt.executeUpdate();

            // Game 3
            pstmt.setString(1, "FIFA 23");
            pstmt.setInt(2, 2022);
            pstmt.setString(3, "Sports");
            pstmt.setString(4, "EA Sports");
            pstmt.executeUpdate();

            // Game 4
            pstmt.setString(1, "Minecraft");
            pstmt.setInt(2, 2011);
            pstmt.setString(3, "Sandbox");
            pstmt.setString(4, "Mojang");
            pstmt.executeUpdate();

            // Game 5
            pstmt.setString(1, "Cyberpunk 2077");
            pstmt.setInt(2, 2020);
            pstmt.setString(3, "RPG");
            pstmt.setString(4, "CD Projekt Red");
            pstmt.executeUpdate();
        }
        System.out.println("Sample games inserted successfully");
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Check if we need to migrate old tables
            boolean needsMigration = false;
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "GAMES", null)) {
                if (rs.next()) {
                    // Check if it's the old schema
                    try (ResultSet columns = conn.getMetaData().getColumns(null, null, "GAMES", "IS_AVAILABLE")) {
                        if (columns.next()) {
                            needsMigration = true;
                        }
                    }
                }
            }

            if (needsMigration) {
                System.out.println("Migrating old tables...");
                // Rename existing tables
                stmt.execute("ALTER TABLE games RENAME TO old_games");
                stmt.execute("ALTER TABLE rentals RENAME TO old_rentals");
            }

            System.out.println("Creating users table...");
            // Create Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(100) NOT NULL,
                    is_admin BOOLEAN NOT NULL
                )
            """);

            System.out.println("Creating games table...");
            // Create Games table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS games (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(100) NOT NULL,
                    release_year INT NOT NULL,
                    category VARCHAR(50) NOT NULL,
                    vendor VARCHAR(100) NOT NULL,
                    available BOOLEAN NOT NULL DEFAULT true
                )
            """);

            System.out.println("Creating rentals table...");
            // Create Rentals table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS rentals (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    game_id INT NOT NULL,
                    rental_date TIMESTAMP NOT NULL,
                    return_date TIMESTAMP,
                    returned BOOLEAN NOT NULL DEFAULT false,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (game_id) REFERENCES games(id)
                )
            """);

            // Migrate data if needed
            if (needsMigration) {
                try {
                    System.out.println("Migrating data from old tables...");
                    // Copy data from old tables to new ones
                    stmt.execute("""
                        INSERT INTO games (id, title, release_year, category, vendor, available)
                        SELECT id, title, release_year, category, vendor, is_available
                        FROM old_games
                    """);
                    
                    stmt.execute("""
                        INSERT INTO rentals (id, user_id, game_id, rental_date, return_date, returned)
                        SELECT id, user_id, game_id, rental_date, return_date,
                               CASE WHEN return_date IS NOT NULL THEN true ELSE false END
                        FROM old_rentals
                    """);
                } finally {
                    // Drop old tables
                    stmt.execute("DROP TABLE IF EXISTS old_rentals");
                    stmt.execute("DROP TABLE IF EXISTS old_games");
                }
            }
        }
    }
} 