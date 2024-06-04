package bryan.movie;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller Class. This Class Handles Movie list and connects to DB
 *
 *
 */



public class HelloController {

    @FXML
    private TextField titleInput;
    @FXML
    private TextField yearInput;
    @FXML
    private TextField SalesInput;

    @FXML
    private TableView<Movie> movieTableView;

    @FXML
    private TableColumn<Movie, String> titleColumn;

    @FXML
    private TableColumn<Movie, Integer> yearColumn;

    @FXML
    private TableColumn<Movie, Long> saleColumn;

    private String lastLoadedFilePath;

    private Stage stage;
/**
 * Initializes the controller class.
 *
 * */
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        saleColumn.setCellValueFactory(new PropertyValueFactory<>("sales"));
    }

    /**
     *
     * Creates connection and table to AccessDB
     *
     *
     */
    @FXML
    protected void onCreateTable() {
        Connection conn = null;
        String dbFilePath = ".//Movie.accdb";
        File dbFile = new File(dbFilePath);

        if (!dbFile.exists()) {
            try {
                DatabaseBuilder.create(Database.FileFormat.V2010, dbFile);
                System.out.println("The database file has been created.");
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
            }
        }

        String databaseURL = "jdbc:ucanaccess://" + dbFilePath;

        try {
            conn = DriverManager.getConnection(databaseURL);
            try (Statement dropTableStatement = conn.createStatement()) {
                dropTableStatement.execute("DROP TABLE IF EXISTS Movie");
                System.out.println("Table Movie dropped successfully.");
            } catch (SQLException dropException) {
                Logger.getLogger(HelloController.class.getName()).log(Level.SEVERE, null, dropException);
            }

            String sql = "CREATE TABLE Movie (Title nvarchar(255), Year INT, Sales BIGINT)";
            try (Statement createTableStatement = conn.createStatement()) {
                createTableStatement.execute(sql);
                conn.commit();
                System.out.println("Table Movie created successfully.");
            } catch (SQLException createException) {
                Logger.getLogger(HelloController.class.getName()).log(Level.SEVERE, null, createException);
            }
        } catch (SQLException ex) {
            Logger.getLogger(HelloController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Failed to close the database connection.");
                ex.printStackTrace();
            }
        }
    }

    /**
     *
     * Imports movies from a JSON file selected by the user
     *
     *
     */

    @FXML
    protected void importJson() {
        Connection conn = null;
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open JSON File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();

                try (FileReader fr = new FileReader(selectedFile)) {
                    Movie[] movies = gson.fromJson(fr, Movie[].class);

                    String dbFilePath = ".//Movie.accdb";
                    String databaseURL = "jdbc:ucanaccess://" + dbFilePath;

                    conn = DriverManager.getConnection(databaseURL);

                    String sql = "INSERT INTO Movie (Title, Year, Sales) VALUES (?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        for (Movie movie : movies) {
                            pstmt.setString(1, movie.getTitle());
                            pstmt.setInt(2, movie.getYear());
                            pstmt.setLong(3, movie.getSales());
                            pstmt.executeUpdate();
                        }
                        System.out.println("Movie data saved to database successfully.");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException | JsonSyntaxException | JsonIOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Failed to close the database connection.");
                ex.printStackTrace();
            }
        }
    }


    /**
     *
     * Displays DB list to tableView
     *
     *
     */
    @FXML
    protected void onListRecords() {
        String dbFilePath = ".//Movie.accdb";
        String databaseURL = "jdbc:ucanaccess://" + dbFilePath;

        try (Connection conn = DriverManager.getConnection(databaseURL)) {
            String sql = "SELECT * FROM Movie";
            try (Statement statement = conn.createStatement()) {
                ResultSet resultSet = statement.executeQuery(sql);

                ObservableList<Movie> olMovies = movieTableView.getItems();
                while (resultSet.next()) {
                    String title = resultSet.getString("Title");
                    Integer year = resultSet.getInt("Year");
                    Long sales = resultSet.getLong("Sales");

                    Movie movie = new Movie(title, year, sales);
                    olMovies.add(movie);
                }
                movieTableView.setItems(olMovies);
            } catch (SQLException e) {
                System.err.println("Failed to execute query.");
                e.printStackTrace();
            }
        } catch (SQLException ex) {
            System.err.println("Failed to establish database connection.");
            ex.printStackTrace();
        }
    }

    /**
     *
     * Adds records and Validates
     *
     *
     */

    @FXML
    protected void addRecord() {
        String dbFilePath = ".//Movie.accdb";
        String databaseURL = "jdbc:ucanaccess://" + dbFilePath;

        String title = titleInput.getText();
        Integer year = Integer.parseInt(yearInput.getText());
        Long sales = Long.valueOf(SalesInput.getText());

        String titleValidationResult = Validation.validateTitle(title);
        String yearValidationResult = Validation.validateYear(year.toString());
        String salesValidationResult = Validation.validateSales(sales.toString());

        if (!titleValidationResult.isEmpty()) {
            showAlert("Error", "Invalid Input", titleValidationResult);
            return;
        }
        if (!yearValidationResult.isEmpty()) {
            showAlert("Error", "Invalid Input", yearValidationResult);
            return;
        }
        if (!salesValidationResult.isEmpty()) {
            showAlert("Error", "Invalid Input", salesValidationResult);
            return;
        }

        try {
            Movie movie = new Movie(title, year, sales);

            String insertSQL = "INSERT INTO Movie (Title, Year, Sales) VALUES (?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(databaseURL);
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, movie.getTitle());
                pstmt.setInt(2, movie.getYear());
                pstmt.setLong(3, movie.getSales());
                pstmt.executeUpdate();
                System.out.println("Movie data saved to database successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Database Error", "Failed to add record to the database.");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            showAlert("Error", "Invalid Input", "Please enter valid values for year and sales.");
        }
    }

    /**
     *
     * show alrts
     *
     *
     */
    @FXML
    private void showAlert(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    @FXML
    protected void onDeleteRecord() {
        Movie selectedMovie = movieTableView.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            showAlert("Error", "No Selection", "Please select a movie in the table to delete.");
            return;
        }

        // Remove from TableView
        movieTableView.getItems().remove(selectedMovie);

    }


    @FXML
    protected void onAboutClick() {
        Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
        aboutAlert.setTitle("About Movie Database");
        aboutAlert.setHeaderText("Name and Integrity Statement");
        aboutAlert.setContentText("I Certify that this submission is my original Work - Bryan Joya");

        aboutAlert.showAndWait(); // Display the alert and wait for it to be closed
    }


        @FXML
    protected void onExitClick() {
        Platform.exit();
    }
}
