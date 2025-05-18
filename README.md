# Game Rental System

A Java-based game rental management system that allows users to rent games, return them, and administrators to manage the game inventory.

## Prerequisites

- Java JDK 11 or higher
- Maven (for dependency management)

## Setup Instructions

1. Clone or download this repository to your local machine

2. Make sure you have Java JDK installed

   - To check, open a terminal/command prompt and run:

   ```
   java -version
   ```

   - If Java is not installed, download and install it from [Oracle's website](https://www.oracle.com/java/technologies/downloads/) or use your system's package manager

3. Make sure you have Maven installed
   - To check, open a terminal/command prompt and run:
   ```
   mvn -version
   ```
   - If Maven is not installed, download it from [Maven's website](https://maven.apache.org/download.cgi) or use your system's package manager

## Running the Application

1. Open a terminal/command prompt
2. Navigate to the project directory
3. Build the project with Maven:
   ```
   mvn clean package
   ```
4. Run the application:
   ```
   java -jar target/game-rental-system-1.0.jar
   ```

## Features

- User Management
  - Register new users (both admin and regular users)
  - Login system
- Game Management (Admin)
  - Add new games to the system
  - View all games in inventory
  - View all rental records
- Rental Management (Users)
  - View available games
  - Rent games
  - Return games
  - View personal rental history

## Database

The application uses H2 database which is automatically initialized on first run. All data is stored in a local file `gamerentaldb.mv.db` in the application directory.
