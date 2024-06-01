package org.example.hygyhgvkju;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game extends Application implements Serializable {
    private static final int DEFAULT_GRID_SIZE = 5;
    private static final String STONE_COLOR_1 = colorToHex(Color.RED);
    private static final String STONE_COLOR_2 = colorToHex(Color.BLUE);

    /**
     * Function that convers the colour in hexadecimal
     * @param color
     * @return String code
     */
    private static String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
    private int gridSize;
    private boolean isPlayer1Turn;
    private List<Stick> sticks;
    private List<Stone> stones;
    private Canvas canvas;
    private GraphicsContext gc;

    /**
     * Creates the game grid, the buttons.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Positional Game");
        gridSize = DEFAULT_GRID_SIZE;
        isPlayer1Turn = true;
        sticks = new ArrayList<>();
        stones = new ArrayList<>();

        Label gridSizeLabel = new Label("Grid Size:");
        TextField gridSizeField = new TextField(Integer.toString(DEFAULT_GRID_SIZE));
        Button newGameButton = new Button("New Game");
        newGameButton.setOnAction(e -> {
            gridSize = Integer.parseInt(gridSizeField.getText());
            createNewGame();
        });

        VBox configPanel = new VBox(10);
        configPanel.setPadding(new Insets(10));
        configPanel.getChildren().addAll(gridSizeLabel, gridSizeField, newGameButton);

        canvas = new Canvas();
        canvas.setWidth(400);
        canvas.setHeight(400);
        gc = canvas.getGraphicsContext2D();
        canvas.setOnMouseClicked(e -> handleMousePressed(e.getX(), e.getY()));

        Button loadButton = new Button("Load");
        loadButton.setOnAction(e -> loadGame());
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveGame());
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> primaryStage.close());
        Button exportImageButton = new Button("Export Board as Image");
        exportImageButton.setOnAction(e -> exportBoardAsImage());

        HBox controlPanel = new HBox(10);
        controlPanel.setPadding(new Insets(10));;
        controlPanel.getChildren().addAll(loadButton, saveButton, exitButton, exportImageButton);
        BorderPane root = new BorderPane();
        root.setTop(configPanel);
        root.setCenter(canvas);
        root.setBottom(controlPanel);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * Draws the game board including sticks and stones.
     * The board is drawn based on the current state of the game.
     * Sticks are drawn in gray and stones are drawn in their respective colors.
     */
    private void drawBoard() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.GRAY);
        double cellWidth = canvas.getWidth() / gridSize;
        double cellHeight = canvas.getHeight() / gridSize;
        for (int i = 0; i <= gridSize; i++) {
            double x = i * cellWidth;
            double y = i * cellHeight;
            gc.strokeLine(x, 0, x, canvas.getHeight());
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        for (Stick stick : sticks) {
            double x1 = stick.getNode1X() * cellWidth;
            double y1 = stick.getNode1Y() * cellHeight;
            double x2 = stick.getNode2X() * cellWidth;
            double y2 = stick.getNode2Y() * cellHeight;
            gc.strokeLine(x1, y1, x2, y2);
        }

        for (Stone stone : stones) {
            double centerX = stone.getX() * cellWidth;
            double centerY = stone.getY() * cellHeight;
            gc.setFill(Color.web(stone.getColor()));
            gc.fillOval(centerX - cellWidth / 4, centerY - cellHeight / 4, cellWidth / 2, cellHeight / 2);
        }
    }

    /**
     * Creates new game, redraws random horizontal and vertical sticks.
     */
    private void createNewGame() {
        sticks.clear();
        stones.clear();
        isPlayer1Turn = true;

        Random random = new Random();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize - 1; j++) {
                if (random.nextBoolean()) {
                    Stick stick = new Stick(i, j, i, j + 1);
                    sticks.add(stick);
                }
            }
        }
        for (int i = 0; i < gridSize - 1; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (random.nextBoolean()) {
                    Stick stick = new Stick(i, j, i + 1, j);
                    sticks.add(stick);
                }
            }
        }
        drawBoard();
    }


    /**
     * Handles the event when the mouse is pressed on the canvas.
     * This method is responsible for placing stones on the game board
     * based on the player's and AI's moves.
     *
     * @param x The x-coordinate of the mouse click.
     * @param y The y-coordinate of the mouse click.
     */
    private void handleMousePressed(double x, double y) {
        int nodeX = (int) Math.round(x / (canvas.getWidth() / gridSize));
        int nodeY = (int) Math.round(y / (canvas.getHeight() / gridSize));

        if (isValidMove(nodeX, nodeY)) {
            Stone stone = new Stone(nodeX, nodeY, isPlayer1Turn ? STONE_COLOR_1 : STONE_COLOR_2);
            stones.add(stone);
            isPlayer1Turn = !isPlayer1Turn;
            drawBoard();

            if (isGameOver()) {
                String winner = isPlayer1Turn ? "Player 2" : "Player 1";
                System.out.println("Game Over! " + winner + " wins!");
            }
        } else {
            System.out.println("Invalid move! Stone cannot be placed there.");
        }
    }


    /**
     * Calculates the AI next move
     * @return Point
     */
    private Point getAIMove() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (isValidMove(i, j)) {
                    return new Point(i, j);
                }
            }
        }
        return null;
    }

    /**
     * Checks if the proposed stone placement is valid.
     * A stone can be placed if:
     * 1. The game board is empty (for the first stone).
     * 2. The proposed node is not already occupied by another stone.
     * 3. The proposed node is adjacent to the previously placed stone.
     *
     * @param nodeX The x-coordinate of the proposed node.
     * @param nodeY The y-coordinate of the proposed node.
     * @return True if the move is valid, false otherwise.
     */
    private boolean isValidMove(int nodeX, int nodeY) {
        if (stones.isEmpty()) {
            return true;
        }

        if (isStoneExist(nodeX, nodeY)) {
            return false;
        }

        Stone prevStone = stones.get(stones.size() - 1);
        int prevStoneX = prevStone.getX();
        int prevStoneY = prevStone.getY();

        boolean onSameStick = false;
        for (Stick stick : sticks) {
            if ((stick.getNode1X() == prevStoneX && stick.getNode1Y() == prevStoneY &&
                    (stick.getNode2X() == nodeX && stick.getNode2Y() == nodeY || stick.getNode1X() == nodeX && stick.getNode1Y() == nodeY)) ||
                    (stick.getNode2X() == prevStoneX && stick.getNode2Y() == prevStoneY &&
                            (stick.getNode1X() == nodeX && stick.getNode1Y() == nodeY || stick.getNode2X() == nodeX && stick.getNode2Y() == nodeY))) {
                onSameStick = true;
                break;
            }
        }
        return onSameStick;
    }

    /**
     * Checks if a stone is already placed in that node
     * @param nodeX
     * @param nodeY
     * @return true/false
     */
    private boolean isStoneExist(int nodeX, int nodeY) {
        for (Stone stone : stones) {
            if (stone.getX() == nodeX && stone.getY() == nodeY) {
                return true;
            }
        }
        return false;
    }
    private boolean isGameOver() {
        for (Stick stick : sticks) {
            if (isValidMove(stick.getNode1X(), stick.getNode1Y()) || isValidMove(stick.getNode2X(), stick.getNode2Y())) {
                return false;
            }
        }
        return true;
    }
    /**
     * Saves the current state of the game to a file using object serialization.
     * Opens a file chooser dialog to select the location to save the game.
     * Writes game-related data (grid size, player turn, sticks, stones) to the selected file.
     */
    private void saveGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Game");
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeInt(gridSize);
                oos.writeBoolean(isPlayer1Turn);
                oos.writeObject(sticks);
                oos.writeObject(stones);
                System.out.println("Game saved successfully!");
            } catch (IOException e) {
                System.out.println("Failed to save the game!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads a saved game state from a file using object deserialization.
     * Opens a file chooser dialog to select the saved game file.
     * Reads game-related data (grid size, player turn, sticks, stones) from the selected file.
     * Updates the game state with the loaded data and redraws the board.
     */
    private void loadGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Game");
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                gridSize = ois.readInt();
                isPlayer1Turn = ois.readBoolean();
                sticks = (List<Stick>) ois.readObject();
                stones = (List<Stone>) ois.readObject();
                drawBoard();
                System.out.println("Game loaded successfully!");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Failed to load the game!");
                e.printStackTrace();
            }
        }
    }
    /**
     * Exports the current state of the game board as a PNG image file.
     * Prompts the user to select the location to save the image.
     * Draws the board onto a BufferedImage using Graphics2D, then saves it as a PNG file.
     */
    private void exportBoardAsImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Board as Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            int width = (int) canvas.getWidth();
            int height = (int) canvas.getHeight();

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            drawBoardWithGraphics2D(g2d);

            g2d.dispose();

            try {
                ImageIO.write(bufferedImage, "png", file);
                System.out.println("Board exported successfully as PNG image.");
            } catch (IOException e) {
                System.out.println("Error exporting board as PNG image: " + e.getMessage());
            }
        }
    }
    /**
     * Draws the game board onto a BufferedImage using Graphics2D.
     * This method is called by exportBoardAsImage().
     *
     * @param g2d The Graphics2D object to draw with.
     */
    private void drawBoardWithGraphics2D(Graphics2D g2d) {
        double cellWidth = canvas.getWidth() / gridSize;
        double cellHeight = canvas.getHeight() / gridSize;

        g2d.setColor(java.awt.Color.GRAY);
        for (int i = 0; i <= gridSize; i++) {
            double x = i * cellWidth;
            double y = i * cellHeight;
            g2d.drawLine((int) x, 0, (int) x, (int) canvas.getHeight());
            g2d.drawLine(0, (int) y, (int) canvas.getWidth(), (int) y);
        }
        g2d.setColor(java.awt.Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        for (Stick stick : sticks) {
            double x1 = stick.getNode1X() * cellWidth;
            double y1 = stick.getNode1Y() * cellHeight;
            double x2 = stick.getNode2X() * cellWidth;
            double y2 = stick.getNode2Y() * cellHeight;
            g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        }
        for (Stone stone : stones) {
            double centerX = stone.getX() * cellWidth;
            double centerY = stone.getY() * cellHeight;
            java.awt.Color color = java.awt.Color.decode(stone.getColor());
            g2d.setColor(color);
            g2d.fillOval((int) (centerX - cellWidth / 4), (int) (centerY - cellHeight / 4), (int) (cellWidth / 2), (int) (cellHeight / 2));
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
