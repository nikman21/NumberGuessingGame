package com.number_guessing_game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Client extends Application {
    private String name;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    // JavaFX UI elements
    private TextArea messageArea;
    private TextField messageField;
    private TextField nameField;
    private Button connectButton;
    private Button sendButton;

    public Client() {
    }

    public Client(Socket socket, String name) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.name = name;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // UI setup
        primaryStage.setTitle("Chat Application");

        // Name input and connect button
        nameField = new TextField();
        nameField.setPromptText("Enter your username");
        connectButton = new Button("Connect");
        connectButton.setOnAction(e -> connect());

        // Message area and message input
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageField = new TextField();
        messageField.setPromptText("Enter your message");
        sendButton = new Button("Send");
        sendButton.setDisable(true); // Disable until connected
        sendButton.setOnAction(e -> sendMessage());

        // Layout setup
        VBox root = new VBox(10);
        HBox nameBox = new HBox(5, nameField, connectButton);
        HBox messageBox = new HBox(5, messageField, sendButton);
        root.getChildren().addAll(nameBox, messageArea, messageBox);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connect() {
        name = nameField.getText();
        if (name.isEmpty()) {
            showAlert("Username required", "Please enter a username to connect.");
            return;
        }
        try {
            socket = new Socket("localhost", 1234);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send the username to the server
            bufferedWriter.write(name);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            listenForMessages();
            sendButton.setDisable(false); // Enable the send button after connecting
            connectButton.setDisable(true); // Disable the connect button

        } catch (IOException e) {
            showAlert("Connection Error", "Could not connect to server.");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void sendMessage() {
        String messageToSend = messageField.getText();
        if (messageToSend.isEmpty()) return;

        try {
            // Send the message to the server
            bufferedWriter.write(name + ": " + messageToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Display the message in the local messageArea
            messageArea.appendText("Me: " + messageToSend + "\n");

            // Clear the message field after sending
            messageField.clear();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void listenForMessages() {
        new Thread(() -> {
            String msgFromGroupChat;

            while (socket.isConnected()) {
                try {
                    msgFromGroupChat = bufferedReader.readLine();
                    if (msgFromGroupChat != null) {
                        String finalMsgFromGroupChat = msgFromGroupChat;
                        messageArea.appendText(finalMsgFromGroupChat + "\n");
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }).start();
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
