package com.univasf.magiccube3d.model;

import java.util.HashMap;
import java.util.Map;

// Esta classe representaria o estado do Cubo de Rubik
// e conteria a lógica para as rotações das faces.
public class Cube {

    // Mapa que associa cada tipo de face (FaceType) com o objeto Face
    // correspondente
    private Map<FaceType, Face> faces = new HashMap<>();

    // Exemplo: um array para armazenar as cores de cada facelet
    private Facelet[][][] facelets; // Dimensões podem ser 3x3x6 (face, linha, coluna) ou similar

    public Cube() {
        // Inicializar o cubo ao seu estado resolvido
        initializeSolvedState();
    }

    private void initializeSolvedState() {
        // Lógica para definir as cores iniciais de cada facelet
        System.out.println("Cubo inicializado no estado resolvido.");

        // Para cada face do cubo, cria uma nova face com uma cor associada
        faces.put(FaceType.UP, new Face(FaceType.UP, CubeColor.WHITE));
        faces.put(FaceType.DOWN, new Face(FaceType.DOWN, CubeColor.YELLOW));
        faces.put(FaceType.FRONT, new Face(FaceType.FRONT, CubeColor.RED));
        faces.put(FaceType.BACK, new Face(FaceType.BACK, CubeColor.ORANGE));
        faces.put(FaceType.LEFT, new Face(FaceType.LEFT, CubeColor.BLUE));
        faces.put(FaceType.RIGHT, new Face(FaceType.RIGHT, CubeColor.GREEN));

    }

    public void rotateFace(String face, boolean clockwise) {
        FaceType faceType = FaceType.valueOf(face.toUpperCase());
        Face faceToRotate = faces.get(faceType);

        faceToRotate.rotate(clockwise); // usa o novo méthod

        rotateAdjacentEdges(faceType, clockwise); // ainda será implementado

        System.out.println(
                "Rotacionando face " + faceType + (clockwise ? " no sentido horário" : " no sentido anti-horário"));
    }

    // Gira as bordas das faces adjacentes à face especificada, simulando a rotação
    // de um cubo mágico real
    private void rotateAdjacentEdges(FaceType face, boolean clockwise) {
        // Obtemos as seis faces do cubo
        Face up = faces.get(FaceType.UP);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);
        Face right = faces.get(FaceType.RIGHT);
        Face front = faces.get(FaceType.FRONT);
        Face back = faces.get(FaceType.BACK);

        // Para cada face, definimos como suas bordas vizinhas devem ser movidas
        switch (face) {
            case FRONT -> {
                // Ao girar a face da frente, mexemos nas bordas: UP[2], RIGHT[0], DOWN[0],
                // LEFT[2]
                Facelet[] upRow = up.getRow(2); // Linha inferior da face superior
                Facelet[] downRow = down.getRow(0); // Linha superior da face inferior
                Facelet[] leftCol = left.getColumn(2); // Coluna direita da face esquerda
                Facelet[] rightCol = right.getColumn(0); // Coluna esquerda da face direita

                if (clockwise) {
                    // Gira as arestas no sentido horário
                    up.setRow(2, reverse(leftCol));
                    right.setColumn(0, upRow);
                    down.setRow(0, reverse(rightCol));
                    left.setColumn(2, downRow);
                } else {
                    // Gira as arestas no sentido anti-horário
                    up.setRow(2, rightCol);
                    left.setColumn(2, upRow);
                    down.setRow(0, leftCol);
                    right.setColumn(0, reverse(downRow));
                }
            }

            case BACK -> {
                // Ao girar a face de trás, mexemos em UP[0], LEFT[0], DOWN[2], RIGHT[2]
                Facelet[] upRow = up.getRow(0);
                Facelet[] downRow = down.getRow(2);
                Facelet[] leftCol = left.getColumn(0);
                Facelet[] rightCol = right.getColumn(2);

                if (clockwise) {
                    up.setRow(0, rightCol);
                    left.setColumn(0, upRow);
                    down.setRow(2, leftCol);
                    right.setColumn(2, reverse(downRow));
                } else {
                    up.setRow(0, reverse(leftCol));
                    right.setColumn(2, upRow);
                    down.setRow(2, reverse(rightCol));
                    left.setColumn(0, downRow);
                }
            }

            case UP -> {
                // Girar a face de cima envolve: FRONT[0], RIGHT[0], BACK[0], LEFT[0]
                Facelet[] frontRow = front.getRow(0);
                Facelet[] rightRow = right.getRow(0);
                Facelet[] backRow = back.getRow(0);
                Facelet[] leftRow = left.getRow(0);

                if (clockwise) {
                    front.setRow(0, leftRow);
                    right.setRow(0, frontRow);
                    back.setRow(0, reverse(rightRow));
                    left.setRow(0, reverse(backRow));
                } else {
                    front.setRow(0, rightRow);
                    left.setRow(0, frontRow);
                    back.setRow(0, reverse(leftRow));
                    right.setRow(0, reverse(backRow));
                }
            }

            case DOWN -> {
                // Girar a face de baixo envolve: FRONT[2], RIGHT[2], BACK[2], LEFT[2]
                Facelet[] frontRow = front.getRow(2);
                Facelet[] rightRow = right.getRow(2);
                Facelet[] backRow = back.getRow(2);
                Facelet[] leftRow = left.getRow(2);

                if (clockwise) {
                    front.setRow(2, rightRow);
                    right.setRow(2, backRow);
                    back.setRow(2, reverse(leftRow));
                    left.setRow(2, reverse(frontRow));
                } else {
                    front.setRow(2, leftRow);
                    right.setRow(2, frontRow);
                    back.setRow(2, reverse(rightRow));
                    left.setRow(2, reverse(backRow));
                }
            }

            case LEFT -> {
                // Girar a face esquerda afeta: UP[0], FRONT[0], DOWN[0], BACK[2] (coluna
                // direita)
                Facelet[] upCol = up.getColumn(0);
                Facelet[] frontCol = front.getColumn(0);
                Facelet[] downCol = down.getColumn(0);
                Facelet[] backCol = back.getColumn(2); // Coluna oposta na face BACK

                if (clockwise) {
                    up.setColumn(0, backCol);
                    front.setColumn(0, upCol);
                    down.setColumn(0, frontCol);
                    back.setColumn(2, reverse(downCol));
                } else {
                    up.setColumn(0, frontCol);
                    back.setColumn(2, upCol);
                    down.setColumn(0, reverse(backCol));
                    front.setColumn(0, downCol);
                }
            }

            case RIGHT -> {
                // Girar a face direita afeta: UP[2], FRONT[2], DOWN[2], BACK[0] (coluna
                // esquerda)
                Facelet[] upCol = up.getColumn(2);
                Facelet[] frontCol = front.getColumn(2);
                Facelet[] downCol = down.getColumn(2);
                Facelet[] backCol = back.getColumn(0); // Coluna oposta na face BACK

                if (clockwise) {
                    up.setColumn(2, frontCol);
                    back.setColumn(0, upCol);
                    down.setColumn(2, reverse(backCol));
                    front.setColumn(2, downCol);
                } else {
                    up.setColumn(2, backCol);
                    front.setColumn(2, upCol);
                    down.setColumn(2, frontCol);
                    back.setColumn(0, reverse(downCol));
                }
            }
        }
    }

    private Facelet[] reverse(Facelet[] arr) {
        Facelet[] reversed = new Facelet[arr.length];
        for (int i = 0; i < arr.length; i++) {
            reversed[i] = arr[arr.length - 1 - i];
        }
        return reversed;
    }

    public Facelet[][][] getFacelets() {
        return facelets;
    }
}
