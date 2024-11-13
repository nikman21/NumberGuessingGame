package com.number_guessing_game;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class NumberGuessingServer {
    private int port;
    private int hiddenNumber;
    private ArrayList<ClientHandler> clients = new ArrayList<>();
    private int currentPlayerIndex = 0;

    public NumberGuessingServer() {
        this.port = 8888;
    }

    public void go() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("Server started on port " + this.port);
            Random random = new Random();
            this.hiddenNumber = random.nextInt(1000000) + 1;

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket);
                clients.add(client);

                // Set the name of the client
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String name = reader.readLine();
                client.setName(name);

                client.start();
                sendPlayerList();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Notify all clients of the current players in the game
    public synchronized void sendPlayerList() {
        StringBuilder playerList = new StringBuilder("Players in the game: ");
        for (ClientHandler client : clients) {
            playerList.append(client.getName()).append(" ");
        }
        for (ClientHandler client : clients) {
            client.sendMessage(playerList.toString());
        }
        notifyCurrentPlayer();
    }

    private synchronized void notifyCurrentPlayer() {
        ClientHandler currentPlayer = clients.get(currentPlayerIndex);
        currentPlayer.sendMessage("It's your turn to guess!");
        currentPlayer.sendMessage("your_turn");  // Send a "your_turn" message to indicate it's their turn
    }

    private synchronized void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
        notifyCurrentPlayer();
    }

    // Generates a new number for the game
    public synchronized void redoNumber() {
        Random random = new Random();
        this.hiddenNumber = random.nextInt(1000000) + 1;
    }

    class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter writer;
        private String name;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
                writer.println("Welcome to the Number Guessing Game! To exit the game type -1");

                while (true) {
                    String input = reader.readLine();

                    if (input == null) {
                        System.out.println("Input is null, please try again.");
                        continue;
                    }

                    if (input.equals("-1")) {
                        System.out.println("Exiting program.");
                        break;
                    }

                    try {
                        int guess = Integer.parseInt(input);

                        // Check if it’s the client's turn
                        if (clients.get(currentPlayerIndex) == this) {
                            if (guess == hiddenNumber) {
                                sendToAll("The winner is " + this.name + ". A new round has started.");
                                redoNumber(); // New round
                                currentPlayerIndex = 0;
                                sendPlayerList(); // Notify players about the new round
                            } else if (guess < hiddenNumber) {
                                writer.println("Your number: " + guess + " was Too Low");
                                nextTurn();
                            } else {
                                writer.println("Your number: " + guess + " was Too High");
                                nextTurn();
                            }
                        } else {
                            writer.println("It's not your turn. Please wait.");
                        }
                    } catch (NumberFormatException e) {
                        writer.println("Invalid input. Please enter a valid number.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e);
            }
        }

        public void sendMessage(String message) {
            writer.println(message);
        }
    }

    public synchronized void sendToAll(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        NumberGuessingServer server = new NumberGuessingServer();
        server.go();
    }
}
