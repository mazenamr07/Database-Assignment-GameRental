package com.rental;

import com.rental.db.Database;
import com.rental.model.User;
import com.rental.model.Game;
import com.rental.model.Rental;
import com.rental.dao.UserDAO;
import com.rental.dao.GameDAO;
import com.rental.dao.RentalDAO;
import java.util.Scanner;
import java.sql.SQLException;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class GameRentalSystem {
    private static Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;

    public static void main(String[] args) {
        // Initialize database
        Database.initializeDatabase();
        
        try {
            // Validate rental statuses on startup
            RentalDAO.validateRentalStatuses();
        } catch (SQLException e) {
            System.out.println("Warning: Could not validate rental statuses: " + e.getMessage());
        }
        
        while (true) {
            if (currentUser == null) {
                showMainMenu();
            } else if (currentUser.isAdmin()) {
                showAdminMenu();
            } else {
                showClientMenu();
            }
        }
    }

    private static void showMainMenu() {
        System.out.println("\n=== Game Rental System ===");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                register();
                break;
            case 3:
                System.out.println("Thank you for using Game Rental System!");
                System.exit(0);
            default:
                System.out.println("Invalid option!");
        }
    }

    private static void showAdminMenu() {
        System.out.println("\n=== Admin Menu ===");
        System.out.println("1. Add Game");
        System.out.println("2. View All Games");
        System.out.println("3. View All Rentals");
        System.out.println("4. Logout");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                addGame();
                break;
            case 2:
                viewAllGames();
                break;
            case 3:
                viewAllRentals();
                break;
            case 4:
                logout();
                break;
            default:
                System.out.println("Invalid option!");
        }
    }

    private static void showClientMenu() {
        System.out.println("\n=== Client Menu ===");
        System.out.println("1. View Available Games");
        System.out.println("2. Rent a Game");
        System.out.println("3. Return a Game");
        System.out.println("4. View My Rentals");
        System.out.println("5. Logout");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                viewAvailableGames();
                break;
            case 2:
                rentGame();
                break;
            case 3:
                returnGame();
                break;
            case 4:
                viewMyRentals();
                break;
            case 5:
                logout();
                break;
            default:
                System.out.println("Invalid option!");
        }
    }

    private static void login() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            User user = UserDAO.login(email, password);
            if (user != null) {
                currentUser = user;
                System.out.println("Login successful! Welcome, " + user.getName());
            } else {
                System.out.println("Invalid email or password!");
            }
        } catch (SQLException e) {
            System.out.println("Error during login: " + e.getMessage());
        }
    }

    private static void register() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        
        String email;
        while (true) {
            System.out.print("Enter email: ");
            email = scanner.nextLine();
            try {
                if (UserDAO.emailExists(email)) {
                    System.out.println("Email already exists! Please choose another.");
                    continue;
                }
                break;
            } catch (SQLException e) {
                System.out.println("Error checking email: " + e.getMessage());
                return;
            }
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Are you an admin? (yes/no): ");
        boolean isAdmin = scanner.nextLine().toLowerCase().startsWith("y");

        try {
            User user = UserDAO.register(name, email, password, isAdmin);
            System.out.println("Registration successful! Please login.");
        } catch (SQLException e) {
            System.out.println("Error during registration: " + e.getMessage());
        }
    }

    private static void addGame() {
        System.out.print("Enter game title: ");
        String title = scanner.nextLine();
        System.out.print("Enter release year: ");
        int releaseYear = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter category: ");
        String category = scanner.nextLine();
        System.out.print("Enter vendor: ");
        String vendor = scanner.nextLine();

        try {
            Game game = new Game(title, releaseYear, category, vendor);
            GameDAO.createGame(game);
            System.out.println("Game added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding game: " + e.getMessage());
        }
    }

    private static void viewAllGames() {
        try {
            List<Game> games = GameDAO.getAllGames();
            if (games.isEmpty()) {
                System.out.println("No games found in the system.");
                return;
            }

            System.out.println("\n=== All Games ===");
            System.out.printf("%-5s %-30s %-6s %-15s %-20s %-10s%n", 
                            "ID", "Title", "Year", "Category", "Vendor", "Available");
            System.out.println("-".repeat(90));
            
            for (Game game : games) {
                System.out.printf("%-5d %-30s %-6d %-15s %-20s %-10s%n",
                    game.getId(),
                    game.getTitle(),
                    game.getReleaseYear(),
                    game.getCategory(),
                    game.getVendor(),
                    game.isAvailable() ? "Yes" : "No"
                );
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving games: " + e.getMessage());
        }
    }

    private static void viewAvailableGames() {
        try {
            List<Game> games = GameDAO.getAvailableGames();
            if (games.isEmpty()) {
                System.out.println("No games available for rent at the moment.");
                return;
            }

            System.out.println("\n=== Available Games ===");
            System.out.printf("%-5s %-30s %-6s %-15s %-20s%n", 
                            "ID", "Title", "Year", "Category", "Vendor");
            System.out.println("-".repeat(80));
            
            for (Game game : games) {
                System.out.printf("%-5d %-30s %-6d %-15s %-20s%n",
                    game.getId(),
                    game.getTitle(),
                    game.getReleaseYear(),
                    game.getCategory(),
                    game.getVendor()
                );
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving available games: " + e.getMessage());
        }
    }

    private static void viewAllRentals() {
        try {
            List<Rental> rentals = RentalDAO.getAllRentals();
            if (rentals.isEmpty()) {
                System.out.println("No rentals found in the system.");
                return;
            }

            // Split rentals into active and returned
            List<Rental> activeRentals = new ArrayList<>();
            List<Rental> returnedRentals = new ArrayList<>();
            
            for (Rental rental : rentals) {
                if (rental.isReturned()) {
                    returnedRentals.add(rental);
                } else {
                    activeRentals.add(rental);
                }
            }

            // Display active rentals
            System.out.println("\n=== Active Rentals ===");
            if (!activeRentals.isEmpty()) {
                System.out.printf("%-5s %-20s %-30s %-15s%n",
                                "ID", "User", "Game", "Rental Date");
                System.out.println("-".repeat(75));
                
                for (Rental rental : activeRentals) {
                    System.out.printf("%-5d %-20s %-30s %-15s%n",
                        rental.getId(),
                        rental.getUser().getName(),
                        rental.getGame().getTitle(),
                        rental.getRentalDate().toLocalDate()
                    );
                }
            } else {
                System.out.println("No active rentals.");
            }

            // Display returned rentals
            System.out.println("\n=== Returned Rentals ===");
            if (!returnedRentals.isEmpty()) {
                System.out.printf("%-5s %-20s %-30s %-15s %-15s%n",
                                "ID", "User", "Game", "Rental Date", "Return Date");
                System.out.println("-".repeat(90));
                
                for (Rental rental : returnedRentals) {
                    System.out.printf("%-5d %-20s %-30s %-15s %-15s%n",
                        rental.getId(),
                        rental.getUser().getName(),
                        rental.getGame().getTitle(),
                        rental.getRentalDate().toLocalDate(),
                        rental.getReturnDate().toLocalDate()
                    );
                }
            } else {
                System.out.println("No returned rentals.");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving rentals: " + e.getMessage());
        }
    }

    private static void viewMyRentals() {
        try {
            List<Rental> rentals = RentalDAO.getUserRentals(currentUser.getId());
            if (rentals.isEmpty()) {
                System.out.println("You have no rental history.");
                return;
            }

            // Split rentals into active and returned
            List<Rental> activeRentals = new ArrayList<>();
            List<Rental> returnedRentals = new ArrayList<>();
            
            for (Rental rental : rentals) {
                if (rental.isReturned()) {
                    returnedRentals.add(rental);
                } else {
                    activeRentals.add(rental);
                }
            }

            // Display active rentals
            if (!activeRentals.isEmpty()) {
                System.out.println("\n=== Your Active Rentals ===");
                System.out.printf("%-5s %-30s %-15s%n",
                                "ID", "Game", "Rental Date");
                System.out.println("-".repeat(55));
                
                for (Rental rental : activeRentals) {
                    System.out.printf("%-5d %-30s %-15s%n",
                        rental.getId(),
                        rental.getGame().getTitle(),
                        rental.getRentalDate().toLocalDate()
                    );
                }
            } else {
                System.out.println("\nYou have no active rentals.");
            }

            // Display returned rentals
            if (!returnedRentals.isEmpty()) {
                System.out.println("\n=== Your Returned Rentals ===");
                System.out.printf("%-5s %-30s %-15s %-15s%n",
                                "ID", "Game", "Rental Date", "Return Date");
                System.out.println("-".repeat(70));
                
                for (Rental rental : returnedRentals) {
                    System.out.printf("%-5d %-30s %-15s %-15s%n",
                        rental.getId(),
                        rental.getGame().getTitle(),
                        rental.getRentalDate().toLocalDate(),
                        rental.getReturnDate().toLocalDate()
                    );
                }
            } else {
                System.out.println("\nYou have no returned rentals.");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving your rentals: " + e.getMessage());
        }
    }

    private static void rentGame() {
        viewAvailableGames();
        
        System.out.print("\nEnter the ID of the game you want to rent (0 to cancel): ");
        int gameId = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        if (gameId == 0) {
            System.out.println("Rental cancelled.");
            return;
        }
        
        try {
            Game game = GameDAO.getGameById(gameId);
            if (game == null) {
                System.out.println("Game not found.");
                return;
            }
            
            if (!game.isAvailable()) {
                System.out.println("This game is not available for rent.");
                return;
            }
            
            Rental rental = new Rental(currentUser, game, LocalDateTime.now());
            RentalDAO.createRental(rental);
            System.out.println("Game rented successfully! Enjoy playing " + game.getTitle() + "!");
            
        } catch (SQLException e) {
            System.out.println("Error renting game: " + e.getMessage());
        }
    }

    private static void returnGame() {
        try {
            List<Rental> activeRentals = RentalDAO.getUserRentals(currentUser.getId());
            activeRentals.removeIf(Rental::isReturned); // Filter out already returned games
            
            if (activeRentals.isEmpty()) {
                System.out.println("You have no active rentals to return.");
                return;
            }

            System.out.println("\n=== Your Active Rentals ===");
            System.out.printf("%-5s %-30s %-15s%n",
                            "ID", "Game", "Rental Date");
            System.out.println("-".repeat(55));
            
            for (Rental rental : activeRentals) {
                System.out.printf("%-5d %-30s %-15s%n",
                    rental.getId(),
                    rental.getGame().getTitle(),
                    rental.getRentalDate().toLocalDate()
                );
            }
            
            System.out.print("\nEnter the ID of the rental you want to return (0 to cancel): ");
            int rentalId = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            if (rentalId == 0) {
                System.out.println("Return cancelled.");
                return;
            }
            
            // Verify the rental belongs to the current user
            boolean validRental = activeRentals.stream()
                .anyMatch(r -> r.getId() == rentalId);
                
            if (!validRental) {
                System.out.println("Invalid rental ID.");
                return;
            }
            
            RentalDAO.returnGame(rentalId);
            System.out.println("Game returned successfully!");
            
            // Show updated rental list
            System.out.println("\nYour updated rental list:");
            viewMyRentals();
            
        } catch (SQLException e) {
            System.out.println("Error returning game: " + e.getMessage());
        }
    }

    private static void logout() {
        currentUser = null;
        System.out.println("Logged out successfully!");
    }
} 