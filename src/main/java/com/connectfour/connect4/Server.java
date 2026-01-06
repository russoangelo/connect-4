package com.connectfour.connect4;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server {

    private static int gameCounter = 0;

    public static void main(String[] args) {

        final int PORT = 12345;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("🎮 SERVER FORZA 4 MULTI-PARTITA");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("📡 Porta: " + PORT);
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

            while (true) {

                System.out.println("⏳ In attesa di 2 giocatori per nuova partita...");

                Socket player1Socket = serverSocket.accept();
                System.out.println("  ✅ Giocatore 1 connesso: " + player1Socket.getInetAddress());

                Socket player2Socket = serverSocket.accept();
                System.out.println("  ✅ Giocatore 2 connesso: " + player2Socket.getInetAddress());

                gameCounter++;
                int gameId = gameCounter;

                System.out.println("\n🎯 PARTITA #" + gameId + " INIZIATA!");
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

                new Thread(() -> startGame(player1Socket, player2Socket, gameId)).start();

            }

        } catch (IOException e) {
            System.err.println("❌ Errore nel server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void startGame(Socket player1, Socket player2, int gameId) {
        try {
            Game game = new Game();

            ClientHandler handler1 = new ClientHandler(player1, game, 'X', gameId);
            ClientHandler handler2 = new ClientHandler(player2, game, 'O', gameId);

            handler1.setOpponent(handler2);
            handler2.setOpponent(handler1);

            Thread thread1 = new Thread(handler1);
            Thread thread2 = new Thread(handler2);

            thread1.start();
            thread2.start();

            System.out.println("  [Partita #" + gameId + "] Thread avviati");

            handler1.sendMessage("YOUR_TURN");
            handler2.sendMessage("WAIT_TURN");

            thread1.join();
            thread2.join();

            System.out.println("🏁 Partita #" + gameId + " terminata!\n");

        } catch (InterruptedException e) {
            System.err.println("❌ Partita #" + gameId + " interrotta: " + e.getMessage());
        }
    }
}