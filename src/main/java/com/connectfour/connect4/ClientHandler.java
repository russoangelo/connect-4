package com.connectfour.connect4;

import java.net.*;
import java.io.*;

public class ClientHandler implements Runnable {

    private Socket socket;
    private Game game;
    private char playerSymbol;
    private ClientHandler opponent;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected;

    public ClientHandler(Socket socket, Game game, char playerSymbol) {
        this.socket = socket;
        this.game = game;
        this.playerSymbol = playerSymbol;
        this.connected = true;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOpponent(ClientHandler opponent) {
        this.opponent = opponent;
    }

    public void sendMessage(String message) {
        if (connected && out != null) {
            out.println(message);
        }
    }

    public void sendBoard() {
        if (connected && out != null) {
            String board = game.getBoardAsString();
            // Invia il tabellone come UN UNICO messaggio con un marker
            out.println("BOARD_START");
            out.print(board);  // NON println - il board ha già i \n
            out.println("BOARD_END");
            out.flush();
        }
    }

    @Override
    public void run() {
        try {
            // Invia al client il suo simbolo
            sendMessage("PLAYER_ID:" + playerSymbol);
            sendMessage("Sei il giocatore " + playerSymbol);

            // Invia il tabellone iniziale
            sendBoard();

            String input;
            while (connected && (input = in.readLine()) != null) {

                if (game.isGameOver()) {
                    break;
                }

                if (game.getCurrentPlayer() != playerSymbol) {
                    sendMessage("WAIT:Non è il tuo turno!");
                    continue;
                }

                try {
                    int col = Integer.parseInt(input.trim());

                    if (game.makeMove(col, playerSymbol)) {
                        // Mossa valida
                        sendMessage("VALID_MOVE");
                        sendBoard();

                        if (opponent != null) {
                            opponent.sendMessage("OPPONENT_MOVE:" + col);
                            opponent.sendBoard();
                        }

                        // Controlla fine gioco
                        if (game.isGameOver()) {
                            char winner = game.getWinner();

                            if (winner == 'D') {
                                sendMessage("DRAW");
                                if (opponent != null) {
                                    opponent.sendMessage("DRAW");
                                }
                            } else if (winner == playerSymbol) {
                                sendMessage("WIN");
                                if (opponent != null) {
                                    opponent.sendMessage("LOSE");
                                }
                            } else {
                                sendMessage("LOSE");
                                if (opponent != null) {
                                    opponent.sendMessage("WIN");
                                }
                            }
                            break;
                        } else {
                            if (opponent != null) {
                                opponent.sendMessage("YOUR_TURN");
                            }
                        }

                    } else {
                        sendMessage("INVALID_MOVE:Mossa non valida");
                    }

                } catch (NumberFormatException e) {
                    sendMessage("INVALID_INPUT:Inserisci un numero 0-6");
                }
            }

        } catch (IOException e) {
            System.out.println("Giocatore " + playerSymbol + " disconnesso");
        } finally {
            if (opponent != null && !game.isGameOver()) {
                opponent.sendMessage("OPPONENT_DISCONNECTED");
            }

            connected = false;
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}