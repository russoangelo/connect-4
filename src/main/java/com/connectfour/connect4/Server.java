package com.connectfour.connect4;

import java.net.*;
import java.io.*;

public class Server {

    public static void main(String[] args) {

        final int PORT = 12345;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Server Forza 4 avviato sulla porta " + PORT);
            System.out.println("In attesa di 2 giocatori...");

            // Accetta primo giocatore
            Socket player1Socket = serverSocket.accept();
            System.out.println("Giocatore 1 (X) connesso da " + player1Socket.getInetAddress());

            // Accetta secondo giocatore
            Socket player2Socket = serverSocket.accept();
            System.out.println("Giocatore 2 (O) connesso da " + player2Socket.getInetAddress());

            // Crea l'oggetto Game condiviso
            Game game = new Game();

            // Crea i ClientHandler
            ClientHandler handler1 = new ClientHandler(player1Socket, game, 'X');
            ClientHandler handler2 = new ClientHandler(player2Socket, game, 'O');

            // Collega i due handler
            handler1.setOpponent(handler2);
            handler2.setOpponent(handler1);

            // Avvia i thread
            Thread thread1 = new Thread(handler1);
            Thread thread2 = new Thread(handler2);

            thread1.start();
            thread2.start();

            System.out.println("Partita iniziata!");

            // Notifica SOLO il primo giocatore che è il suo turno
            handler1.sendMessage("YOUR_TURN");
            // Notifica il secondo che deve aspettare
            handler2.sendMessage("WAIT_TURN");

            // Aspetta che entrambi i thread finiscano
            thread1.join();
            thread2.join();

            System.out.println("Partita terminata!");

        } catch (IOException | InterruptedException e) {
            System.err.println("Errore nel server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}