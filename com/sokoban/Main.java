package com.sokoban;

import com.sokoban.ui.SokobanFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SokobanFrame());
    }
}