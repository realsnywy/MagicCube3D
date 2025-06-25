package com.univasf.magiccube3d.util;

/**
 * Classe utilitária para gerenciar as configurações de áudio globais do
 * aplicativo.
 * <p>
 * Essa classe encapsula o gerenciamento do volume global de áudio, permitindo
 * que outras partes da aplicação consultem ou modifiquem o volume em um
 * intervalo controlado (entre 0 e 1).
 * </p>
 *
 * <p>
 * Exemplo de uso:
 *
 * <pre>
 * double volumeAtual = AudioConfig.getGlobalVolume(); // Obtém o volume atual
 * AudioConfig.setGlobalVolume(0.8); // Define o volume para 80%
 * </pre>
 * </p>
 *
 * @author Desenvolvedor
 */

public class AudioConfig {

  // Volume global do sistema, armazenado como um valor entre 0 (mudo) e 1 (máximo)
  private static double globalVolume = 0.5; // 50%

  /**
   * Obtém o volume global configurado.
   *
   * @return o volume global atual, um valor entre 0 (mudo) e 1 (máximo).
   */

  public static double getGlobalVolume() {
    return globalVolume;
  }

  /**
   * Define o volume global para um valor especificado.
   *
   * @param volume O novo valor de volume desejado, deve estar dentro do
   *               intervalo [0, 1]. Caso o valor esteja fora desse intervalo,
   *               ele será ajustado automaticamente para o limite mais
   *               próximo.
   */

  public static void setGlobalVolume(double volume) {
    globalVolume = Math.max(0, Math.min(1, volume)); // Garante entre 0 e 1
  }
}
