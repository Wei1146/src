package com.sokoban;

import com.sokoban.ui.GamePanel;
import com.sokoban.util.AudioManager;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("推箱子 - Sokoban");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);
            frame.pack();
            frame.setLocationRelativeTo(null);

            // 窗口关闭时停止背景音乐
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    AudioManager.stopBackgroundMusic();
                }
            });

            frame.setVisible(true);
            gamePanel.requestFocusInWindow();
        });
    }
}