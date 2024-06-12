import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import javax.swing.*;

public class Minesweeper {
    private class MineTile extends JButton {
        int r;
        int c;

        public MineTile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    int tileSize = 70;
    int numRows = 8;
    int numCols = numRows;
    int boardWidth = numCols * tileSize;
    int boardHeight = numRows * tileSize;

    JFrame frame = new JFrame("Minesweeper");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel controlPanel = new JPanel();

    int mineCount = 10;
    JTextField mineCountInput = new JTextField(Integer.toString(mineCount), 5);
    JButton restartButton = new JButton("Restart");
    JButton undoButton = new JButton("Undo");
    MineTile[][] board = new MineTile[numRows][numCols];
    ArrayList<MineTile> mineList;
    Random random = new Random();

    int tilesClicked = 0; // Goal is to click all tiles except the ones containing mines
    boolean gameOver = false;
    Stack<String[][]> gameStateStack = new Stack<>();

    Minesweeper() {
        frame.setSize(boardWidth, boardHeight + 100);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Arial", Font.BOLD, 25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Minesweeper: " + Integer.toString(mineCount));
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel, BorderLayout.CENTER);
        frame.add(textPanel, BorderLayout.NORTH);

        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(new JLabel("Mines:"));
        controlPanel.add(mineCountInput);
        controlPanel.add(restartButton);
        controlPanel.add(undoButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        boardPanel.setLayout(new GridLayout(numRows, numCols)); // 8x8
        frame.add(boardPanel, BorderLayout.CENTER);

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = new MineTile(r, c);
                board[r][c] = tile;

                tile.setFocusable(false);
                tile.setMargin(new Insets(0, 0, 0, 0));
                tile.setFont(new Font("Arial Unicode MS", Font.PLAIN, 45));
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) {
                            return;
                        }
                        MineTile tile = (MineTile) e.getSource();

                        // Left click
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (tile.getText().equals("")) {
                                if (mineList.contains(tile)) {
                                    revealMines();
                                } else {
                                    checkMine(tile.r, tile.c);
                                    captureGameState();
                                }
                            }
                        }
                        // Right click
                        else if (e.getButton() == MouseEvent.BUTTON3) {
                            if (tile.getText().equals("") && tile.isEnabled()) {
                                tile.setText("🚩");
                                captureGameState();
                            } else if (tile.getText().equals("🚩")) {
                                tile.setText("");
                                captureGameState();
                            }
                        }
                    }
                });

                boardPanel.add(tile);
            }
        }

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
                captureGameState();
            }
        });

        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoLastMove();
            }
        });

        frame.setVisible(true);

        setMines();
        captureGameState(); // Capture initial game state
    }

    void setMines() {
        mineList = new ArrayList<>();
        int mineLeft = mineCount;
        while (mineLeft > 0) {
            int r = random.nextInt(numRows); // 0-7
            int c = random.nextInt(numCols);

            MineTile tile = board[r][c];
            if (!mineList.contains(tile)) {
                mineList.add(tile);
                mineLeft -= 1;
            }
        }
    }

    void revealMines() {
        for (MineTile tile : mineList) {
            tile.setText("💣");
        }

        gameOver = true;
        textLabel.setText("Game Over!");
    }

    void checkMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return;
        }

        MineTile tile = board[r][c];
        if (!tile.isEnabled()) {
            return;
        }
        tile.setEnabled(false);
        tilesClicked += 1;

        int minesFound = 0;

        // Top 3
        minesFound += countMine(r - 1, c - 1); // Top left
        minesFound += countMine(r - 1, c); // Top
        minesFound += countMine(r - 1, c + 1); // Top right

        // Left and right
        minesFound += countMine(r, c - 1); // Left
        minesFound += countMine(r, c + 1); // Right

        // Bottom 3
        minesFound += countMine(r + 1, c - 1); // Bottom left
        minesFound += countMine(r + 1, c); // Bottom
        minesFound += countMine(r + 1, c + 1); // Bottom right

        if (minesFound > 0) {
            tile.setText(Integer.toString(minesFound));
        } else {
            tile.setText("");

            // Top 3
            checkMine(r - 1, c - 1); // Top left
            checkMine(r - 1, c); // Top
            checkMine(r - 1, c + 1); // Top right

            // Left and right
            checkMine(r, c - 1); // Left
            checkMine(r, c + 1); // Right

            // Bottom 3
            checkMine(r + 1, c - 1); // Bottom left
            checkMine(r + 1, c); // Bottom
            checkMine(r + 1, c + 1); // Bottom right
        }

        if (tilesClicked == numRows * numCols - mineList.size()) {
            gameOver = true;
            textLabel.setText("Mines Cleared!");
        }
    }

    int countMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return 0;
        }
        if (mineList.contains(board[r][c])) {
            return 1;
        }
        return 0;
    }

    void captureGameState() {
        String[][] state = new String[numRows][numCols];
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = board[r][c];
                state[r][c] = tile.getText();
            }
        }
        gameStateStack.push(state);
    }

    void undoLastMove() {
        if (!gameStateStack.isEmpty()) {
            String[][] prevState = gameStateStack.pop();
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    MineTile tile = board[r][c];
                    String prevStateText = prevState[r][c];
                    tile.setText(prevStateText);
                    if (prevStateText.isEmpty()) {
                        tile.setEnabled(true); // Enable the tile if it was empty
                    }
                }
            }
            // Reset game over status if it was a game over state
            gameOver = false;
            if (!gameStateStack.isEmpty() && gameStateStack.peek().equals("Game Over!")) {
                gameOver = true;
            }
        } else {
            // If the stack is empty, reset the game to its initial state
            setMines();
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    MineTile tile = board[r][c];
                    tile.setText("");
                    tile.setEnabled(true);
                }
            }
            gameOver = false;
            textLabel.setText("Minesweeper: " + Integer.toString(mineCount));
        }
    }

    void restartGame() {
        tilesClicked = 0;
        gameOver = false;
        gameStateStack.clear();
        textLabel.setText("Minesweeper: " + mineCountInput.getText());
        mineCount = Integer.parseInt(mineCountInput.getText());
        setMines();
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = board[r][c];
                tile.setEnabled(true);
                tile.setText("");
            }
        }
    }
}