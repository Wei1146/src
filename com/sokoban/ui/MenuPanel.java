package com.sokoban.ui;

import com.sokoban.model.Level;
import com.sokoban.util.AudioManager;
import com.sokoban.util.LevelLoader;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MenuPanel extends JPanel {
    private JFrame parentFrame;
    private JButton startButton, musicButton;

    public MenuPanel(JFrame frame) {
        this.parentFrame = frame;
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel title = new JLabel("推箱子 Sokoban");
        title.setFont(new Font("微软雅黑", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0;
        add(title, gbc);

        startButton = new JButton("开始游戏");
        startButton.setFont(new Font("微软雅黑", Font.PLAIN, 24));
        startButton.setPreferredSize(new Dimension(200, 60));
        startButton.addActionListener(e -> startGame());
        gbc.gridy = 1;
        add(startButton, gbc);

        musicButton = new JButton(getMusicButtonText());
        musicButton.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        musicButton.addActionListener(e -> toggleMusic());
        gbc.gridy = 2;
        add(musicButton, gbc);

        JLabel info = new JLabel("使用方向键移动，将箱子推到红色圆圈上");
        info.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        info.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 3;
        add(info, gbc);
    }

    private String getMusicButtonText() {
        return AudioManager.getInstance().isMuted() ? "🔇 背景音乐 (关)" : "🔊 背景音乐 (开)";
    }

    private void toggleMusic() {
        AudioManager.getInstance().toggleMute();
        musicButton.setText(getMusicButtonText());
    }

    private void startGame() {
        List<Level> levels = LevelLoader.loadAllLevels();
        if (levels.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有关卡数据！");
            return;
        }
        GamePanel gamePanel = new GamePanel(levels, 0);
        parentFrame.getContentPane().removeAll();
        parentFrame.add(gamePanel);
        parentFrame.revalidate();
        parentFrame.pack();
        gamePanel.requestFocusInWindow();
    }
}