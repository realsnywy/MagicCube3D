package com.univasf.magiccube3d.model;

import javafx.scene.paint.Color;

// Representa uma única peça colorida (quadradinho) na face do cubo.
public class Facelet {
    private Color color;

    // Construtor que recebe a cor do facelet
    public Facelet(Color color) {
        this.color = color;
    }

    // Getter da cor
    public Color getColor() {
        return color;
    }

    // Setter da cor
    public void setColor(Color color) {
        this.color = color;
    }

    // Método toString sobrescrito para facilitar a impressão (ex: 0xffffffff)
    @Override
    public String toString() {
        return color.toString();
    }
}
