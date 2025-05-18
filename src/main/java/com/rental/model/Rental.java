package com.rental.model;

import java.time.LocalDateTime;

public class Rental {
    private int id;
    private int userId;
    private int gameId;
    private User user;
    private Game game;
    private LocalDateTime rentalDate;
    private LocalDateTime returnDate;
    private boolean returned;

    public Rental(int id, int userId, int gameId, LocalDateTime rentalDate) {
        this.id = id;
        this.userId = userId;
        this.gameId = gameId;
        this.rentalDate = rentalDate;
        this.returnDate = null;
        this.returned = false;
    }

    public Rental(User user, Game game, LocalDateTime rentalDate) {
        this.id = 0;
        this.user = user;
        this.userId = user.getId();
        this.game = game;
        this.gameId = game.getId();
        this.rentalDate = rentalDate;
        this.returnDate = null;
        this.returned = false;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        this.userId = user.getId();
    }

    public Game getGame() { return game; }
    public void setGame(Game game) {
        this.game = game;
        this.gameId = game.getId();
    }

    public LocalDateTime getRentalDate() { return rentalDate; }
    public void setRentalDate(LocalDateTime rentalDate) { this.rentalDate = rentalDate; }

    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }

    public boolean isReturned() { return returned; }
    public void setReturned(boolean returned) { this.returned = returned; }

    @Override
    public String toString() {
        String status = returned ? "Returned" : "Active";
        String userName = user != null ? user.getName() : "User#" + userId;
        String gameTitle = game != null ? game.getTitle() : "Game#" + gameId;
        return String.format("Rental ID: %d | User: %s | Game: %s | Status: %s",
                id, userName, gameTitle, status);
    }
} 