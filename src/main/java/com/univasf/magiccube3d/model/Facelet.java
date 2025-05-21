package com.univasf.magiccube3d.model;

import javafx.scene.paint.Color;

// Representa um quadrado colorido em uma das faces do cubo mágico.
public class Facelet {
    private Color color;

    // Construtor que define a cor do facelet.
    public Facelet(Color color) {
        this.color = color;
    }

    // Retorna a cor do facelet.
    public Color getColor() {
        return color;
    }

    // Define a cor do facelet.
    public void setColor(Color color) {
        this.color = color;
    }

    // Retorna uma representação em string da cor.
    @Override
    public String toString() {
        return color.toString();
    }
}
