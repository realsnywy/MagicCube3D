package com.univasf.magiccube3d.model;

import javafx.scene.paint.Color;

// Classe que modela uma face do cubo mágico baseado em facelet
public class Face {
    public static final int SIZE = 3; // Tamanho padrão para cubo 3x3

    private Facelet[][] facelets; // Matriz 3x3 de facelets
    private FaceType faceType; // Qual face é essa (UP, FRONT, etc.)

    // Getter de um facelet específico
    public Facelet getFacelet(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE)
            throw new IllegalArgumentException("Invalid indices");
        return facelets[row][col];
    }


    // Setter de um facelet específico
    public void setFacelet(int row, int col, Facelet facelet) {
        facelets[row][col] = facelet;
    }

    // Getter de um facetype
    public FaceType getFaceType() {
        return faceType;
    }

    // Construtor: inicializa todos os quadradinhos com a mesma cor
    public Face(FaceType faceType, Color initialColor) {
        this.faceType = faceType;
        facelets = new Facelet[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                facelets[i][j] = new Facelet(initialColor);
            }
        }
    }

    // Roda a face no sentido horário (90°)
    public void rotateClockwise() {
        Facelet center = facelets[1][1]; // Salva o centro
        Facelet[][] rotated = new Facelet[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                rotated[j][SIZE - 1 - i] = facelets[i][j];
            }
        }
        rotated[1][1] = center; // Restaura o centro
        facelets = rotated;
    }

    public void rotateCounterClockwise() {
        Facelet center = facelets[1][1];
        Facelet[][] rotated = new Facelet[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                rotated[SIZE - 1 - j][i] = facelets[i][j];
            }
        }
        rotated[1][1] = center;
        facelets = rotated;
    }


    // Exibir a face no terminal (usando representação aproximada da cor)
    public void printFace() {
        System.out.println("Face: " + faceType);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Color c = facelets[i][j].getColor();
                System.out.print(getColorInitial(c) + " ");
            }
            System.out.println();
        }
    }

    // Método auxiliar para representar cores por letras
    private String getColorInitial(Color color) {
        if (color.equals(Color.WHITE))
            return "W";
        if (color.equals(Color.YELLOW))
            return "Y";
        if (color.equals(Color.RED))
            return "R";
        if (color.equals(Color.ORANGE))
            return "O";
        if (color.equals(Color.GREEN))
            return "G";
        if (color.equals(Color.BLUE))
            return "B";
        return "?"; // Cor desconhecida
    }
}
