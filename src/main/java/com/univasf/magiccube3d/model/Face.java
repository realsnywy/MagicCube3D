package com.univasf.magiccube3d.model;

//Classe que modela uma face do cubo mágico baseado em facelet
public class Face {
    public static final int SIZE = 3; // Tamanho padrão para cubo 3x3

    private Facelet[][] facelets;     // Matriz 3x3 de facelets
    private FaceType faceType;        // Qual face é essa (UP, FRONT, etc.)

    // Getter de um facelet específico
    public Facelet getFacelet(int row, int col) {
        return facelets[row][col];
    }

    // Setter de um facelet específico
    public void setFacelet(int row, int col, Facelet facelet) {
        facelets[row][col] = facelet;
    }

    //Getter de um facetype
    public FaceType getFaceType() {
        return faceType;
    }

    // Construtor: inicializa todos os quadradinhos com a mesma cor
    public Face(FaceType faceType, CubeColor initialColor) {
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
        Facelet[][] rotated = new Facelet[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                rotated[j][SIZE - 1 - i] = facelets[i][j];
            }
        }
        facelets = rotated;
    }

    // (Opcional) Exibir a face no terminal
    public void printFace() {
        System.out.println("Face: " + faceType);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                System.out.print(facelets[i][j].getColor().name().charAt(0) + " ");
            }
            System.out.println();
        }
    }
}

