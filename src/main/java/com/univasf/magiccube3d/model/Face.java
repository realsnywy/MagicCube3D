package com.univasf.magiccube3d.model;

import javafx.scene.paint.Color;

// Classe que representa uma face do cubo mágico (3x3)
public class Face {
    public static final int SIZE = 3; // Tamanho padrão da face (3x3)

    private Facelet[][] facelets; // Matriz de facelets da face
    private FaceType faceType; // Tipo da face (ex: UP, FRONT, etc.)

    // Retorna o facelet na posição especificada
    public Facelet getFacelet(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE)
            throw new IllegalArgumentException("Invalid indices");
        return facelets[row][col];
    }

    // Define o facelet na posição especificada
    public void setFacelet(int row, int col, Facelet facelet) {
        facelets[row][col] = facelet;
    }

    // Retorna o tipo da face
    public FaceType getFaceType() {
        return faceType;
    }

    // Construtor: inicializa a face com todos os facelets da mesma cor
    public Face(FaceType faceType, Color initialColor) {
        this.faceType = faceType;
        facelets = new Facelet[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                facelets[i][j] = new Facelet(initialColor);
            }
        }
    }

    // Rotaciona a face 90° no sentido horário
    public void rotateClockwise() {
        Facelet center = facelets[1][1]; // Salva o centro
        Facelet[][] rotated = new Facelet[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                rotated[j][SIZE - 1 - i] = facelets[i][j];
            }
        }
        rotated[1][1] = center; // Mantém o centro fixo
        facelets = rotated;
    }

    // Rotaciona a face 90° no sentido anti-horário
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

    // Imprime a face no terminal usando letras para representar as cores
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

    // Retorna a letra correspondente à cor do facelet
    private String getColorInitial(Color color) {
        if (color.equals(Color.web("FFFFFF")))
            return "W";
        if (color.equals(Color.web("FFD600")))
            return "Y";
        if (color.equals(Color.web("E53935")))
            return "R";
        if (color.equals(Color.web("FF9800")))
            return "O";
        if (color.equals(Color.web("43A047")))
            return "G";
        if (color.equals(Color.web("1E88E5")))
            return "B";
        return "?"; // Cor não reconhecida
    }
}
