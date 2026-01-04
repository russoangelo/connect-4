package com.connectfour.connect4;

public class Game {

    public static final int ROWS = 6;
    public static final int COLS = 7;

    private char[][] board;
    private char currentPlayer;
    private boolean gameOver;
    private char winner;

    public Game() {
        board = new char[ROWS][COLS];
        currentPlayer = 'X';
        gameOver = false;
        winner = ' ';

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = ' ';
            }
        }
    }

    public synchronized boolean makeMove(int col, char player) {
        System.out.println("DEBUG: makeMove chiamato - col=" + col + ", player=" + player + ", currentPlayer=" + currentPlayer);

        if (col < 0 || col >= COLS || gameOver) {
            System.out.println("DEBUG: Mossa rifiutata - colonna invalida o gioco finito");
            return false;
        }

        if (player != currentPlayer) {
            System.out.println("DEBUG: Mossa rifiutata - non è il turno di " + player);
            return false;
        }

        // IMPORTANTE: Parte dal BASSO (riga 5) e va verso l'ALTO (riga 0)
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == ' ') {
                board[row][col] = player;
                System.out.println("DEBUG: ✓ Pedina inserita in riga=" + row + ", col=" + col);

                // Controlla vittoria o pareggio
                if (checkWin(player)) {
                    gameOver = true;
                    winner = player;
                    System.out.println("DEBUG: " + player + " ha vinto!");
                } else if (isDraw()) {
                    gameOver = true;
                    winner = 'D';
                    System.out.println("DEBUG: Pareggio!");
                } else {
                    // Cambia turno
                    currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                    System.out.println("DEBUG: Turno cambiato -> " + currentPlayer);
                }

                return true;
            }
        }

        System.out.println("DEBUG: Colonna " + col + " è PIENA!");
        return false;
    }

    public synchronized boolean checkWin(char player) {
        // Controllo orizzontale
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS - 3; c++) {
                if (board[r][c] == player &&
                        board[r][c + 1] == player &&
                        board[r][c + 2] == player &&
                        board[r][c + 3] == player) {
                    return true;
                }
            }
        }

        // Controllo verticale
        for (int r = 0; r < ROWS - 3; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] == player &&
                        board[r + 1][c] == player &&
                        board[r + 2][c] == player &&
                        board[r + 3][c] == player) {
                    return true;
                }
            }
        }

        // Controllo diagonale (\)
        for (int r = 0; r < ROWS - 3; r++) {
            for (int c = 0; c < COLS - 3; c++) {
                if (board[r][c] == player &&
                        board[r + 1][c + 1] == player &&
                        board[r + 2][c + 2] == player &&
                        board[r + 3][c + 3] == player) {
                    return true;
                }
            }
        }

        // Controllo diagonale (/)
        for (int r = 3; r < ROWS; r++) {
            for (int c = 0; c < COLS - 3; c++) {
                if (board[r][c] == player &&
                        board[r - 1][c + 1] == player &&
                        board[r - 2][c + 2] == player &&
                        board[r - 3][c + 3] == player) {
                    return true;
                }
            }
        }

        return false;
    }

    public synchronized boolean isDraw() {
        for (int c = 0; c < COLS; c++) {
            if (board[0][c] == ' ') {
                return false;
            }
        }
        return true;
    }

    public synchronized String getBoardAsString() {
        StringBuilder sb = new StringBuilder();

        for (int r = 0; r < ROWS; r++) {
            sb.append("|");
            for (int c = 0; c < COLS; c++) {
                sb.append(board[r][c]).append("|");
            }
            sb.append("\n");
        }
        sb.append(" 0 1 2 3 4 5 6 \n");

        return sb.toString();
    }

    public synchronized char getCurrentPlayer() {
        return currentPlayer;
    }

    public synchronized boolean isGameOver() {
        return gameOver;
    }

    public synchronized char getWinner() {
        return winner;
    }
}