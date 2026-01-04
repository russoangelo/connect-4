package com.connectfour.connect4;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.io.*;
import java.net.Socket;

public class ClientGUI extends Application {

    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int CELL_SIZE = 80;
    private static final int DISC_RADIUS = 30;

    private Circle[][] discCircles = new Circle[ROWS][COLS];
    private boolean[][] isAnimated = new boolean[ROWS][COLS];
    private Pane gamePane;
    private Label statusLabel;
    private Label turnLabel;

    private char playerSymbol;
    private boolean myTurn = false;
    private boolean gameOver = false;

    private PrintWriter out;
    private BufferedReader in;

    private StringBuilder boardBuffer = new StringBuilder();
    private int boardLinesReceived = 0;

    @Override
    public void start(Stage primaryStage) {
        // Layout principale
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2C3E50;");

        // Pannello superiore con info
        VBox topPanel = createTopPanel();
        root.setTop(topPanel);

        // Griglia di gioco
        gamePane = createGameBoard();
        root.setCenter(gamePane);

        // Pannello inferiore con bottoni colonna
        HBox bottomPanel = createColumnButtons();
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, COLS * CELL_SIZE + 40, ROWS * CELL_SIZE + 180);
        primaryStage.setTitle("Forza 4 - Connessione...");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Connessione al server in un thread separato
        new Thread(() -> connectToServer(primaryStage)).start();
    }

    private VBox createTopPanel() {
        VBox topPanel = new VBox(10);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(15));
        topPanel.setStyle("-fx-background-color: #34495E;");

        turnLabel = new Label("In attesa del server...");
        turnLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ECF0F1;");

        topPanel.getChildren().addAll(turnLabel, statusLabel);
        return topPanel;
    }

    private Pane createGameBoard() {
        Pane pane = new Pane();
        pane.setPrefSize(COLS * CELL_SIZE, ROWS * CELL_SIZE);
        pane.setStyle("-fx-background-color: #3498DB;");

        // Crea la griglia blu e i cerchi per le pedine
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                // Cerchio bianco (vuoto)
                Circle hole = new Circle(DISC_RADIUS);
                hole.setCenterX(col * CELL_SIZE + CELL_SIZE / 2);
                hole.setCenterY(row * CELL_SIZE + CELL_SIZE / 2);
                hole.setFill(Color.WHITE);
                pane.getChildren().add(hole);

                // Cerchio per la pedina (inizialmente trasparente)
                Circle disc = new Circle(DISC_RADIUS);
                disc.setCenterX(col * CELL_SIZE + CELL_SIZE / 2);
                disc.setCenterY(row * CELL_SIZE + CELL_SIZE / 2);
                disc.setFill(Color.TRANSPARENT);
                disc.setStroke(Color.TRANSPARENT);
                discCircles[row][col] = disc;
                pane.getChildren().add(disc);
            }
        }

        return pane;
    }

    private HBox createColumnButtons() {
        HBox buttonPanel = new HBox(5);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(15));
        buttonPanel.setStyle("-fx-background-color: #34495E;");

        for (int col = 0; col < COLS; col++) {
            final int column = col;
            Button btn = new Button("" + col);
            btn.setPrefSize(CELL_SIZE - 10, 40);
            btn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            btn.setOnAction(e -> {
                if (gameOver) {
                    // Non fare niente se il gioco è finito
                    return;
                }

                if (myTurn) {
                    // È il tuo turno - invia la mossa
                    sendMove(column);
                }
                // Se NON è il tuo turno, semplicemente ignora il click (no messaggi)
            });

            buttonPanel.getChildren().add(btn);
        }

        return buttonPanel;
    }

    private void connectToServer(Stage stage) {
        try {
            Socket socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Platform.runLater(() -> stage.setTitle("Forza 4 - Connesso"));

            // Thread per ascoltare i messaggi dal server
            String message;
            while ((message = in.readLine()) != null) {
                processServerMessage(message, stage);
            }

        } catch (IOException e) {
            Platform.runLater(() -> {
                updateStatus("Errore di connessione: " + e.getMessage());
                stage.setTitle("Forza 4 - Errore connessione");
            });
        }
    }

    private void processServerMessage(String message, Stage stage) {
        Platform.runLater(() -> {
            if (message.equals("BOARD_START")) {
                boardBuffer = new StringBuilder();
                boardLinesReceived = 0;

            } else if (message.equals("BOARD_END")) {
                System.out.println("DEBUG CLIENT: Tabellone ricevuto:\n" + boardBuffer.toString());
                updateBoard(boardBuffer.toString());
                boardBuffer = new StringBuilder();
                boardLinesReceived = 0;

            } else if (boardBuffer.length() > 0 || (boardLinesReceived == 0 && message.contains("|"))) {
                if (message.contains("|")) {
                    boardBuffer.append(message).append("\n");
                    boardLinesReceived++;
                }

            } else if (message.startsWith("PLAYER_ID:")) {
                playerSymbol = message.substring(10).charAt(0);
                stage.setTitle("Forza 4 - Giocatore " + playerSymbol);
                updateTurn("Sei il giocatore " + playerSymbol + " - " + (playerSymbol == 'X' ? "ROSSO" : "GIALLO"));

            } else if (message.equals("YOUR_TURN")) {
                myTurn = true;
                updateTurn("🎯 È IL TUO TURNO!");
                updateStatus("Clicca su una colonna per giocare");

            } else if (message.equals("WAIT_TURN")) {
                // Messaggio iniziale per chi deve aspettare
                myTurn = false;
                updateTurn("⏳ Turno dell'avversario");
                updateStatus("Aspetta il tuo turno...");

            } else if (message.startsWith("WAIT:")) {
                myTurn = false;
                updateStatus(message.substring(5));

            } else if (message.equals("VALID_MOVE")) {
                // Dopo una mossa valida, NON è più il tuo turno
                myTurn = false;
                updateTurn("⏳ Turno dell'avversario");
                updateStatus("Mossa accettata! Aspetta l'avversario...");

            } else if (message.startsWith("INVALID_MOVE:")) {
                // Resta il tuo turno se la mossa è invalida
                updateStatus("❌ " + message.substring(13));

            } else if (message.startsWith("OPPONENT_MOVE:")) {
                myTurn = false;
                updateTurn("⏳ Turno dell'avversario");
                updateStatus("L'avversario sta giocando...");

            } else if (message.equals("WIN")) {
                gameOver = true;
                myTurn = false;
                updateTurn("🎉 HAI VINTO! 🎉");
                updateStatus("Congratulazioni!");
                showWinDialog("Hai vinto!");

            } else if (message.equals("LOSE")) {
                gameOver = true;
                myTurn = false;
                updateTurn("😞 HAI PERSO");
                updateStatus("Ritenta!");
                showWinDialog("Hai perso!");

            } else if (message.equals("DRAW")) {
                gameOver = true;
                myTurn = false;
                updateTurn("🤝 PAREGGIO");
                updateStatus("Partita pari");
                showWinDialog("Pareggio!");

            } else if (message.equals("OPPONENT_DISCONNECTED")) {
                gameOver = true;
                updateTurn("⚠️ L'avversario si è disconnesso");
                updateStatus("Hai vinto per abbandono!");
                showWinDialog("Avversario disconnesso!");

            } else if (!message.trim().isEmpty()) {
                updateStatus(message);
            }
        });
    }

    private void updateBoard(String boardString) {
        String[] lines = boardString.split("\n");

        System.out.println("DEBUG: Processando tabellone con " + lines.length + " righe");

        // IMPORTANTE: Confronta con lo stato precedente
        boolean[][] newState = new boolean[ROWS][COLS];

        for (int lineIndex = 0; lineIndex < ROWS && lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            int row = lineIndex;
            int colIndex = 0;

            for (int i = 0; i < line.length() && colIndex < COLS; i++) {
                char c = line.charAt(i);
                if (c == 'X' || c == 'O' || c == ' ') {
                    Circle disc = discCircles[row][colIndex];

                    if (c == 'X') {
                        newState[row][colIndex] = true;
                        // Anima SOLO se NON era già presente
                        if (!isAnimated[row][colIndex]) {
                            System.out.println("DEBUG: Animazione ROSSA in riga=" + row + ", col=" + colIndex);
                            animateDisc(disc, Color.RED, row, colIndex);
                            isAnimated[row][colIndex] = true;
                        } else {
                            // Era già presente - assicurati sia rossa
                            disc.setFill(Color.RED);
                            disc.setStroke(Color.BLACK);
                            disc.setStrokeWidth(2);
                        }
                    } else if (c == 'O') {
                        newState[row][colIndex] = true;
                        if (!isAnimated[row][colIndex]) {
                            System.out.println("DEBUG: Animazione GIALLA in riga=" + row + ", col=" + colIndex);
                            animateDisc(disc, Color.YELLOW, row, colIndex);
                            isAnimated[row][colIndex] = true;
                        } else {
                            disc.setFill(Color.YELLOW);
                            disc.setStroke(Color.BLACK);
                            disc.setStrokeWidth(2);
                        }
                    } else {
                        newState[row][colIndex] = false;
                        disc.setFill(Color.TRANSPARENT);
                        disc.setStroke(Color.TRANSPARENT);
                        isAnimated[row][colIndex] = false;
                    }
                    colIndex++;
                }
            }
        }
    }

    private void animateDisc(Circle disc, Color color, int targetRow, int targetCol) {
        // Animazione: la pedina cade dall'alto
        Circle fallingDisc = new Circle(DISC_RADIUS);
        fallingDisc.setCenterX(disc.getCenterX());
        fallingDisc.setCenterY(-DISC_RADIUS * 2); // Parte da SOPRA
        fallingDisc.setFill(color);
        fallingDisc.setStroke(Color.BLACK);
        fallingDisc.setStrokeWidth(2);

        gamePane.getChildren().add(fallingDisc);

        // Calcola distanza
        double targetY = disc.getCenterY();
        double distance = targetY + DISC_RADIUS * 2; // Distanza totale da percorrere

        // Animazione
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(fallingDisc);
        transition.setDuration(Duration.millis(500));
        transition.setByY(distance);

        transition.setOnFinished(e -> {
            gamePane.getChildren().remove(fallingDisc);
            disc.setFill(color);
            disc.setStroke(Color.BLACK);
            disc.setStrokeWidth(2);
        });

        transition.play();
    }

    private void sendMove(int column) {
        if (out != null) {
            out.println(column);
            myTurn = false;
        }
    }

    private void updateTurn(String text) {
        turnLabel.setText(text);
    }

    private void updateStatus(String text) {
        statusLabel.setText(text);
    }

    private void showWinDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fine partita");
        alert.setHeaderText(message);
        alert.setContentText("Chiudi per terminare");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}