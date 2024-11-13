package com.number_guessing_game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NumberGuessingClient extends Application {
    private String name;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    // JavaFX UI elements
    private TextArea messageArea;
    private TextField nameField;
    private TextField guessField;
    private Button connectButton;
    private Button guessButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Number Guessing Game Client");

        // UI components
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setWrapText(true);

        nameField = new TextField();
        nameField.setPromptText("Enter your name");

        guessField = new TextField();
        guessField.setPromptText("Enter your guess");
        guessField.setDisable(true); // Initially disabled until connected

        connectButton = new Button("Connect");
        connectButton.setOnAction(e -> connectToServer());

        guessButton = new Button("Guess");
        guessButton.setDisable(true); // Initially disabled until connected
        guessButton.setOnAction(e -> sendGuess());

        // Layout
        VBox layout = new VBox(10, new Label("Name:"), nameField, connectButton, 
                               new Label("Guess:"), guessField, guessButton, 
                               new Label("Messages:"), messageArea);
        layout.setSpacing(10);

        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connectToServer() {
        String serverAddress = "localhost";
        int port = 8888;

        // Connect to the server
        try {
            socket = new Socket(serverAddress, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            name = nameField.getText().trim();
            if (name.isEmpty()) {
                messageArea.appendText("Please enter a name.\n");
                return;
            }
            writer.println(name); // Send name to server

            messageArea.appendText("Connected to server on port " + port + "\n");
            connectButton.setDisable(true);
            guessField.setDisable(false);
            guessButton.setDisable(false);

            // Start listening to server messages
            new Thread(() -> listenToServer()).start();
        } catch (IOException e) {
            messageArea.appendText("Unable to connect to server: " + e.getMessage() + "\n");
        }
    }

    private void listenToServer() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                String finalMessage = message;
                // Update the message area in the JavaFX Application Thread
                javafx.application.Platform.runLater(() -> messageArea.appendText(finalMessage + "\n"));
            }
        } catch (IOException e) {
            javafx.application.Platform.runLater(() -> messageArea.appendText("Disconnected from server.\n"));
        }
    }

    private void sendGuess() {
        String guessText = guessField.getText().trim();
        if (guessText.isEmpty()) {
            messageArea.appendText("Please enter a guess.\n");
            return;
        }

        try {
            int guess = Integer.parseInt(guessText);
            writer.println(guess); 
            guessField.clear();
        } catch (NumberFormatException e) {
            messageArea.appendText("Invalid input. Please enter a valid number.\n");
        }
    }
}
