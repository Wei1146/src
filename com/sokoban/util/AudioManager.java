package com.sokoban.util;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

/**
 * 音频管理类：负责播放移动音效、胜利音效和背景音乐
 */
public class AudioManager {
    private static Clip bgmClip;
    private static boolean bgmPlaying = false;

    /**
     * 播放移动音效（短促）
     */
    public static void playMoveSound() {
        playSound("move.wav");
    }

    /**
     * 播放胜利音效（短促）
     */
    public static void playWinSound() {
        playSound("win.wav");
    }

    /**
     * 启动背景音乐（循环播放）
     */
    public static void playBackgroundMusic() {
        if (bgmPlaying) return;
        bgmClip = playSoundLoop("bgm.wav");
        if (bgmClip != null) {
            bgmPlaying = true;
        }
    }

    /**
     * 停止背景音乐（窗口关闭时调用）
     */
    public static void stopBackgroundMusic() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
            bgmPlaying = false;
        }
    }

    /**
     * 播放一次短音效（非循环）
     */
    private static void playSound(String fileName) {
        try {
            URL url = getAudioUrl(fileName);
            if (url == null) return;
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();  // 异步播放，不阻塞
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            // 忽略异常，保证游戏正常运行
        }
    }

    /**
     * 播放循环音效（用于背景音乐）
     */
    private static Clip playSoundLoop(String fileName) {
        try {
            URL url = getAudioUrl(fileName);
            if (url == null) return null;
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            return clip;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 统一资源查找：优先从 /res/audio/ 找，再尝试 /audio/
     */
    private static URL getAudioUrl(String fileName) {
        URL url = AudioManager.class.getResource("/res/audio/" + fileName);
        if (url == null) {
            url = AudioManager.class.getResource("/audio/" + fileName);
        }
        return url;
    }
}