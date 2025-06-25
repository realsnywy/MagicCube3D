package com.univasf.magiccube3d.util;

import java.io.InputStream;
import java.net.URL;
import javax.sound.sampled.*;

// Importe a classe ModPlay3 se ela estiver em um pacote diferente.
// Exemplo: import com.univasf.magiccube3d.tracker.ModPlay3;

/**
 * Utilitário para reprodução de arquivos de música Tracker (e.g., .MOD).
 * <p>
 * Esta classe fornece métodos estáticos para tocar e parar músicas
 * localizadas no diretório de recursos do projeto.
 * A reprodução é feita em uma thread separada e entra em loop automaticamente.
 * </p>
 * <p>
 * **Requisito:** A classe ModPlay3 (fornecida separadamente) deve estar
 * presente no classpath do projeto para que esta classe funcione.
 * </p>
 * <p>
 * Exemplo de uso:
 *
 * <pre>
 * MusicPlayer.playMusic("song.mod");
 * // ... later
 * MusicPlayer.stopMusic();
 * </pre>
 * </p>
 */
public class MusicPlayer {

  // Taxa de amostragem utilizada para reprodução de áudio, definida como 48000 Hz, que é uma taxa comum para som de alta qualidade.
  private static final int SAMPLING_RATE = 48000;
  // Referência para a thread de reprodução de música atual, responsável por executar a lógica de reprodução e controle do áudio.
  private static MediaPlayerThread currentPlayerThread;

