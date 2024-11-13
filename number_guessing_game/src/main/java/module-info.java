module com.number_guessing_game {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.number_guessing_game to javafx.fxml;
    exports com.number_guessing_game;
}
