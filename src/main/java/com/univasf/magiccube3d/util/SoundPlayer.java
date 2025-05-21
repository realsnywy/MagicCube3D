package com.univasf.magiccube3d.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

/**
 * Utilitário para reprodução de arquivos de áudio localizados no diretório de
 * recursos do projeto.
 * <p>
 * Esta classe fornece um método estático para tocar sons a partir do caminho
 * "/com/univasf/magiccube3d/sounds/" dentro do classpath.
 * </p>
 *
 * <p>
 * Exemplo de uso:
 *
 * <pre>
 * SoundPlayer.playSound("move.wav");
 * </pre>
 * </p>
 *
 * @author SeuNome
 */
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
