package com.univasf.magiccube3d.model;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.paint.Color;

// Representa o estado de um Cubo Mágico 3x3 e implementa a lógica de rotação das faces e camadas.
public class Cube {

    // Mapeia cada FaceType para sua respectiva Face
    private final Map<FaceType, Face> faces = new HashMap<>();

    public Cube() {
        initializeSolvedState();
    }

    // Inicializa o cubo no estado resolvido (cada face com uma cor uniforme)
    private void initializeSolvedState() {
        faces.put(FaceType.UP, new Face(FaceType.UP, Color.web("FFFFFF")));
        faces.put(FaceType.DOWN, new Face(FaceType.DOWN, Color.web("FFD600")));
        faces.put(FaceType.FRONT, new Face(FaceType.FRONT, Color.web("E53935")));
        faces.put(FaceType.BACK, new Face(FaceType.BACK, Color.web("FF9800")));
        faces.put(FaceType.LEFT, new Face(FaceType.LEFT, Color.web("1E88E5")));
        faces.put(FaceType.RIGHT, new Face(FaceType.RIGHT, Color.web("43A047")));
        System.out.println("Cubo inicializado no estado resolvido.");
    }

    // Rotaciona uma face do cubo no sentido horário ou anti-horário
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

    // Rotaciona a camada central do cubo em torno do eixo X ou Y
    public void rotateCenter(String axis, boolean clockwise) {
        if ("X".equals(axis)) {
            rotateSliceX(1, clockwise);
            System.out.println("Rotacionando camada central no eixo X"
                    + (clockwise ? " no sentido horário" : " no sentido anti-horário"));
        } else if ("Y".equals(axis)) {
            rotateSliceY(1, clockwise);
            System.out.println("Rotacionando camada central no eixo Y"
                    + (clockwise ? " no sentido horário" : " no sentido anti-horário"));
        }
    }

    // Rotaciona a camada horizontal do meio (y=1) ao redor do eixo X
    private void rotateSliceX(int y, boolean clockwise) {
        // Envolve as faces FRONT, RIGHT, BACK, LEFT (linha do meio)
        Face front = faces.get(FaceType.FRONT);
        Face right = faces.get(FaceType.RIGHT);
        Face back = faces.get(FaceType.BACK);
        Face left = faces.get(FaceType.LEFT);

        Facelet[] frontRow = getRowCopy(front, y);
        Facelet[] rightRow = getRowCopy(right, y);
        Facelet[] backRow = getRowCopy(back, y);
        Facelet[] leftRow = getRowCopy(left, y);

        if (clockwise) {
            setRow(front, y, leftRow);
            setRow(right, y, frontRow);
            setRow(back, y, rightRow);
            setRow(left, y, backRow);
        } else {
            setRow(front, y, rightRow);
            setRow(left, y, frontRow);
            setRow(back, y, leftRow);
            setRow(right, y, backRow);
        }
    }

    // Rotaciona a camada vertical do meio (x=1) ao redor do eixo Y
    private void rotateSliceY(int x, boolean clockwise) {
        // Envolve as faces UP, FRONT, DOWN, BACK (coluna do meio)
        Face up = faces.get(FaceType.UP);
        Face front = faces.get(FaceType.FRONT);
        Face down = faces.get(FaceType.DOWN);
        Face back = faces.get(FaceType.BACK);

        Facelet[] upCol = getColumnCopy(up, x);
        Facelet[] frontCol = getColumnCopy(front, x);
        Facelet[] downCol = getColumnCopy(down, x);
        Facelet[] backCol = getColumnCopy(back, 2 - x); // BACK é invertida

        if (clockwise) {
            setColumn(up, x, reverse(backCol));
            setColumn(front, x, upCol);
            setColumn(down, x, frontCol);
            setColumn(back, 2 - x, reverse(downCol));
        } else {
            setColumn(up, x, frontCol);
            setColumn(back, 2 - x, reverse(upCol));
            setColumn(down, x, reverse(backCol));
            setColumn(front, x, downCol);
        }
    }

    // Rotaciona as bordas adjacentes à face especificada
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

    // --- Métodos auxiliares para rotação das bordas de cada face ---

    // Rotaciona as bordas ao redor da face FRONT no sentido horário