  /**
   * Toca um arquivo de música Tracker.
   * Se uma música já estiver tocando, ela será parada primeiro.
   * A música tocará em loop contínuo.
   *
   * @param fileName O nome do arquivo de música (e.g., "mysong.mod") localizado
   *                 em
   *                 "/com/univasf/magiccube3d/music/" ou, como fallback, em
   *                 "/com/univasf/magiccube3d/sounds/".
   */
  public static void playMusic(String fileName) {
    stopMusic(); // Para qualquer música que esteja tocando

    try {
      URL musicURL = MusicPlayer.class.getResource("/com/univasf/magiccube3d/music/" + fileName);
      if (musicURL == null) {
        musicURL = MusicPlayer.class.getResource("/com/univasf/magiccube3d/sounds/" + fileName);
      }

      if (musicURL != null) {
        currentPlayerThread = new MediaPlayerThread(musicURL);
        currentPlayerThread.start();
      } else {
        System.err.println("Arquivo de música não encontrado: " + fileName);
      }
    } catch (Exception e) {
      System.err.println("Erro ao iniciar a reprodução da música: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Para a reprodução da música atual, se houver alguma tocando.
   */
  public static void stopMusic() {
    if (currentPlayerThread != null) {
      currentPlayerThread.stopPlayback();
      try {
        currentPlayerThread.join(1000); // Espera a thread terminar
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      currentPlayerThread = null;
    }
  }

  private static class MediaPlayerThread extends Thread {
    private ModPlay3 modPlay3Instance;
    private SourceDataLine sourceDataLine;
    private volatile boolean running = true;
    private URL musicResourceURL;

    public MediaPlayerThread(URL url) {
      this.musicResourceURL = url;
      this.setDaemon(true); // Permite que a JVM finalize mesmo se esta thread estiver rodando
    }

    @Override
    public void run() {
      InputStream inputStream = null;
      try {
        // Tenta carregar o módulo
        try {
          inputStream = musicResourceURL.openStream();
          modPlay3Instance = new ModPlay3(inputStream, false); // Formato padrão
        } catch (IllegalArgumentException e) {
          System.err.println("Falha ao carregar módulo como formato padrão (" + e.getMessage()
              + "), tentando formato Ultimate Soundtracker.");
          if (inputStream != null) {
            try {
              inputStream.close();
            } catch (java.io.IOException ioe) {
              /* ignora */ }
          }
          inputStream = musicResourceURL.openStream(); // Reabre o stream
          modPlay3Instance = new ModPlay3(inputStream, true); // Formato Ultimate Soundtracker
        }

        if (modPlay3Instance == null) {
          System.err.println("Falha ao carregar o módulo com ambos os formatos.");
          return;
        }

        AudioFormat audioFormat = new AudioFormat(SAMPLING_RATE, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        if (!AudioSystem.isLineSupported(info)) {
          System.err.println("Linha de áudio não suportada para o formato: " + audioFormat);
          return;
        }

        sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
        int sdlBufferSize = (SAMPLING_RATE / 5) * 4; // Buffer para ~0.2s
        sourceDataLine.open(audioFormat, sdlBufferSize);

        // Define o volume global antes de iniciar a reprodução
        setLineVolume(sourceDataLine, AudioConfig.getGlobalVolume());

        sourceDataLine.start();

        int maxSamplesPerTick = (SAMPLING_RATE * 5) / (32 * 2);
        int mixBufStereoSamples = maxSamplesPerTick + 32;

        int[] mixBuf = new int[mixBufStereoSamples * 2];
        byte[] outBuf = new byte[mixBufStereoSamples * 4];

        modPlay3Instance.setSequencer(true);

        while (running) {
          // Verifica se a música terminou para fazer o loop
          if (!modPlay3Instance.getSequencer() && modPlay3Instance.getSongLength() > 0) {
            System.out.println("Música terminada. Reiniciando (loop)...");
            modPlay3Instance.setSequencePos(0, 0); // Volta para o início
            modPlay3Instance.setSequencer(true); // Reativa o sequencer
            if (sourceDataLine != null) {
              sourceDataLine.flush(); // Limpa o buffer de áudio
            }
          }

          int samplesGenerated = modPlay3Instance.getAudio(SAMPLING_RATE, mixBuf);

          if (samplesGenerated > 0) {
            ModPlay3.clip(mixBuf, outBuf, samplesGenerated * 2);
            sourceDataLine.write(outBuf, 0, samplesGenerated * 4);
          } else if (running && !modPlay3Instance.getSequencer()) {
            // Música terminou e não gerou amostras, o loop acima deve tratar.
            // Pequena pausa para evitar busy-loop em casos raros.
            try {
              Thread.sleep(20);
            } catch (InterruptedException e) {
              if (!running)
                break;
              Thread.currentThread().interrupt();
            }
          } else if (running) { // Sequencer ativo, mas 0 amostras (silêncio/delay no módulo)
            try {
              Thread.sleep(10);
            } catch (InterruptedException e) {
              if (!running)
                break;
              Thread.currentThread().interrupt();
            }
          }
          // Se !running, o loop terminará
        }
      } catch (LineUnavailableException e) {
        System.err.println("Linha de áudio indisponível: " + e.getMessage());
        e.printStackTrace();
      } catch (java.io.IOException e) {
        System.err.println("Erro de I/O ao ler dados do módulo: " + e.getMessage());
        e.printStackTrace();
      } catch (Exception e) {
        if (running) {
          System.err.println("Erro durante a reprodução da música: " + e.getMessage());
          e.printStackTrace();
        }
      } finally {
        if (sourceDataLine != null) {
          sourceDataLine.drain();
          sourceDataLine.stop();
          sourceDataLine.close();
        }
        if (inputStream != null) {
          try {
            inputStream.close();
          } catch (java.io.IOException ioe) {
            /* ignora */ }
        }
        running = false;
      }
    }

    /**
     * Define o volume do SourceDataLine, se suportado.
     */
    private void setLineVolume(SourceDataLine line, double volume) {
      try {
        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
          FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
          float min = gainControl.getMinimum();
          float max = gainControl.getMaximum();
          float gain = (float) (min + (max - min) * volume);
          gainControl.setValue(gain);
        }
      } catch (Exception e) {
        // Silenciosamente ignora se não suportado
      }
    }

    public void stopPlayback() {
      running = false;
      if (modPlay3Instance != null) {
        modPlay3Instance.setSequencer(false);
      }
      this.interrupt();
    }
  }
}
