@echo off
echo Compiling Java files...
javac -d target/classes -cp "lib/*" src/main/java/com/rental/model/*.java src/main/java/com/rental/db/*.java src/main/java/com/rental/dao/*.java src/main/java/com/rental/GameRentalSystem.java

if %ERRORLEVEL% EQU 0 (
    echo Running the application...
    java -cp "target/classes;lib/*" com.rental.GameRentalSystem
) else (
    echo Compilation failed!
    pause
) 