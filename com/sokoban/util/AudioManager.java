package com.sokoban.util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioManager {
    private static AudioManager instance;
    private Clip bgmClip, effectClip;
    private boolean muted = false;

    // ⚠️ 请修改为你的真实绝对路径（最后不要忘记斜杠）
    private static final String AUDIO_BASE_PATH = "D:/Java program/Box pushing/Box pushing/res/audio/";

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    public boolean isMuted() { return muted; }

    public void toggleMute() {
        muted = !muted;
        if (muted) {
            stopBGM();
        } else {
            playBGM("bgm.wav");
        }
    }

    public void playBGM(String fileName) {
        if (muted) return;
        stopBGM();
        try {
            File file = new File(AUDIO_BASE_PATH + fileName);
            System.out.println("尝试播放背景音乐: " + file.getAbsolutePath());
            if (!file.exists()) {
                System.err.println("背景音乐文件不存在: " + file.getAbsolutePath());
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
            System.out.println("背景音乐播放成功");
        } catch (Exception e) {
            System.err.println("背景音乐播放失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }

    public void playEffect(String fileName) {
        if (muted) return;
        try {
            File file = new File(AUDIO_BASE_PATH + fileName);
            if (!file.exists()) {
                System.err.println("音效文件不存在: " + file.getAbsolutePath());
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            effectClip = AudioSystem.getClip();
            effectClip.open(ais);
            effectClip.start();
            System.out.println("播放音效: " + fileName);
        } catch (Exception e) {
            System.err.println("音效播放失败: " + e.getMessage());
        }
    }
}