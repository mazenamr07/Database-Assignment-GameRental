package com.rental.model;

public class Game {
    private int id;
    private String title;
    private int releaseYear;
    private String category;
    private String vendor;
    private boolean isAvailable;

    public Game(int id, String title, int releaseYear, String category, String vendor) {
        this.id = id;
        this.title = title;
        this.releaseYear = releaseYear;
        this.category = category;
        this.vendor = vendor;
        this.isAvailable = true;
    }

    public Game(String title, int releaseYear, String category, String vendor) {
        this(0, title, releaseYear, category, vendor);
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    @Override
    public String toString() {
        return String.format("ID: %d | Title: %s | Year: %d | Category: %s | Vendor: %s | Available: %s",
                id, title, releaseYear, category, vendor, isAvailable ? "Yes" : "No");
    }
} 