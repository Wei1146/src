package com.sokoban.ui;

import com.sokoban.model.Level;
import com.sokoban.util.AudioManager;
import com.sokoban.util.LevelLoader;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SokobanFrame extends JFrame {
    private GamePanel gamePanel;
    private List<Level> levels;
    private int currentLevelIndex = 0;
    private JMenuItem musicToggleItem;

    public SokobanFrame() {
        setTitle("推箱子v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        levels = LevelLoader.loadAllLevels();
        if (levels.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有关卡数据！");
            System.exit(0);
        }

        gamePanel = new GamePanel(levels, currentLevelIndex);

        setLayout(new BorderLayout());
        setJMenuBar(createMenuBar());
        add(gamePanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        // 初始焦点
        gamePanel.requestFocusInWindow();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu optionMenu = new JMenu("选项");
        JMenuItem restartItem = new JMenuItem("重玩");
        restartItem.addActionListener(e -> {
            gamePanel.restartLevel();
            gamePanel.requestFocusInWindow();
        });
        optionMenu.add(restartItem);

        JMenuItem undoItem = new JMenuItem("悔一步");
        undoItem.addActionListener(e -> {
            gamePanel.undoMove();
            gamePanel.requestFocusInWindow();
        });
        optionMenu.add(undoItem);

        optionMenu.addSeparator();

        JMenuItem prevItem = new JMenuItem("上一关");
        prevItem.addActionListener(e -> {
            prevLevel();
            gamePanel.requestFocusInWindow();
        });
        optionMenu.add(prevItem);

        JMenuItem nextItem = new JMenuItem("下一关");
        nextItem.addActionListener(e -> {
            nextLevel();
            gamePanel.requestFocusInWindow();
        });
        optionMenu.add(nextItem);

        JMenuItem finalItem = new JMenuItem("最终关");
        finalItem.addActionListener(e -> {
            finalLevel();
            gamePanel.requestFocusInWindow();
        });
        optionMenu.add(finalItem);

        JMenuItem jumpItem = new JMenuItem("送关");
        jumpItem.addActionListener(e -> {
            jumpToLevel();
            gamePanel.requestFocusInWindow();
        });
        optionMenu.add(jumpItem);

        menuBar.add(optionMenu);

        JMenu musicMenu = new JMenu("设置音乐");
        musicToggleItem = new JMenuItem(getMusicText());
        musicToggleItem.addActionListener(e -> {
            toggleMusic();
            gamePanel.requestFocusInWindow();
        });
        musicMenu.add(musicToggleItem);

        JMenuItem changeMusicItem = new JMenuItem("更换音乐");
        changeMusicItem.addActionListener(e -> {
            changeMusic();
            gamePanel.requestFocusInWindow();
        });
        musicMenu.add(changeMusicItem);

        menuBar.add(musicMenu);

        JMenu helpMenu = new JMenu("帮助");
        JMenuItem helpItem = new JMenuItem("帮助");
        helpItem.addActionListener(e -> showHelp());
        helpMenu.add(helpItem);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(new Color(240, 240, 240));

        String[] buttonNames = {"重玩", "悔一步", "上一关", "下一关", "最终关", "送关", "音乐关", "更换音乐", "默认", "收藏", "分享"};
        for (String name : buttonNames) {
            JButton btn = new JButton(name);
            btn.addActionListener(e -> {
                switch (name) {
                    case "重玩": gamePanel.restartLevel(); break;
                    case "悔一步": gamePanel.undoMove(); break;
                    case "上一关": prevLevel(); break;
                    case "下一关": nextLevel(); break;
                    case "最终关": finalLevel(); break;
                    case "送关": jumpToLevel(); break;
                    case "音乐关": toggleMusic(); break;
                    case "更换音乐": changeMusic(); break;
                    case "默认": setDefaultMusic(); break;
                    case "收藏": favorite(); break;
                    case "分享": share(); break;
                }
                gamePanel.requestFocusInWindow();
            });
            panel.add(btn);
        }
        return panel;
    }

    private void prevLevel() {
        if (currentLevelIndex > 0) {
            currentLevelIndex--;
            loadLevel(currentLevelIndex);
        } else {
            JOptionPane.showMessageDialog(this, "已是第一关");
        }
    }

    private void nextLevel() {
        if (currentLevelIndex < levels.size() - 1) {
            currentLevelIndex++;
            loadLevel(currentLevelIndex);
        } else {
            JOptionPane.showMessageDialog(this, "已是最后一关");
        }
    }

    private void finalLevel() {
        currentLevelIndex = levels.size() - 1;
        loadLevel(currentLevelIndex);
    }

    private void jumpToLevel() {
        String input = JOptionPane.showInputDialog(this, "输入关卡号 (1-" + levels.size() + "):", "送关", JOptionPane.PLAIN_MESSAGE);
        try {
            int levelNum = Integer.parseInt(input);
            if (levelNum >= 1 && levelNum <= levels.size()) {
                currentLevelIndex = levelNum - 1;
                loadLevel(currentLevelIndex);
            } else {
                JOptionPane.showMessageDialog(this, "无效关卡号");
            }
        } catch (NumberFormatException ex) {
            // ignore
        }
    }

    private void loadLevel(int index) {
        currentLevelIndex = index;
        gamePanel.loadLevel(levels.get(currentLevelIndex));
        setTitle("推箱子v2.0 - 第" + (currentLevelIndex + 1) + "关");
        // 关键：重新获取焦点
        gamePanel.requestFocusInWindow();
    }

    private void toggleMusic() {
        AudioManager.getInstance().toggleMute();
        musicToggleItem.setText(getMusicText());
    }

    private String getMusicText() {
        return AudioManager.getInstance().isMuted() ? "开启音乐" : "关闭音乐";
    }

    private void changeMusic() {
        JFileChooser chooser = new JFileChooser("res/audio");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("音频文件", "wav"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            AudioManager.getInstance().playBGM(path);
        }
    }

    private void setDefaultMusic() {
        AudioManager.getInstance().playBGM("bgm.wav");
    }

    private void favorite() {
        JOptionPane.showMessageDialog(this, "收藏功能待实现");
    }

    private void share() {
        JOptionPane.showMessageDialog(this, "分享功能待实现");
    }

    private void showHelp() {
        JOptionPane.showMessageDialog(this, "推箱子游戏规则：\n使用方向键移动玩家，将箱子推到红色圆圈上。\n支持悔步、重玩、关卡切换。");
    }
}