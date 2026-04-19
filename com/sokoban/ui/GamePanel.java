package com.sokoban.ui;

import com.sokoban.model.Level;
import com.sokoban.model.TileType;
import com.sokoban.util.AudioManager;
import com.sokoban.util.LevelLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel {
    private static final int TILE_SIZE = 64;
    private static final String IMAGE_BASE_PATH = "D:/Java program/Box pushing/Box pushing/res/images/"; // 修改为你的路径

    private List<Level> allLevels;
    private int currentLevelIdx;
    private Level currentLevel;
    private Level originalLevel;
    private Stack<MoveRecord> moveHistory;

    private BufferedImage backgroundImg;
    private BufferedImage wallImg, boxImg, targetImg;
    private BufferedImage playerUpImg, playerDownImg, playerLeftImg, playerRightImg;
    private BufferedImage currentPlayerImg;

    private enum Dir { UP, DOWN, LEFT, RIGHT }
    private Dir currentDir = Dir.DOWN;

    public GamePanel(List<Level> levels, int startIndex) {
        this.allLevels = levels;
        this.currentLevelIdx = startIndex;
        this.currentLevel = levels.get(startIndex);
        this.originalLevel = copyLevel(currentLevel);
        this.moveHistory = new Stack<>();

        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        loadResources();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleMove(e);
            }
        });
        if (!AudioManager.getInstance().isMuted()) {
            AudioManager.getInstance().playBGM("bgm.wav");
        }
        requestFocusInWindow(); // 初始焦点
    }

    private void loadResources() {
        try {
            File bgFile = new File(IMAGE_BASE_PATH + "background.png");
            backgroundImg = bgFile.exists() ? ImageIO.read(bgFile) : null;
            wallImg = ImageIO.read(new File(IMAGE_BASE_PATH + "wall.png"));
            boxImg = ImageIO.read(new File(IMAGE_BASE_PATH + "box.png"));
            targetImg = ImageIO.read(new File(IMAGE_BASE_PATH + "ball.png"));
            playerUpImg = ImageIO.read(new File(IMAGE_BASE_PATH + "up.png"));
            playerDownImg = ImageIO.read(new File(IMAGE_BASE_PATH + "down.png"));
            playerLeftImg = ImageIO.read(new File(IMAGE_BASE_PATH + "left.png"));
            playerRightImg = ImageIO.read(new File(IMAGE_BASE_PATH + "right.png"));
            currentPlayerImg = playerDownImg;
            System.out.println("图片加载成功");
        } catch (IOException e) {
            System.err.println("图片加载失败: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "图片加载失败！\n请检查路径：" + IMAGE_BASE_PATH);
        }
    }

    private void handleMove(KeyEvent e) {
        if (currentLevel.isWin()) return;
        int dx = 0, dy = 0;
        Dir newDir = null;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    dy = -1; newDir = Dir.UP; break;
            case KeyEvent.VK_DOWN:  dy = 1;  newDir = Dir.DOWN; break;
            case KeyEvent.VK_LEFT:  dx = -1; newDir = Dir.LEFT; break;
            case KeyEvent.VK_RIGHT: dx = 1;  newDir = Dir.RIGHT; break;
            default: return;
        }
        Level before = copyLevel(currentLevel);
        boolean moved = currentLevel.move(dx, dy);
        if (moved) {
            moveHistory.push(new MoveRecord(before, dx, dy));
            if (newDir != null) {
                currentDir = newDir;
                switch (currentDir) {
                    case UP:    currentPlayerImg = playerUpImg; break;
                    case DOWN:  currentPlayerImg = playerDownImg; break;
                    case LEFT:  currentPlayerImg = playerLeftImg; break;
                    case RIGHT: currentPlayerImg = playerRightImg; break;
                }
            }
            AudioManager.getInstance().playEffect("move.wav");
            repaint();
            if (currentLevel.isWin()) {
                handleWin();
            }
        }
    }

    private void handleWin() {
        AudioManager.getInstance().stopBGM();
        AudioManager.getInstance().playEffect("win.wav");
        int nextIdx = currentLevelIdx + 1;
        if (nextIdx < allLevels.size()) {
            int option = JOptionPane.showConfirmDialog(this,
                    "恭喜过关！是否进入下一关？", "胜利",
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                loadLevel(allLevels.get(nextIdx));
                currentLevelIdx = nextIdx;
                originalLevel = copyLevel(currentLevel);
                moveHistory.clear();
                repaint();
                if (!AudioManager.getInstance().isMuted()) {
                    AudioManager.getInstance().playBGM("bgm.wav");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "恭喜通关！");
        }
    }

    public void restartLevel() {
        restoreLevel(originalLevel);
        moveHistory.clear();
        repaint();
        requestFocusInWindow();
    }

    public void undoMove() {
        if (!moveHistory.isEmpty()) {
            MoveRecord rec = moveHistory.pop();
            restoreLevel(rec.level);
            repaint();
            requestFocusInWindow();
        }
    }

    public void loadLevel(Level newLevel) {
        this.currentLevel = newLevel;
        this.originalLevel = copyLevel(newLevel);
        this.moveHistory.clear();
        repaint();
        requestFocusInWindow();  // 关键：重新获取焦点
    }

    private Level copyLevel(Level src) {
        Level copy = new Level(src.getWidth(), src.getHeight(), src.getLevelNumber());
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                copy.setTile(x, y, src.getTile(x, y));
            }
        }
        return copy;
    }

    private void restoreLevel(Level level) {
        for (int y = 0; y < currentLevel.getHeight(); y++) {
            for (int x = 0; x < currentLevel.getWidth(); x++) {
                currentLevel.setTile(x, y, level.getTile(x, y));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int offsetX = (getWidth() - currentLevel.getWidth() * TILE_SIZE) / 2;
        int offsetY = (getHeight() - currentLevel.getHeight() * TILE_SIZE) / 2;

        for (int y = 0; y < currentLevel.getHeight(); y++) {
            for (int x = 0; x < currentLevel.getWidth(); x++) {
                int px = offsetX + x * TILE_SIZE;
                int py = offsetY + y * TILE_SIZE;
                if (backgroundImg != null) {
                    g2d.drawImage(backgroundImg, px, py, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g2d.setColor(new Color(60, 60, 70));
                    g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        for (int y = 0; y < currentLevel.getHeight(); y++) {
            for (int x = 0; x < currentLevel.getWidth(); x++) {
                TileType tile = currentLevel.getTile(x, y);
                int px = offsetX + x * TILE_SIZE;
                int py = offsetY + y * TILE_SIZE;
                switch (tile) {
                    case WALL:
                        if (wallImg != null) g2d.drawImage(wallImg, px, py, TILE_SIZE, TILE_SIZE, null);
                        else { g2d.setColor(Color.GRAY); g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE); }
                        break;
                    case BOX:
                        if (boxImg != null) g2d.drawImage(boxImg, px, py, TILE_SIZE, TILE_SIZE, null);
                        else { g2d.setColor(new Color(139,69,19)); g2d.fillRect(px+5, py+5, TILE_SIZE-10, TILE_SIZE-10); }
                        break;
                    case TARGET:
                        if (targetImg != null) g2d.drawImage(targetImg, px, py, TILE_SIZE, TILE_SIZE, null);
                        else { g2d.setColor(Color.RED); g2d.drawOval(px+15, py+15, TILE_SIZE-30, TILE_SIZE-30); }
                        break;
                    case BOX_ON_TARGET:
                        if (targetImg != null) g2d.drawImage(targetImg, px, py, TILE_SIZE, TILE_SIZE, null);
                        else { g2d.setColor(Color.RED); g2d.drawOval(px+15, py+15, TILE_SIZE-30, TILE_SIZE-30); }
                        if (boxImg != null) g2d.drawImage(boxImg, px, py, TILE_SIZE, TILE_SIZE, null);
                        else { g2d.setColor(new Color(139,69,19)); g2d.fillRect(px+5, py+5, TILE_SIZE-10, TILE_SIZE-10); }
                        break;
                    case PLAYER:
                    case PLAYER_ON_TARGET:
                        if (currentPlayerImg != null) g2d.drawImage(currentPlayerImg, px, py, TILE_SIZE, TILE_SIZE, null);
                        else drawDefaultPlayer(g2d, px, py);
                        break;
                    default: break;
                }
            }
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g2d.drawString("第 " + (currentLevelIdx + 1) + " 关", 20, 40);
        if (currentLevel.isWin()) {
            g2d.setColor(Color.YELLOW);
            g2d.drawString("胜利！", getWidth() - 80, 40);
        }
    }

    private void drawDefaultPlayer(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(70, 130, 200));
        g2d.fillOval(x + 12, y + 8, TILE_SIZE - 24, TILE_SIZE - 24);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x + 22, y + 18, 6, 6);
        g2d.fillOval(x + 36, y + 18, 6, 6);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x + 24, y + 20, 3, 3);
        g2d.fillOval(x + 38, y + 20, 3, 3);
        g2d.fillRect(x + 20, y + 32, TILE_SIZE - 40, TILE_SIZE - 40);
    }

    private static class MoveRecord {
        Level level;
        int dx, dy;
        MoveRecord(Level level, int dx, int dy) {
            this.level = level;
            this.dx = dx;
            this.dy = dy;
        }
    }
}