# Software-Engineering
# Booktracker Application

## Overview

The Booktracker Application is a Java program that helps manage and analyze reading habits data. It imports data from an Excel file into an SQLite database and provides various functionalities to interact with this data.

## Prerequisites

- Java 23 (as specified in pom.xml)
- Maven (for dependency management)
- SQLite JDBC driver (included in dependencies)
- Apache POI libraries (included in dependencies)
- Excel file named `reading_habits_dataset.xlsx` in the resources folder

## Installation

1. Clone the repository or download the source files
2. Ensure you have the Excel file in the correct location (`src/main/resources/reading_habits_dataset.xlsx`)
3. Build the project using Maven:
   ```
   mvn clean install
   ```

## Running the Application

Execute the application using the following command:
```
mvn exec:java -Dexec.mainClass="org.example.Main"
```

Alternatively, you can run the `Main` class directly from your IDE.

## Functionalities

The application provides the following menu options:

### 1. Add a new user
- Prompts for age, gender, and name
- Adds a new user to the database

### 2. View reading habits for a user
- Enter a user ID to view all their reading habits
- Displays habit ID, pages read, book title, and submission date

### 3. Update a book title
- Enter the current book title and the new title
- Updates all occurrences of that book title in the database

### 4. Delete a reading habit
- Enter a habit ID to delete
- Removes the specified reading habit from the database

### 5. View mean age of users
- Calculates and displays the average age of all users

### 6. View number of readers for a book
- Enter a book title (or partial title)
- Shows how many distinct users have read that book

### 7. View total pages read by all users
- Calculates and displays the sum of all pages read by all users

### 8. View users who read multiple books
- Shows the count of users who have read more than one distinct book

### 9. Exit
- Closes the application

## Data Structure

The application uses two main tables:

1. **User** table:
   - userID (primary key)
   - age
   - gender
   - Name

2. **ReadingHabit** table:
   - habitID (primary key)
   - userID (foreign key to User)
   - pagesRead
   - book (title)
   - submissionMoment

## Notes

- The application automatically initializes the database and imports data from the Excel file on startup
- The Excel file should have two sheets:
  - Sheet 0: Reading habits data
  - Sheet 1: User data
- All database operations are performed on an SQLite database file named `booktracker.db` that will be created in the project directory

## Troubleshooting

- If you get "Excel file not found" errors, ensure the Excel file is in the correct location
- If you encounter database issues, delete the `booktracker.db` file and restart the application to recreate it
- Make sure all required dependencies are properly downloaded by Maven
