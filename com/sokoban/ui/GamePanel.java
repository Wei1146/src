package com.sokoban.ui;

import com.sokoban.model.Level;
import com.sokoban.model.TileType;
import com.sokoban.util.LevelLoader;
import com.sokoban.util.AudioManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class GamePanel extends JPanel {

    private Level currentLevel;
    private int currentLevelIndex;
    private TileType[][] initialEntity;
    private int initialPlayerX, initialPlayerY;

    private int currentBoxCount;
    private Map<Integer, Integer> bestStars;
    private boolean isRandomLevel;

    private JLabel statusLabel;
    private JLabel winLabel;
    private JButton undoButton;
    private JButton resetButton;
    private JButton prevButton;
    private JButton nextButton;
    private JButton randomButton;
    private JButton hintButton;
    private JButton starsButton;

    private Point hintBoxPosition;
    private Timer hintTimer;
    private boolean hintFlash;

    private static final int TILE_SIZE = 64;
    private static final int BOARD_WIDTH = 800;
    private static final int BOARD_HEIGHT = 700;

    private Image backgroundImage;

    public GamePanel() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(new Color(30, 30, 30));
        setFocusable(true);
        requestFocusInWindow();

        bestStars = new HashMap<>();
        for (int i = 0; i < LevelLoader.getLevelCount(); i++) {
            bestStars.put(i, 0);
        }

        loadBackgroundImage();
        initComponents();
        loadLevel(0);

        AudioManager.playBackgroundMusic();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
    }

    private void loadBackgroundImage() {
        java.net.URL url = getClass().getResource("/res/images/background.png");
        if (url == null) {
            url = getClass().getResource("/images/background.png");
        }
        if (url != null) {
            backgroundImage = new ImageIcon(url).getImage();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(50, 50, 50));
        statusLabel = new JLabel("关卡: 1 / 3    步数: 0    最佳: ☆☆☆");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        topPanel.add(statusLabel);

        winLabel = new JLabel("");
        winLabel.setForeground(new Color(255, 215, 0));
        winLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        topPanel.add(winLabel);
        add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(50, 50, 50));

        undoButton = new JButton("撤销 (Z)");
        resetButton = new JButton("重置 (R)");
        prevButton = new JButton("上一关");
        nextButton = new JButton("下一关");
        randomButton = new JButton("随机关卡");
        hintButton = new JButton("💡 提示");
        starsButton = new JButton("⭐ 星级记录");

        styleButton(undoButton);
        styleButton(resetButton);
        styleButton(prevButton);
        styleButton(nextButton);
        styleButton(randomButton);
        styleButton(hintButton);
        styleButton(starsButton);

        undoButton.addActionListener(this::undoAction);
        resetButton.addActionListener(this::resetAction);
        prevButton.addActionListener(this::prevLevelAction);
        nextButton.addActionListener(this::nextLevelAction);
        randomButton.addActionListener(this::randomLevelAction);
        hintButton.addActionListener(this::hintAction);
        starsButton.addActionListener(this::showStarsRecord);

        buttonPanel.add(undoButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(randomButton);
        buttonPanel.add(hintButton);
        buttonPanel.add(starsButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(70, 70, 70));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    public void loadLevel(int index) {
        currentLevelIndex = index;
        currentLevel = LevelLoader.loadLevel(currentLevelIndex);
        isRandomLevel = false;
        currentBoxCount = countBoxes(currentLevel);

        initialEntity = currentLevel.getEntityCopy();
        initialPlayerX = currentLevel.getPlayerX();
        initialPlayerY = currentLevel.getPlayerY();

        updateStatus();
        repaint();
        requestFocusInWindow();
        winLabel.setText("");
        clearHint();
    }

    private void resetCurrentLevel() {
        if (currentLevel != null) {
            currentLevel.reset(initialEntity, initialPlayerX, initialPlayerY);
            updateStatus();
            repaint();
            requestFocusInWindow();
            winLabel.setText("");
            clearHint();
        }
    }

    private void updateStatus() {
        if (currentLevelIndex >= 0) {
            int best = bestStars.getOrDefault(currentLevelIndex, 0);
            String bestStarStr = getStarString(best);
            statusLabel.setText(String.format("关卡: %d / %d    步数: %d    最佳: %s",
                    currentLevelIndex + 1, LevelLoader.getLevelCount(),
                    currentLevel.getMoveCount(), bestStarStr));
        } else {
            statusLabel.setText(String.format("随机关卡    步数: %d", currentLevel.getMoveCount()));
        }

        if (currentLevel.isGameWin()) {
            int stars;
            if (isRandomLevel) {
                stars = LevelLoader.getRandomLevelStars(currentBoxCount, currentLevel.getMoveCount());
            } else {
                stars = LevelLoader.getFixedLevelStars(currentLevelIndex, currentLevel.getMoveCount());
            }
            winLabel.setText(" ✨ 通关成功！获得 " + getStarString(stars) + " ✨ ");
        } else {
            winLabel.setText("");
        }
    }

    private String getStarString(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < stars ? "★" : "☆");
        }
        return sb.toString();
    }

    private int countBoxes(Level level) {
        int count = 0;
        for (int y = 0; y < level.getHeight(); y++) {
            for (int x = 0; x < level.getWidth(); x++) {
                if (level.getEntity(x, y) == TileType.BOX) {
                    count++;
                }
            }
        }
        return count;
    }

    private void handleKeyPress(KeyEvent e) {
        if (currentLevel.isGameWin()) return;

        int key = e.getKeyCode();
        boolean moved = false;
        switch (key) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                moved = currentLevel.movePlayer(0, -1);
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                moved = currentLevel.movePlayer(0, 1);
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                moved = currentLevel.movePlayer(-1, 0);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                moved = currentLevel.movePlayer(1, 0);
                break;
            case KeyEvent.VK_Z:
                if (currentLevel.undo()) {
                    repaint();
                    updateStatus();
                    clearHint();
                }
                return;
            case KeyEvent.VK_R:
                resetCurrentLevel();
                repaint();
                return;
            default:
                return;
        }

        if (moved) {
            AudioManager.playMoveSound();
            repaint();
            updateStatus();
            clearHint();
            if (currentLevel.isGameWin()) {
                handleWin();
            }
        }
    }

    private void handleWin() {
        AudioManager.playWinSound();

        int stars;
        if (isRandomLevel) {
            stars = LevelLoader.getRandomLevelStars(currentBoxCount, currentLevel.getMoveCount());
        } else {
            stars = LevelLoader.getFixedLevelStars(currentLevelIndex, currentLevel.getMoveCount());
            if (stars > bestStars.getOrDefault(currentLevelIndex, 0)) {
                bestStars.put(currentLevelIndex, stars);
                updateStatus();
            }
        }

        String starString = getStarString(stars);
        int option = JOptionPane.showConfirmDialog(this,
                String.format("恭喜过关！\n步数: %d\n获得星级: %s\n是否进入下一关？",
                        currentLevel.getMoveCount(), starString),
                "胜利", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            if (currentLevelIndex >= 0 && currentLevelIndex + 1 < LevelLoader.getLevelCount()) {
                loadLevel(currentLevelIndex + 1);
            } else if (currentLevelIndex == -1) {
                loadLevel(0);
            } else {
                JOptionPane.showMessageDialog(this, "已经是最后一关了！");
            }
        }
        requestFocusInWindow(); // 对话框关闭后重新获取焦点
    }

    private void undoAction(ActionEvent e) {
        if (currentLevel.undo()) {
            repaint();
            updateStatus();
            requestFocusInWindow();
            clearHint();
        }
    }

    private void resetAction(ActionEvent e) {
        resetCurrentLevel();
        repaint();
        requestFocusInWindow();
    }

    private void prevLevelAction(ActionEvent e) {
        if (currentLevelIndex > 0) {
            loadLevel(currentLevelIndex - 1);
        } else if (currentLevelIndex == -1) {
            loadLevel(LevelLoader.getLevelCount() - 1);
        } else {
            JOptionPane.showMessageDialog(this, "已经是第一关了！");
        }
        requestFocusInWindow();
    }

    private void nextLevelAction(ActionEvent e) {
        if (currentLevelIndex >= 0 && currentLevelIndex + 1 < LevelLoader.getLevelCount()) {
            loadLevel(currentLevelIndex + 1);
        } else if (currentLevelIndex == -1) {
            loadLevel(0);
        } else {
            JOptionPane.showMessageDialog(this, "已经是最后一关了！");
        }
        requestFocusInWindow();
    }

    private void randomLevelAction(ActionEvent e) {
        Level newLevel = LevelLoader.generateRandomLevel();
        if (newLevel != null) {
            currentLevel = newLevel;
            isRandomLevel = true;
            currentLevelIndex = -1;
            currentBoxCount = countBoxes(currentLevel);

            initialEntity = currentLevel.getEntityCopy();
            initialPlayerX = currentLevel.getPlayerX();
            initialPlayerY = currentLevel.getPlayerY();

            updateStatus();
            repaint();
            requestFocusInWindow();
            winLabel.setText("");
            clearHint();
        } else {
            JOptionPane.showMessageDialog(this, "随机生成失败，请重试");
            requestFocusInWindow();
        }
    }

    // ==================== 提示功能（已修复焦点丢失） ====================
    private void hintAction(ActionEvent e) {
        if (currentLevel.isGameWin()) {
            JOptionPane.showMessageDialog(this, "游戏已胜利，无需提示！");
            requestFocusInWindow();
            return;
        }

        Point hintBox = findPushableBox();
        if (hintBox == null) {
            JOptionPane.showMessageDialog(this, "暂无合适提示，请尝试其他操作。");
            requestFocusInWindow();
            return;
        }

        hintBoxPosition = hintBox;
        startHintFlash();

        String direction = getPushDirection(hintBox);
        if (direction != null) {
            System.out.println("提示：箱子位于 (" + hintBox.x + "," + hintBox.y + ")，建议" + direction + "推");
        }
        requestFocusInWindow(); // 关键：重新获得键盘焦点
    }

    private Point findPushableBox() {
        int w = currentLevel.getWidth();
        int h = currentLevel.getHeight();
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        boolean[][] reachable = computePlayerReachableIgnoreBoxes();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (currentLevel.getEntity(x, y) == TileType.BOX) {
                    for (int dir = 0; dir < 4; dir++) {
                        int pushX = x + dx[dir];
                        int pushY = y + dy[dir];
                        int backX = x - dx[dir];
                        int backY = y - dy[dir];
                        if (pushX >= 0 && pushX < w && pushY >= 0 && pushY < h &&
                                backX >= 0 && backX < w && backY >= 0 && backY < h) {
                            if (currentLevel.getGround(pushX, pushY) != TileType.WALL &&
                                    currentLevel.getEntity(pushX, pushY) != TileType.BOX) {
                                if (reachable[backY][backX]) {
                                    return new Point(x, y);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean[][] computePlayerReachableIgnoreBoxes() {
        int w = currentLevel.getWidth();
        int h = currentLevel.getHeight();
        boolean[][] visited = new boolean[h][w];
        java.util.Queue<Point> queue = new java.util.LinkedList<>();
        Point player = new Point(currentLevel.getPlayerX(), currentLevel.getPlayerY());
        queue.add(player);
        visited[player.y][player.x] = true;
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            for (int i = 0; i < 4; i++) {
                int nx = p.x + dx[i];
                int ny = p.y + dy[i];
                if (nx >= 0 && nx < w && ny >= 0 && ny < h && !visited[ny][nx]) {
                    if (currentLevel.getGround(nx, ny) != TileType.WALL &&
                            currentLevel.getEntity(nx, ny) != TileType.BOX) {
                        visited[ny][nx] = true;
                        queue.add(new Point(nx, ny));
                    }
                }
            }
        }
        return visited;
    }

    private String getPushDirection(Point box) {
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        String[] dirName = {"右", "左", "下", "上"};
        for (int i = 0; i < 4; i++) {
            int pushX = box.x + dx[i];
            int pushY = box.y + dy[i];
            int backX = box.x - dx[i];
            int backY = box.y - dy[i];
            if (pushX >= 0 && pushX < currentLevel.getWidth() && pushY >= 0 && pushY < currentLevel.getHeight() &&
                    backX >= 0 && backX < currentLevel.getWidth() && backY >= 0 && backY < currentLevel.getHeight()) {
                if (currentLevel.getGround(pushX, pushY) != TileType.WALL &&
                        currentLevel.getEntity(pushX, pushY) != TileType.BOX) {
                    boolean[][] reachable = computePlayerReachableIgnoreBoxes();
                    if (reachable[backY][backX]) {
                        return dirName[i];
                    }
                }
            }
        }
        return null;
    }

    private void startHintFlash() {
        if (hintTimer != null && hintTimer.isRunning()) {
            hintTimer.stop();
        }
        hintFlash = false;
        hintTimer = new Timer(200, new ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                hintFlash = !hintFlash;
                repaint();
                count++;
                if (count >= 10) {
                    hintTimer.stop();
                    hintBoxPosition = null;
                    repaint();
                }
            }
        });
        hintTimer.start();
    }

    private void clearHint() {
        if (hintTimer != null && hintTimer.isRunning()) {
            hintTimer.stop();
        }
        hintBoxPosition = null;
        hintFlash = false;
        repaint();
    }

    private void showStarsRecord(ActionEvent e) {
        StringBuilder sb = new StringBuilder();
        sb.append("固定关卡最佳星级记录：\n\n");
        for (int i = 0; i < LevelLoader.getLevelCount(); i++) {
            int stars = bestStars.getOrDefault(i, 0);
            String starString = getStarString(stars);
            sb.append(String.format("第 %d 关： %s\n", i + 1, starString));
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "星级记录", JOptionPane.INFORMATION_MESSAGE);
        requestFocusInWindow();
    }

    // ==================== 绘制 ====================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        if (currentLevel == null) return;

        int width = currentLevel.getWidth();
        int height = currentLevel.getHeight();

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int offsetX = (panelWidth - width * TILE_SIZE) / 2;
        int offsetY = (panelHeight - height * TILE_SIZE) / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int drawX = offsetX + x * TILE_SIZE;
                int drawY = offsetY + y * TILE_SIZE;
                TileType tile = currentLevel.getDisplayTile(x, y);
                drawTile(g2d, drawX, drawY, tile);

                if (hintBoxPosition != null && hintBoxPosition.x == x && hintBoxPosition.y == y && hintFlash) {
                    g2d.setColor(new Color(0, 255, 0, 150));
                    g2d.setStroke(new BasicStroke(4));
                    g2d.drawRect(drawX + 2, drawY + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                }
            }
        }

        if (currentLevel.isGameWin()) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, panelWidth, panelHeight);
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 36));
            String msg = "胜利!";
            FontMetrics fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(msg);
            g2d.drawString(msg, (panelWidth - msgWidth) / 2, panelHeight / 2);
        }
    }

    private void drawTile(Graphics2D g, int x, int y, TileType tile) {
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRect(x + 2, y + 2, TILE_SIZE, TILE_SIZE);

        switch (tile) {
            case WALL:
                g.setColor(new Color(80, 80, 80));
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                g.setColor(new Color(50, 50, 50));
                g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                break;
            case FLOOR:
                g.setColor(new Color(220, 200, 160));
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                g.setColor(new Color(180, 150, 110));
                g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                break;
            case TARGET:
                g.setColor(new Color(220, 200, 160));
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                g.setColor(new Color(255, 100, 100));
                g.fillOval(x + TILE_SIZE/4, y + TILE_SIZE/4, TILE_SIZE/2, TILE_SIZE/2);
                g.setColor(new Color(255, 50, 50));
                g.drawOval(x + TILE_SIZE/4, y + TILE_SIZE/4, TILE_SIZE/2, TILE_SIZE/2);
                break;
            case BOX:
                g.setColor(new Color(160, 100, 40));
                g.fillRect(x + 4, y + 4, TILE_SIZE - 8, TILE_SIZE - 8);
                g.setColor(new Color(120, 70, 20));
                g.drawRect(x + 4, y + 4, TILE_SIZE - 8, TILE_SIZE - 8);
                g.setColor(new Color(100, 60, 20));
                g.drawLine(x + 8, y + TILE_SIZE/2, x + TILE_SIZE - 8, y + TILE_SIZE/2);
                g.drawLine(x + TILE_SIZE/2, y + 8, x + TILE_SIZE/2, y + TILE_SIZE - 8);
                break;
            case BOX_ON_TARGET:
                g.setColor(new Color(220, 200, 160));
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                g.setColor(new Color(255, 100, 100));
                g.fillOval(x + TILE_SIZE/4, y + TILE_SIZE/4, TILE_SIZE/2, TILE_SIZE/2);
                g.setColor(new Color(160, 100, 40));
                g.fillRect(x + 4, y + 4, TILE_SIZE - 8, TILE_SIZE - 8);
                g.setColor(new Color(100, 60, 20));
                g.drawLine(x + 8, y + TILE_SIZE/2, x + TILE_SIZE - 8, y + TILE_SIZE/2);
                g.drawLine(x + TILE_SIZE/2, y + 8, x + TILE_SIZE/2, y + TILE_SIZE - 8);
                break;
            case PLAYER:
                g.setColor(new Color(220, 200, 160));
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                g.setColor(new Color(70, 130, 200));
                g.fillOval(x + 8, y + 8, TILE_SIZE - 16, TILE_SIZE - 16);
                g.setColor(Color.WHITE);
                g.fillOval(x + TILE_SIZE/3, y + TILE_SIZE/3, 6, 6);
                g.fillOval(x + TILE_SIZE*2/3 - 6, y + TILE_SIZE/3, 6, 6);
                g.setColor(new Color(50, 50, 50));
                g.fillOval(x + TILE_SIZE/3 + 2, y + TILE_SIZE/3 + 2, 3, 3);
                g.fillOval(x + TILE_SIZE*2/3 - 4, y + TILE_SIZE/3 + 2, 3, 3);
                g.drawArc(x + TILE_SIZE/3, y + TILE_SIZE*2/3 - 6, TILE_SIZE/3, TILE_SIZE/6, 0, -180);
                break;
            case PLAYER_ON_TARGET:
                g.setColor(new Color(220, 200, 160));
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                g.setColor(new Color(255, 100, 100));
                g.fillOval(x + TILE_SIZE/4, y + TILE_SIZE/4, TILE_SIZE/2, TILE_SIZE/2);
                g.setColor(new Color(70, 130, 200));
                g.fillOval(x + 8, y + 8, TILE_SIZE - 16, TILE_SIZE - 16);
                g.setColor(Color.WHITE);
                g.fillOval(x + TILE_SIZE/3, y + TILE_SIZE/3, 6, 6);
                g.fillOval(x + TILE_SIZE*2/3 - 6, y + TILE_SIZE/3, 6, 6);
                g.setColor(new Color(50, 50, 50));
                g.fillOval(x + TILE_SIZE/3 + 2, y + TILE_SIZE/3 + 2, 3, 3);
                g.fillOval(x + TILE_SIZE*2/3 - 4, y + TILE_SIZE/3 +    2, 3, 3);
                g.drawArc(x + TILE_SIZE/3, y + TILE_SIZE*2/3 - 6, TILE_SIZE/3, TILE_SIZE/6, 0, -180);
                break;
            default:
                g.setColor(Color.GRAY);
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                break;
        }
    }

    private static class Point {
        int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
    }
}