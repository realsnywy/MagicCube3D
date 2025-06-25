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
 * @author Anderson Vicente (@4ndersu)
 */
public class SoundPlayer {

    public static void playSound(String fileName) {
        try {
            // Obtém a URL completa do arquivo de som baseado no nome fornecido
            URL soundURL = SoundPlayer.class.getResource("/com/univasf/magiccube3d/sounds/" + fileName);

            if (soundURL != null) {
                // Cria uma instância de Media com a URL do arquivo e um MediaPlayer para gerenciar a reprodução do som
                Media media = new Media(soundURL.toString());
                MediaPlayer player = new MediaPlayer(media);
                // Define o volume global
                player.setVolume(AudioConfig.getGlobalVolume());
                player.play();
            } else {
                // Exibe uma mensagem de erro se o arquivo de som não for encontrado
                System.err.println("Arquivo de som não encontrado: " + fileName);
            }
        } catch (Exception e) {
            // Tratamento de exceções: imprime a pilha de erros no console
            e.printStackTrace();
        }
    }
}
