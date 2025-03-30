package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.sql.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class Main {
    private static final String DB_URL = "jdbc:sqlite:booktracker.db";
    private static final String EXCEL_FILE = "reading_habits_dataset.xlsx";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            initializeDatabase();
            importExcelData();

            System.out.println("\nBooktracker Application\n");
            displayMenu();

        } catch (SQLException | IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void displayMenu() throws SQLException {
        while (true) {
            System.out.println("\nMenu Options:");
            System.out.println("1. Add a new user");
            System.out.println("2. View reading habits for a user");
            System.out.println("3. Update a book title");
            System.out.println("4. Delete a reading habit");
            System.out.println("5. View mean age of users");
            System.out.println("6. View number of readers for a book");
            System.out.println("7. View total pages read by all users");
            System.out.println("8. View users who read multiple books");
            System.out.println("9. Exit");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    addUser();
                    break;
                case 2:
                    getUserHabits();
                    break;
                case 3:
                    updateBookTitle();
                    break;
                case 4:
                    deleteHabit();
                    break;
                case 5:
                    getMeanAge();
                    break;
                case 6:
                    getBookReaders();
                    break;
                case 7:
                    getTotalPagesRead();
                    break;
                case 8:
                    getMultiBookReaders();
                    break;
                case 9:
                    System.out.println("Exiting application...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void initializeDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS User (" +
                    "userID INTEGER PRIMARY KEY, " +
                    "age INTEGER, " +
                    "gender TEXT, " +
                    "Name TEXT DEFAULT 'Unknown')");

            stmt.execute("CREATE TABLE IF NOT EXISTS ReadingHabit (" +
                    "habitID INTEGER PRIMARY KEY, " +
                    "userID INTEGER, " +
                    "pagesRead INTEGER, " +
                    "book TEXT, " +
                    "submissionMoment TEXT, " +
                    "FOREIGN KEY(userID) REFERENCES User(userID))");
        }
    }

    private static void importExcelData() throws IOException, SQLException {
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(EXCEL_FILE);
        if (inputStream == null) {
            throw new IOException("Excel file not found in resources");
        }

        Workbook workbook = new XSSFWorkbook(inputStream);

        // Import User data
        Sheet userSheet = workbook.getSheetAt(1);
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR IGNORE INTO User (userID, age, gender) VALUES (?, ?, ?)")) {

            for (Row row : userSheet) {
                if (row.getRowNum() == 0) continue;

                int userID = (int) getNumericCellValue(row.getCell(0));
                int age = (int) getNumericCellValue(row.getCell(1));
                String gender = getStringCellValue(row.getCell(2));

                stmt.setInt(1, userID);
                stmt.setInt(2, age);
                stmt.setString(3, gender);
                stmt.executeUpdate();
            }
        }

        // Import ReadingHabit data
        Sheet habitSheet = workbook.getSheetAt(0);
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR IGNORE INTO ReadingHabit (habitID, userID, pagesRead, book, submissionMoment) VALUES (?, ?, ?, ?, ?)")) {

            for (Row row : habitSheet) {
                if (row.getRowNum() == 0) continue;

                int habitID = (int) getNumericCellValue(row.getCell(0));
                int userID = (int) getNumericCellValue(row.getCell(1));
                int pagesRead = (int) getNumericCellValue(row.getCell(2));
                String book = getStringCellValue(row.getCell(3));
                String submissionMoment = getStringCellValue(row.getCell(4));

                stmt.setInt(1, habitID);
                stmt.setInt(2, userID);
                stmt.setInt(3, pagesRead);
                stmt.setString(4, book);
                stmt.setString(5, submissionMoment);
                stmt.executeUpdate();
            }
        }

        workbook.close();
        inputStream.close();
        System.out.println("Excel data imported successfully");
    }

    // Helper methods for cell value handling
    private static String getStringCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    private static double getNumericCellValue(Cell cell) {
        if (cell == null) return 0;

        switch (cell.getCellType()) {
            case NUMERIC: return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            default: return 0;
        }
    }

    // Application functionality methods
    private static void addUser() throws SQLException {
        System.out.print("Enter age: ");
        int age = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter gender (m/f): ");
        String gender = scanner.nextLine();

        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO User (age, gender, Name) VALUES (?, ?, ?)")) {
            stmt.setInt(1, age);
            stmt.setString(2, gender);
            stmt.setString(3, name);
            stmt.executeUpdate();
            System.out.println("User added successfully.");
        }
    }

    private static void getUserHabits() throws SQLException {
        System.out.print("Enter user ID: ");
        int userID = scanner.nextInt();
        scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM ReadingHabit WHERE userID = ?")) {
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nReading habits for user " + userID + ":");
            System.out.printf("%-8s %-6s %-60s %s%n",
                    "HabitID", "Pages", "Book", "Submission Date");

            while (rs.next()) {
                System.out.printf("%-8d %-6d %-60s %s%n",
                        rs.getInt("habitID"),
                        rs.getInt("pagesRead"),
                        shortenString(rs.getString("book"), 55),
                        rs.getString("submissionMoment"));
            }
        }
    }

    private static void updateBookTitle() throws SQLException {
        System.out.print("Enter current book title: ");
        String oldTitle = scanner.nextLine();

        System.out.print("Enter new book title: ");
        String newTitle = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE ReadingHabit SET book = ? WHERE book = ?")) {
            stmt.setString(1, newTitle);
            stmt.setString(2, oldTitle);
            int count = stmt.executeUpdate();
            System.out.println(count + " records updated.");
        }
    }

    private static void deleteHabit() throws SQLException {
        System.out.print("Enter habit ID to delete: ");
        int habitID = scanner.nextInt();
        scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM ReadingHabit WHERE habitID = ?")) {
            stmt.setInt(1, habitID);
            int count = stmt.executeUpdate();
            System.out.println(count + " records deleted.");
        }
    }

    private static void getMeanAge() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT AVG(age) FROM User")) {
            System.out.printf("Mean age of users: %.2f%n", rs.getDouble(1));
        }
    }

    private static void getBookReaders() throws SQLException {
        System.out.print("Enter book title: ");
        String bookTitle = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(DISTINCT userID) FROM ReadingHabit WHERE book LIKE ?")) {
            stmt.setString(1, "%" + bookTitle + "%");
            ResultSet rs = stmt.executeQuery();
            System.out.println("Number of readers: " + rs.getInt(1));
        }
    }

    private static void getTotalPagesRead() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(pagesRead) FROM ReadingHabit")) {
            System.out.println("Total pages read by all users: " + rs.getInt(1));
        }
    }

    private static void getMultiBookReaders() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM (" +
                             "  SELECT userID FROM ReadingHabit " +
                             "  GROUP BY userID HAVING COUNT(DISTINCT book) > 1" +
                             ")")) {
            System.out.println("Number of users who read multiple books: " + rs.getInt(1));
        }
    }

    private static String shortenString(String str, int maxLength) {
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}