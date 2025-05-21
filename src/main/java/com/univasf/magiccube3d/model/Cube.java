package com.univasf.magiccube3d.model;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.paint.Color;

// Representa o estado do Cubo de Rubik e contém a lógica para as rotações das faces.
public class Cube {

    // Mapa que associa cada tipo de face (FaceType) com o objeto Face
    // correspondente
    private final Map<FaceType, Face> faces = new HashMap<>();

    public Cube() {
        initializeSolvedState();
    }

    // Inicializa o cubo ao seu estado resolvido
    private void initializeSolvedState() {
        faces.put(FaceType.UP, new Face(FaceType.UP, Color.web("FFFFFF")));
        faces.put(FaceType.DOWN, new Face(FaceType.DOWN, Color.web("FFD600")));
        faces.put(FaceType.FRONT, new Face(FaceType.FRONT, Color.web("E53935")));
        faces.put(FaceType.BACK, new Face(FaceType.BACK, Color.web("FF9800")));
        faces.put(FaceType.LEFT, new Face(FaceType.LEFT, Color.web("1E88E5")));
        faces.put(FaceType.RIGHT, new Face(FaceType.RIGHT, Color.web("43A047")));
        System.out.println("Cubo inicializado no estado resolvido.");
    }

    // Rotaciona uma face do cubo
    public void rotateFace(String face, boolean clockwise) {
        FaceType faceType = FaceType.valueOf(face.toUpperCase());
        Face faceToRotate = faces.get(faceType);

        if (clockwise) {
            faceToRotate.rotateClockwise();
        } else {
            // Rotaciona 3 vezes no sentido horário para simular anti-horário
            for (int i = 0; i < 3; i++) {
                faceToRotate.rotateClockwise();
            }
        }

        rotateAdjacentEdges(faceType, clockwise);

        System.out.println(
                "Rotacionando face " + faceType + (clockwise ? " no sentido horário" : " no sentido anti-horário"));
    }

    // Gira as bordas das faces adjacentes à face especificada
    // In Cube.java

    private void rotateAdjacentEdges(FaceType face, boolean clockwise) {
        switch (face) {
            case FRONT -> {
                if (clockwise) {
                    rotateFront();
                } else {
                    rotateFrontPrime();
                }
            }
            case BACK -> {
                if (clockwise) {
                    rotateBack();
                } else {
                    rotateBackPrime();
                }
            }
            case UP -> {
                if (clockwise) {
                    rotateUp();
                } else {
                    rotateUpPrime();
                }
            }
            case DOWN -> {
                if (clockwise) {
                    rotateDown();
                } else {
                    rotateDownPrime();
                }
            }
            case LEFT -> {
                if (clockwise) {
                    rotateLeft();
                } else {
                    rotateLeftPrime();
                }
            }
            case RIGHT -> {
                if (clockwise) {
                    rotateRight();
                } else {
                    rotateRightPrime();
                }
            }
        }
    }

    // --- Helper methods for each face and its inverse ---

    private void rotateFront() {
        Face up = faces.get(FaceType.UP);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);
        Face right = faces.get(FaceType.RIGHT);

        Facelet[] upRow = getRowCopy(up, 2);
        Facelet[] leftCol = getColumnCopy(left, 2);
        Facelet[] downRow = getRowCopy(down, 0);
        Facelet[] rightCol = getColumnCopy(right, 0);

