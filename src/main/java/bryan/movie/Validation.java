package bryan.movie;
/**
        * Provides static methods for validating movie attributes such as title, release year, and sales.
        *
        * This class contains regular expressions for validating different attributes of a movie.
        *
        *
        **/



public class Validation {
    // Regular expressions for validation

    //I certify that this submission is my original work - Bryan Joya

    // Method to validate title
    public static String validateTitle(String title) {
        if (title == null || title.isEmpty()) {
            return "Title cannot be empty.";
        }
        if (!title.matches("[A-Z][A-Za-z0-9-:.\\s]*")) {
            return "Title can’t be empty and can contain letters, numbers, hyphens, colons, periods, and spaces. The first letter must be capitalized.";
        }
        return "";
    }

    // Method to validate year
    public static String validateYear(String year) {
        if (year == null || year.isEmpty()) {
            return "Year cannot be empty.";
        }
        if (!year.matches("\\d+")) {
            return "Year can’t be empty and only contains digits.";
        }
        return "";
    }

    // Method to validate sales
    public static String validateSales(String sales) {
        if (sales == null || sales.isEmpty()) {
            return "Sales cannot be empty.";
        }
        if (!sales.matches("\\d+(\\.\\d+)?")) {
            return "Sales can’t be empty and can only contain digits. The decimal point is optional. The number of decimal places can be 0 or more.";
        }
        return "";
    }


}