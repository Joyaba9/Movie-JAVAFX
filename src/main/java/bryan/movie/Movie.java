package bryan.movie;

import com.google.gson.annotations.SerializedName;
/**
        * Represents a movie with its title, release year, and sales.
        *
        * This class provides methods to get and set the movie's title, release year, and sales.
        *
 **/

public class Movie{

    //I certify that this submission is my original work - Bryan Joya
    @SerializedName("title")
    private String title;

    @SerializedName("year")
    private int year;  // No need for Integer if you do not require null value handling or collections that support only objects

    @SerializedName("sales")
    private long sales;  // Changed from Integer to long to handle larger numbers

    // Constructor
    public Movie(String title, int year, long sales) {
        this.title = title;
        this.year = year;
        this.sales = sales;
    }

    // Getters and Setters

    // returns the Title
    public String getTitle() {
        return title;
    }
// sets the Title
    public void setTitle(String title) {
        this.title = title;
    }
// Returns the Year
    public int getYear() {
        return year;
    }
// sets the year
    public void setYear(int year) {
        this.year = year;
    }
//Returns the sales of the movie.
    public long getSales() {
        return sales;
    }
// Sets the sales of the movie.
    public void setSales(long sales) {
        this.sales = sales;
    }
}