        setRow(up, 2, reverse(leftCol));
        setColumn(right, 0, upRow);
        setRow(down, 0, reverse(rightCol));
        setColumn(left, 2, downRow);
    }

    private void rotateFrontPrime() {
        Face up = faces.get(FaceType.UP);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);
        Face right = faces.get(FaceType.RIGHT);

        Facelet[] upRow = getRowCopy(up, 2);
        Facelet[] leftCol = getColumnCopy(left, 2);
        Facelet[] downRow = getRowCopy(down, 0);
        Facelet[] rightCol = getColumnCopy(right, 0);

        setRow(up, 2, rightCol);
        setColumn(left, 2, reverse(upRow));
        setRow(down, 0, leftCol);
        setColumn(right, 0, reverse(downRow));
    }

    private void rotateBack() {
        Face up = faces.get(FaceType.UP);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);
        Face right = faces.get(FaceType.RIGHT);

        Facelet[] upRow = getRowCopy(up, 0);
        Facelet[] leftCol = getColumnCopy(left, 0);
        Facelet[] downRow = getRowCopy(down, 2);
        Facelet[] rightCol = getColumnCopy(right, 2);

        setRow(up, 0, rightCol);
        setColumn(left, 0, reverse(upRow));
        setRow(down, 2, leftCol);
        setColumn(right, 2, reverse(downRow));
    }

    private void rotateBackPrime() {
        Face up = faces.get(FaceType.UP);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);
        Face right = faces.get(FaceType.RIGHT);

        Facelet[] upRow = getRowCopy(up, 0);
        Facelet[] leftCol = getColumnCopy(left, 0);
        Facelet[] downRow = getRowCopy(down, 2);
        Facelet[] rightCol = getColumnCopy(right, 2);

        setRow(up, 0, reverse(leftCol));
        setColumn(right, 2, upRow);
        setRow(down, 2, reverse(rightCol));
        setColumn(left, 0, downRow);
    }

    private void rotateUp() {
        Face front = faces.get(FaceType.FRONT);
        Face right = faces.get(FaceType.RIGHT);
        Face back = faces.get(FaceType.BACK);
        Face left = faces.get(FaceType.LEFT);

        Facelet[] frontRow = getRowCopy(front, 0);
        Facelet[] rightRow = getRowCopy(right, 0);
        Facelet[] backRow = getRowCopy(back, 0);
        Facelet[] leftRow = getRowCopy(left, 0);

        setRow(front, 0, leftRow);
        setRow(right, 0, frontRow);
        setRow(back, 0, rightRow);
        setRow(left, 0, backRow);
    }

    private void rotateUpPrime() {
        Face front = faces.get(FaceType.FRONT);
        Face right = faces.get(FaceType.RIGHT);
        Face back = faces.get(FaceType.BACK);
        Face left = faces.get(FaceType.LEFT);

        Facelet[] frontRow = getRowCopy(front, 0);
        Facelet[] rightRow = getRowCopy(right, 0);
        Facelet[] backRow = getRowCopy(back, 0);
        Facelet[] leftRow = getRowCopy(left, 0);

        setRow(front, 0, rightRow);
        setRow(left, 0, frontRow);
        setRow(back, 0, leftRow);
        setRow(right, 0, backRow);
    }

    private void rotateDown() {
        Face front = faces.get(FaceType.FRONT);
        Face right = faces.get(FaceType.RIGHT);
        Face back = faces.get(FaceType.BACK);
        Face left = faces.get(FaceType.LEFT);

        Facelet[] frontRow = getRowCopy(front, 2);
        Facelet[] rightRow = getRowCopy(right, 2);
        Facelet[] backRow = getRowCopy(back, 2);
        Facelet[] leftRow = getRowCopy(left, 2);

        setRow(front, 2, rightRow);
        setRow(right, 2, backRow);
        setRow(back, 2, leftRow);
        setRow(left, 2, frontRow);
    }

    private void rotateDownPrime() {
        Face front = faces.get(FaceType.FRONT);
        Face right = faces.get(FaceType.RIGHT);
        Face back = faces.get(FaceType.BACK);
        Face left = faces.get(FaceType.LEFT);

        Facelet[] frontRow = getRowCopy(front, 2);
        Facelet[] rightRow = getRowCopy(right, 2);
        Facelet[] backRow = getRowCopy(back, 2);
        Facelet[] leftRow = getRowCopy(left, 2);

        setRow(front, 2, leftRow);
        setRow(right, 2, frontRow);
        setRow(back, 2, rightRow);
        setRow(left, 2, backRow);
    }

    private void rotateLeft() {
        Face up = faces.get(FaceType.UP);
        Face front = faces.get(FaceType.FRONT);
        Face down = faces.get(FaceType.DOWN);
        Face back = faces.get(FaceType.BACK);

        Facelet[] upCol = getColumnCopy(up, 0);
        Facelet[] frontCol = getColumnCopy(front, 0);
        Facelet[] downCol = getColumnCopy(down, 0);
        Facelet[] backCol = getColumnCopy(back, 2);

        setColumn(up, 0, backCol);
        setColumn(front, 0, upCol);
        setColumn(down, 0, frontCol);
        setColumn(back, 2, downCol);
    }

    private void rotateLeftPrime() {
        Face up = faces.get(FaceType.UP);
        Face front = faces.get(FaceType.FRONT);
        Face down = faces.get(FaceType.DOWN);
        Face back = faces.get(FaceType.BACK);

        Facelet[] upCol = getColumnCopy(up, 0);
        Facelet[] frontCol = getColumnCopy(front, 0);
        Facelet[] downCol = getColumnCopy(down, 0);
        Facelet[] backCol = getColumnCopy(back, 2);

        setColumn(up, 0, frontCol);
        setColumn(back, 2, upCol);
        setColumn(down, 0, backCol);
        setColumn(front, 0, downCol);
    }

    private void rotateRight() {
        Face up = faces.get(FaceType.UP);
        Face front = faces.get(FaceType.FRONT);
        Face down = faces.get(FaceType.DOWN);
        Face back = faces.get(FaceType.BACK);

        Facelet[] upCol = getColumnCopy(up, 2);
        Facelet[] frontCol = getColumnCopy(front, 2);
        Facelet[] downCol = getColumnCopy(down, 2);
        Facelet[] backCol = getColumnCopy(back, 0);

        setColumn(up, 2, frontCol);
        setColumn(back, 0, upCol);
        setColumn(down, 2, backCol);
        setColumn(front, 2, downCol);
    }

    private void rotateRightPrime() {
        Face up = faces.get(FaceType.UP);
        Face front = faces.get(FaceType.FRONT);
        Face down = faces.get(FaceType.DOWN);
        Face back = faces.get(FaceType.BACK);

        Facelet[] upCol = getColumnCopy(up, 2);
        Facelet[] frontCol = getColumnCopy(front, 2);
        Facelet[] downCol = getColumnCopy(down, 2);
        Facelet[] backCol = getColumnCopy(back, 0);

        setColumn(up, 2, backCol);
        setColumn(front, 2, upCol);
        setColumn(down, 2, frontCol);
        setColumn(back, 0, downCol);
    }

    // Métodos auxiliares para copiar linhas/colunas (evita referência direta)
    private Facelet[] getRowCopy(Face face, int row) {
        Facelet[] arr = new Facelet[Face.SIZE];
        for (int i = 0; i < Face.SIZE; i++) {
            arr[i] = face.getFacelet(row, i);
        }
        return arr;
    }

    private Facelet[] getColumnCopy(Face face, int col) {
        Facelet[] arr = new Facelet[Face.SIZE];
        for (int i = 0; i < Face.SIZE; i++) {
            arr[i] = face.getFacelet(i, col);
        }
        return arr;
    }

    private void setRow(Face face, int row, Facelet[] arr) {
        for (int i = 0; i < Face.SIZE; i++) {
            face.setFacelet(row, i, new Facelet(arr[i].getColor()));
        }
    }

    private void setColumn(Face face, int col, Facelet[] arr) {
        for (int i = 0; i < Face.SIZE; i++) {
            face.setFacelet(i, col, new Facelet(arr[i].getColor()));
        }
    }

    private Facelet[] reverse(Facelet[] arr) {
        Facelet[] reversed = new Facelet[arr.length];
        for (int i = 0; i < arr.length; i++) {
            reversed[i] = arr[arr.length - 1 - i];
        }
        return reversed;
    }

    // Exibe todas as faces do cubo no terminal
    public void printCube() {
        for (FaceType type : FaceType.values()) {
            faces.get(type).printFace();
            System.out.println();
        }
    }

    // Getter para acessar uma face específica
    public Face getFace(FaceType type) {
        return faces.get(type);
    }
}
