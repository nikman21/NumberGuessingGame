package com.number_guessing_game;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class Server {
    
    private ServerSocket serverSocket; 


    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try{
            while(!serverSocket.isClosed()) { //while serverSocket is not closed
                Socket socket = serverSocket.accept(); // this waits for the client to connect its a blocking method
                System.out.println("A new client has connected!");
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);

                thread.start();
            }
        }catch(IOException e){
            
        }
    }

    public void closeServerSocket() {
        try{
            if(serverSocket != null) {
                serverSocket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
    

}
