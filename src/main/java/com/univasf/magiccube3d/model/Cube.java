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
    public void initializeSolvedState() {
        faces.put(FaceType.UP, new Face(FaceType.UP, Color.web("FFD600")));      // Yellow on top
        faces.put(FaceType.DOWN, new Face(FaceType.DOWN, Color.web("FFFFFF")));  // White on bottom
        faces.put(FaceType.FRONT, new Face(FaceType.FRONT, Color.web("E53935"))); // Red front
        faces.put(FaceType.BACK, new Face(FaceType.BACK, Color.web("FF9800")));   // Orange back
        faces.put(FaceType.LEFT, new Face(FaceType.LEFT, Color.web("43A047")));   // Blue left
        faces.put(FaceType.RIGHT, new Face(FaceType.RIGHT, Color.web("1E88E5"))); // Green right
        System.out.println("Cubo inicializado no estado resolvido.");
    }

    public boolean isSolved() {
        for (Face face : faces.values()) {
            Color baseColor = face.getFacelet(1, 1).getColor(); // Cor do centro é a cor base fixa
            for (int i = 0; i < Face.SIZE; i++) {
                for (int j = 0; j < Face.SIZE; j++) {
                    if (!face.getFacelet(i, j).getColor().equals(baseColor)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Rotaciona uma face do cubo no sentido horário ou anti-horário
    public void rotateFace(String face, boolean clockwise) {
        FaceType faceType = FaceType.valueOf(face.toUpperCase());
        Face faceToRotate = faces.get(faceType);

        if (clockwise) {
            // Rotaciona 3 vezes no sentido horário para simular anti-horário
            for (int i = 0; i < 3; i++) {
                faceToRotate.rotateClockwise();
            }
        } else {
            faceToRotate.rotateClockwise();
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
        } else if ("M".equals(axis)) {
            rotateSliceY(1, clockwise);
            System.out.println("Rotacionando camada central no eixo Y"
                    + (clockwise ? " no sentido horário" : " no sentido anti-horário"));
        } else if ("S".equals(axis)) {
            rotateSliceS(1, clockwise);
            System.out.println("Rotacionando camada central S"
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
    private void rotateSliceS(int z, boolean clockwise) {
        // S slice: middle layer parallel to FRONT (z=1), affects UP, RIGHT, DOWN, LEFT (row/col at z=1)
        Face up = faces.get(FaceType.UP);
        Face right = faces.get(FaceType.RIGHT);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);

        Facelet[] upRow = getRowCopy(up, 1);
        Facelet[] rightCol = getColumnCopy(right, 1);
        Facelet[] downRow = getRowCopy(down, 1);
        Facelet[] leftCol = getColumnCopy(left, 1);

        if (clockwise) {

            setRow(up, 1, rightCol);
            setColumn(left, 1, reverse(upRow));
            setRow(down, 1, leftCol);
            setColumn(right, 1, reverse(downRow));

        } else {

            setRow(up, 1, reverse(leftCol));
            setColumn(right, 1, upRow);
            setRow(down, 1, reverse(rightCol));
            setColumn(left, 1, downRow);

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
        Facelet[] leftCol = getColumnCopy(left, 2);
        Facelet[] downRow = getRowCopy(down, 2);
        Facelet[] rightCol = getColumnCopy(right, 0);

        setRow(up, 0, reverse(leftCol));
        setColumn(right, 0, upRow);
        setRow(down, 2, reverse(rightCol));
        setColumn(left, 2, downRow);

    }


    // Rotaciona as bordas ao redor da face BACK no sentido anti-horário
    private void rotateBackPrime() {

        Face up = faces.get(FaceType.UP);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);
        Face right = faces.get(FaceType.RIGHT);

        Facelet[] upRow = getRowCopy(up, 0);
        Facelet[] leftCol = getColumnCopy(left, 2);
        Facelet[] downRow = getRowCopy(down, 2);
        Facelet[] rightCol = getColumnCopy(right, 0);

        setRow(up, 0, rightCol);
        setColumn(left, 2, reverse(upRow));
        setRow(down, 2, leftCol);
        setColumn(right, 0, reverse(downRow));

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

        setRow(front, 0, reverse(leftRow));      // FRONT top ← LEFT top (reversed)
        setRow(right, 0, reverse(frontRow));              // RIGHT top ← FRONT top
        setRow(back, 0, reverse(rightRow));      // BACK top ← RIGHT top (reversed)
        setRow(left, 0, reverse(backRow));                // LEFT top ← BACK top

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

        setRow(front, 0, reverse(rightRow));              // FRONT top ← RIGHT top
        setRow(right, 0, reverse(backRow));      // RIGHT top ← BACK top (reversed)
        setRow(back, 0, reverse(leftRow));                // BACK top ← LEFT top
        setRow(left, 0, reverse(frontRow));      // LEFT top ← FRONT top (reversed)

    }

    // Rotaciona as bordas ao redor da face DOWN no sentido horário
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

        setRow(front, 2, reverse(rightRow));
        setRow(right, 2, reverse(backRow));
        setRow(back, 2, reverse(leftRow));
        setRow(left, 2, reverse(frontRow));

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

        setRow(front, 2, reverse(leftRow));               // FRONT bottom ← LEFT bottom
        setRow(right, 2, reverse(frontRow));              // RIGHT bottom ← FRONT bottom
        setRow(back, 2, reverse(rightRow));      // BACK bottom ← RIGHT bottom (reversed)
        setRow(left, 2, reverse(backRow));       // LEFT bottom ← BACK bottom (reversed)

    }

    // Rotaciona as bordas ao redor da face LEFT no sentido horário
    private void rotateLeft() {
        Face up = faces.get(FaceType.UP);
        Face back = faces.get(FaceType.BACK);
        Face down = faces.get(FaceType.DOWN);
        Face front = faces.get(FaceType.FRONT);

        // Ajuste: coluna esquerda (índice 0), e reversões apropriadas
        Facelet[] upCol = getColumnCopy(up, 2);
        Facelet[] frontCol = getColumnCopy(front, 2);
        Facelet[] downCol = getColumnCopy(down, 2);
        Facelet[] backCol = getColumnCopy(back, 0); // BACK é invertida

        setColumn(up, 2, reverse(backCol));
        setColumn(front, 2, upCol);
        setColumn(down, 2, frontCol);
        setColumn(back, 0, reverse(downCol));
    }

    private void rotateLeftPrime() {
        Face up = faces.get(FaceType.UP);
        Face back = faces.get(FaceType.BACK);
        Face down = faces.get(FaceType.DOWN);
        Face front = faces.get(FaceType.FRONT);

        Facelet[] upCol = getColumnCopy(up, 2);
        Facelet[] frontCol = getColumnCopy(front, 2);
        Facelet[] downCol = getColumnCopy(down, 2);
        Facelet[] backCol = getColumnCopy(back, 0); // BACK é invertida

        setColumn(up, 2, frontCol);
        setColumn(back, 0, reverse(upCol));
        setColumn(down, 2, reverse(backCol));
        setColumn(front, 2, downCol);
    }


    // Rotaciona as bordas ao redor da face RIGHT no sentido horário
    private void rotateRight() {

        Face up = faces.get(FaceType.UP);
        Face front = faces.get(FaceType.FRONT);
        Face down = faces.get(FaceType.DOWN);
        Face back = faces.get(FaceType.BACK);

        Facelet[] upCol = getColumnCopy(up, 0);
        Facelet[] frontCol = getColumnCopy(front, 0);
        Facelet[] downCol = getColumnCopy(down, 0);
        Facelet[] backCol = getColumnCopy(back, 2);

        setColumn(up, 0, frontCol);
        setColumn(back, 2, reverse(upCol));
        setColumn(down, 0, reverse(backCol));
        setColumn(front, 0, downCol);
    }

    // Rotaciona as bordas ao redor da face RIGHT no sentido anti-horário
    private void rotateRightPrime() {

        Face up = faces.get(FaceType.UP);
        Face front = faces.get(FaceType.FRONT);
        Face down = faces.get(FaceType.DOWN);
        Face back = faces.get(FaceType.BACK);

        Facelet[] upCol = getColumnCopy(up, 0);
        Facelet[] frontCol = getColumnCopy(front, 0);
        Facelet[] downCol = getColumnCopy(down, 0);
        Facelet[] backCol = getColumnCopy(back, 2);

        setColumn(up, 0, reverse(backCol));
        setColumn(front, 0, upCol);
        setColumn(down, 0, frontCol);
        setColumn(back, 2, reverse(downCol));

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
