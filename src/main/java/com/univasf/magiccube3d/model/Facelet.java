package com.univasf.magiccube3d.model;

// Representa uma única peça colorida (quadradinho) na face do cubo.
public class Facelet {
    private Object color; // Poderia ser javafx.scene.paint.Color ou uma enumeração de cores

    public Facelet(Object color) {
        this.color = color;
    }

    public Object getColor() {
        return color;
    }

    public void setColor(Object color) {
        this.color = color;
    }
}
