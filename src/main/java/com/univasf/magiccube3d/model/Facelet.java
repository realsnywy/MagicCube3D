package com.univasf.magiccube3d.model;

// Representa uma única peça colorida (quadradinho) na face do cubo.
public class Facelet {
    private CubeColor color;

    // Getter da cor
    public CubeColor getColor() {
        return color;
    }

    // Setter da cor
    public void setColor(CubeColor color) {
        this.color = color;
    }

    // Construtor que recebe a cor do facelet
    public Facelet(CubeColor color) {
        this.color = color;
    }

    // Metodo toString sobrescrito para facilitar a impressão (ex: WHITE)
    @Override
    public String toString() {
        return color.name();
    }

}
