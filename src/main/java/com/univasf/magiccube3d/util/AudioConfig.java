package com.univasf.magiccube3d.util;

public class AudioConfig {
  private static double globalVolume = 0.5; // 50%

  public static double getGlobalVolume() {
    return globalVolume;
  }

  public static void setGlobalVolume(double volume) {
    globalVolume = Math.max(0, Math.min(1, volume)); // Garante entre 0 e 1
  }
}