    private void rotateFront() {

        Face up = faces.get(FaceType.UP);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);
        Face right = faces.get(FaceType.RIGHT);

        // Salva as bordas relevantes
        Facelet[] upRow = getRowCopy(up, 2);           // linha inferior do UP
        Facelet[] rightCol = getColumnCopy(right, 2);  // coluna esquerda do RIGHT
        Facelet[] downRow = getRowCopy(down, 0);       // linha superior do DOWN
        Facelet[] leftCol = getColumnCopy(left, 0);    // coluna direita do LEFT

        // Atualiza as bordas no sentido horário
        setRow(up, 2, rightCol);              // UP ← RIGHT
        setColumn(right, 2, reverse(downRow)); // RIGHT ← DOWN (invertido)
        setRow(down, 0, leftCol);             // DOWN ← LEFT
        setColumn(left, 0, reverse(upRow));    // LEFT ← UP (invertido)
    }


    // Rotaciona as bordas ao redor da face FRONT no sentido anti-horário
    private void rotateFrontPrime() {

        Face up = faces.get(FaceType.UP);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);
        Face right = faces.get(FaceType.RIGHT);

        // Salva temporariamente as bordas já preparadas (com inversão quando necessário)
        Facelet[] upRow = getRowCopy(up, 2);         // linha inferior do UP
        Facelet[] leftCol = getColumnCopy(left, 0);  // coluna direita do LEFT
        Facelet[] downRow = getRowCopy(down, 0);     // linha superior do DOWN
        Facelet[] rightCol = getColumnCopy(right, 2); // coluna esquerda do RIGHT

// Atualizando as bordas adjacentes, tomando cuidado com inversões
        setRow(up, 2, reverse(leftCol));      // linha inferior do UP recebe coluna direita do LEFT invertida
        setColumn(left, 0, downRow);          // coluna esquerda do RIGHT recebe linha inferior do UP sem inverter
        setRow(down, 0, reverse(rightCol));  // linha superior do DOWN recebe coluna esquerda do RIGHT invertida
        setColumn(right, 2, upRow);           // coluna direita do LEFT recebe linha superior do DOWN sem inverter
    }

    // Rotaciona as bordas ao redor da face BACK no sentido horário
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

    // Rotaciona as bordas ao redor da face BACK no sentido anti-horário
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

    // Rotaciona as bordas ao redor da face UP no sentido horário
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

    // Rotaciona as bordas ao redor da face UP no sentido anti-horário
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

    // Rotaciona as bordas ao redor da face DOWN no sentido horário
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

    // Rotaciona as bordas ao redor da face DOWN no sentido anti-horário
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

    // Rotaciona as bordas ao redor da face LEFT no sentido horário
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

    // Rotaciona as bordas ao redor da face LEFT no sentido anti-horário
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

    // Rotaciona as bordas ao redor da face RIGHT no sentido horário
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

    // Rotaciona as bordas ao redor da face RIGHT no sentido anti-horário
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

    // Copia uma linha da face (evita referência direta)
    private Facelet[] getRowCopy(Face face, int row) {
        Facelet[] arr = new Facelet[Face.SIZE];
        for (int i = 0; i < Face.SIZE; i++) {
            arr[i] = face.getFacelet(row, i);
        }
        return arr;
    }

    // Copia uma coluna da face (evita referência direta)
    private Facelet[] getColumnCopy(Face face, int col) {
        Facelet[] arr = new Facelet[Face.SIZE];
        for (int i = 0; i < Face.SIZE; i++) {
            arr[i] = face.getFacelet(i, col);
        }
        return arr;
    }

    // Define uma linha da face com base em um array de Facelet
    private void setRow(Face face, int row, Facelet[] arr) {
        for (int i = 0; i < Face.SIZE; i++) {
            face.setFacelet(row, i, new Facelet(arr[i].getColor()));
        }
    }

    // Define uma coluna da face com base em um array de Facelet
    private void setColumn(Face face, int col, Facelet[] arr) {
        for (int i = 0; i < Face.SIZE; i++) {
            face.setFacelet(i, col, new Facelet(arr[i].getColor()));
        }
    }

    // Retorna um array invertido de Facelet
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

    // Retorna uma face específica do cubo
    public Face getFace(FaceType type) {
        return faces.get(type);
    }
}
