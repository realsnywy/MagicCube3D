package com.univasf.magiccube3d.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class SoundPlayer {

    public static void playSound(String fileName) {
        try {
            URL soundURL = SoundPlayer.class.getResource("/com/univasf/magiccube3d/sounds/" + fileName);
            if (soundURL != null) {
                Media media = new Media(soundURL.toString());
                MediaPlayer player = new MediaPlayer(media);
                player.play();
            } else {
                System.err.println("Arquivo de som não encontrado: " + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
