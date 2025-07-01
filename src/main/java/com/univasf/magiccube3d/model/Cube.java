package com.univasf.magiccube3d.model;

// Importa as classes da API Java para estrutura de dados HashMap e Map, úteis para armazenar pares chave-valor.
import java.util.HashMap;
import java.util.Map;
// Importa a classe Color do pacote javafx.scene.paint, utilizada para representar e manipular cores no JavaFX.
import javafx.scene.paint.Color;

// Representa o estado de um Cubo Mágico 3x3 e implementa a lógica de rotação das faces e camadas.
public class Cube {

    // Mapeia cada FaceType para sua respectiva Face
    private final Map<FaceType, Face> faces = new HashMap<>();

    // Retorna uma face específica do cubo
    public Face getFace(FaceType type) {
        return faces.get(type);
    }

    // Constutor que chama o metodo initializeSolvedState() para inicializar o cubo
    // no estado resolvido
    public Cube() {
        initializeSolvedState();
    }

    // Inicializa o cubo no estado resolvido (cada face com uma cor uniforme)
    public void initializeSolvedState() {
        faces.put(FaceType.UP, new Face(FaceType.UP, Color.web("FFD600"))); // Amarelo - TOP
        faces.put(FaceType.DOWN, new Face(FaceType.DOWN, Color.web("FFFFFF"))); // Branco - DOWN
        faces.put(FaceType.FRONT, new Face(FaceType.FRONT, Color.web("E53935"))); // Vermelho - FRONT
        faces.put(FaceType.BACK, new Face(FaceType.BACK, Color.web("FF9800"))); // Laranja - BACK
        faces.put(FaceType.LEFT, new Face(FaceType.LEFT, Color.web("1E88E5"))); // Azul - LEFT
        faces.put(FaceType.RIGHT, new Face(FaceType.RIGHT, Color.web("43A047"))); // Verde - RIGHT
        System.out.println("Cubo inicializado no estado resolvido.");
    }

    // Método que verifica se o cubo está resolvido
    public boolean isSolved() {

        // Percorre todas as faces do cubo
        for (Face face : faces.values()) {

            Color baseColor = face.getFacelet(1, 1).getColor(); // Cor do centro é a cor base fixa

            // Itera sobre todos os facelets da face atual
            for (int i = 0; i < Face.SIZE; i++) {
                for (int j = 0; j < Face.SIZE; j++) {
                    // Verifica se a cor do facelet é diferente da cor base
                    if (!face.getFacelet(i, j).getColor().equals(baseColor)) {
                        return false; // Cubo não está resolvido
                    }
                }
            }
        }
        return true; // Todas as faces têm cores uniformes
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

    // Rotaciona uma face do cubo no sentido horário ou anti-horário
    public void rotateFace(String face, boolean clockwise) {

        FaceType faceType;
        try {
            faceType = FaceType.valueOf(face.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Face inválida: " + face);
        }

        Face faceToRotate = faces.get(faceType);

        if (clockwise) {
            // Rotaciona a face no sentido horário
            for (int i = 0; i < 3; i++) {
                faceToRotate.rotateClockwise();
            }
        } else {
            // Rotaciona a face no sentido antihorário
            faceToRotate.rotateClockwise();
        }

        // Realiza a rotação das arestas adjacentes da face
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

        // Copia a fileira especificada (y) de cada face
        Facelet[] frontRow = getRowCopy(front, y);
        Facelet[] rightRow = getRowCopy(right, y);
        Facelet[] backRow = getRowCopy(back, y);
        Facelet[] leftRow = getRowCopy(left, y);

        // Atualiza as fileiras das faces baseadas na direção da rotação
        if (clockwise) {
            // Sentido horário: troca as fileiras na ordem apropriada, invertendo se
            // necessário
            setRow(front, y, reverse(rightRow)); // Front recebe right
            setRow(left, y, reverse(frontRow)); // Left recebe front
            setRow(back, y, reverse(leftRow)); // Back recebe left
            setRow(right, y, reverse(backRow)); // Right recebe back
        } else {
            // Sentido antihorário: troca as fileiras na ordem apropriada, invertendo se
            // necessário
            setRow(front, y, reverse(leftRow)); // Front recebe left
            setRow(right, y, reverse(frontRow)); // Right recebe front
            setRow(back, y, reverse(rightRow)); // Back recebe right
            setRow(left, y, reverse(backRow)); // Left recebe back
        }
    }

    // Rotaciona a camada vertical do meio (x=1) ao redor do eixo Y
    private void rotateSliceY(int x, boolean clockwise) {
        // Envolve as faces UP, FRONT, DOWN, BACK (coluna do meio)
        Face up = faces.get(FaceType.UP);
        Face front = faces.get(FaceType.FRONT);
        Face down = faces.get(FaceType.DOWN);
        Face back = faces.get(FaceType.BACK);

        // Copia a fileira especificada (x) de cada face
        Facelet[] upCol = getColumnCopy(up, x);
        Facelet[] frontCol = getColumnCopy(front, x);
        Facelet[] downCol = getColumnCopy(down, x);
        Facelet[] backCol = getColumnCopy(back, 2 - x);

        // Atualiza as fileiras das faces baseadas na direção da rotação
        if (clockwise) {
            // Sentido horário: troca as fileiras na ordem apropriada, invertendo se
            // necessário
            setColumn(up, x, reverse(backCol)); // Up recebe back
            setColumn(front, x, upCol); // Front recebe up
            setColumn(down, x, frontCol); // Down recebe front
            setColumn(back, 2 - x, reverse(downCol)); // Back recebe down
        } else {
            // Sentido antihorário: troca as fileiras na ordem apropriada, invertendo se
            // necessário
            setColumn(up, x, frontCol); // Up recebe front
            setColumn(back, 2 - x, reverse(upCol)); // Back recebe up
            setColumn(down, x, reverse(backCol)); // Down recebe back
            setColumn(front, x, downCol); // Front recebe down
        }
    }

    // Rotaciona a camada intermediária paralela à face FRONT (z=1) ao redor do eixo
    // S
    private void rotateSliceS(int z, boolean clockwise) {
        // Envolve as faces UP, RIGHT, DOWN, LEFT (linha/coluna na camada do meio)
        Face up = faces.get(FaceType.UP);
        Face right = faces.get(FaceType.RIGHT);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);

        // Copia a fileira especificada (z=1) de cada face
        Facelet[] upRow = getRowCopy(up, 1);
        Facelet[] rightCol = getColumnCopy(right, 1);
        Facelet[] downRow = getRowCopy(down, 1);
        Facelet[] leftCol = getColumnCopy(left, 1);

        if (clockwise) {
            // Sentido horário: troca as fileiras na ordem apropriada, invertendo se
            // necessário
            setRow(up, 1, leftCol); // Up recebe left
            setColumn(right, 1, reverse(upRow)); // Right recebe up
            setRow(down, 1, rightCol); // Down recebe right
            setColumn(left, 1, reverse(downRow)); // Left recebe down
        } else {
            // Sentido antihorário: troca as fileiras na ordem apropriada, invertendo se
            // necessário
            setRow(up, 1, reverse(rightCol)); // Up recebe right
            setColumn(left, 1, upRow); // Left recebe up
            setRow(down, 1, reverse(leftCol)); // Down recebe left
            setColumn(right, 1, downRow); // Right recebe down
        }
    }

    // Rotaciona as bordas adjacentes à face especificada
    private void rotateAdjacentEdges(FaceType face, boolean clockwise) {
        // Determina quais arestas adjacentes serão rotacionadas com base na face e no
        // sentido da rotação
        switch (face) {
            case FRONT -> {
                // Se a face for FRONT, rotaciona no sentido horário ou anti-horário
                if (clockwise) {
                    rotateFront(); // Roda FRONT no sentido horário
                } else {
                    rotateFrontPrime(); // Roda FRONT no sentido anti-horário
                }
            }
            // Se a face for BACK, rotaciona no sentido horário ou anti-horário
            case BACK -> {
                if (clockwise) {
                    rotateBack(); // Roda BACK no sentido horário
                } else {
                    rotateBackPrime(); // Roda BACK no sentido anti-horário
                }
            }
            case UP -> {
                // Se a face for UP, rotaciona no sentido horário ou anti-horário
                if (clockwise) {
                    rotateUp(); // Roda UP no sentido horário
                } else {
                    rotateUpPrime(); // Roda UP no sentido anti-horário
                }
            }
            case DOWN -> {
                // Se a face for DOWN, rotaciona no sentido horário ou anti-horário
                if (clockwise) {
                    rotateDown(); // Roda DOWN no sentido horário
                } else {
                    rotateDownPrime(); // Roda DOWN no sentido anti-horário
                }
            }
            case LEFT -> {
                // Se a face for LEFT, rotaciona no sentido horário ou anti-horário
                if (clockwise) {
                    rotateLeft(); // Roda LEFT no sentido horário
                } else {
                    rotateLeftPrime(); // Roda LEFT no sentido anti-horário
                }
            }
            case RIGHT -> {
                // Se a face for RIGHT, rotaciona no sentido horário ou anti-horário
                if (clockwise) {
                    rotateRight(); // Roda RIGHT no sentido horário
                } else {
                    rotateRightPrime(); // Roda RIGHT no sentido anti-horário
                }
            }
        }
    }

    // --- Métodos auxiliares para rotação das bordas de cada face ---

    // Rotaciona as bordas ao redor da face FRONT no sentido horário
    private void rotateFront() {

        // Envolve as faces UP, DOWN, LEFT, RIGHT (bordas adjacentes de FRONT)
        Face up = faces.get(FaceType.UP);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);
        Face right = faces.get(FaceType.RIGHT);

        // Copia as bordas adjacentes especificadas
        Facelet[] upRow = getRowCopy(up, 2); // linha inferior do UP
        Facelet[] leftCol = getColumnCopy(left, 2); // coluna direita do LEFT
        Facelet[] downRow = getRowCopy(down, 0); // linha superior do DOWN
        Facelet[] rightCol = getColumnCopy(right, 0); // coluna esquerda do RIGHT

        // Atualizando as bordas adjacentes no sentido horário
        setRow(up, 2, leftCol); // UP recebe a última coluna de LEFT
        setColumn(left, 2, reverse(downRow)); // LEFT recebe a linha superior de DOWN (invertida)
        setRow(down, 0, rightCol); // DOWN recebe a primeira coluna de RIGHT
        setColumn(right, 0, reverse(upRow)); // RIGHT recebe a linha inferior de UP (invertida)

    }

    // Rotaciona as bordas ao redor da face FRONT no sentido anti-horário
    private void rotateFrontPrime() {
        // Envolve as faces UP, DOWN, LEFT, RIGHT (bordas adjacentes de FRONT)
        Face up = faces.get(FaceType.UP);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);
        Face right = faces.get(FaceType.RIGHT);

        // Copia as bordas adjacentes especificadas
        Facelet[] upRow = getRowCopy(up, 2); // Última linha (inferior) do UP
        Facelet[] rightCol = getColumnCopy(right, 0); // Primeira coluna (esquerda) do RIGHT
        Facelet[] downRow = getRowCopy(down, 0); // Primeira linha (superior) do DOWN
        Facelet[] leftCol = getColumnCopy(left, 2); // Última coluna (direita) do LEFT

        // Atualizando as bordas adjacentes no sentido anti-horário
        setRow(up, 2, reverse(rightCol)); // UP recebe a primeira coluna de RIGHT (invertida)
        setColumn(right, 0, downRow); // RIGHT recebe a primeira linha de DOWN
        setRow(down, 0, reverse(leftCol)); // DOWN recebe a última coluna de LEFT (invertida)
        setColumn(left, 2, upRow); // LEFT recebe a última linha de UP

    }

    // Rotaciona as bordas ao redor da face BACK no sentido horário
    private void rotateBack() {

        // Envolve as faces UP, DOWN, LEFT, RIGHT (bordas adjacentes de BACK)
        Face up = faces.get(FaceType.UP);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);
        Face right = faces.get(FaceType.RIGHT);

        // Copia as bordas adjacentes especificadas
        Facelet[] upRow = getRowCopy(up, 0); // Primeira linha (superior) do UP
        Facelet[] leftCol = getColumnCopy(left, 0); // Primeira coluna (esquerda) do LEFT
        Facelet[] downRow = getRowCopy(down, 2); // Última linha (inferior) do DOWN
        Facelet[] rightCol = getColumnCopy(right, 2); // Última coluna (direita) do RIGHT

        // Atualizando as bordas adjacentes no sentido horário
        setRow(up, 0, reverse(rightCol)); // UP recebe a última coluna de RIGHT (invertida)
        setColumn(left, 0, upRow); // LEFT recebe a primeira linha de UP
        setRow(down, 2, reverse(leftCol)); // DOWN recebe a primeira coluna de LEFT (invertida)
        setColumn(right, 2, downRow); // RIGHT recebe a última linha de DOWN

    }

    // Rotaciona as bordas ao redor da face BACK no sentido anti-horário
    private void rotateBackPrime() {

        // Envolve as faces UP, DOWN, LEFT, RIGHT (bordas adjacentes de BACK)
        Face up = faces.get(FaceType.UP);
        Face down = faces.get(FaceType.DOWN);
        Face left = faces.get(FaceType.LEFT);
        Face right = faces.get(FaceType.RIGHT);

        Facelet[] upRow = getRowCopy(up, 0); // Primeira linha (superior) do UP
        Facelet[] leftCol = getColumnCopy(left, 0); // Primeira coluna (esquerda) do LEFT
        Facelet[] downRow = getRowCopy(down, 2); // Última linha (inferior) do DOWN
        Facelet[] rightCol = getColumnCopy(right, 2); // Última coluna (direita) do RIGHT

        // Atualizando as bordas adjacentes no sentido anti-horário
        setRow(up, 0, leftCol); // UP recebe a primeira coluna de LEFT
        setColumn(right, 2, reverse(upRow)); // RIGHT recebe a primeira linha de UP (invertida)
        setRow(down, 2, rightCol); // DOWN recebe a última linha de RIGHT
        setColumn(left, 0, reverse(downRow)); // LEFT recebe a primeira linha de DOWN (invertida)

    }

    // Rotaciona as bordas ao redor da face UP no sentido horário
    private void rotateUp() {

        // Envolve as faces FRONT, BACK, LEFT, RIGHT (bordas adjacentes de UP)
        Face front = faces.get(FaceType.FRONT);
        Face right = faces.get(FaceType.RIGHT);
        Face back = faces.get(FaceType.BACK);
        Face left = faces.get(FaceType.LEFT);

        // Copia as bordas adjacentes especificadas
        Facelet[] frontRow = getRowCopy(front, 0); // Primeira linha (superior) de FRONT
        Facelet[] rightRow = getRowCopy(right, 0); // Primeira linha (superior) de RIGHT
        Facelet[] backRow = getRowCopy(back, 0); // Primeira linha (superior) de BACK
        Facelet[] leftRow = getRowCopy(left, 0); // Primeira linha (superior) de LEFT

        // Atualizando as bordas adjacentes no sentido horário
        setRow(front, 0, reverse(rightRow)); // FRONT recebe a primeira linha de RIGHT (invertida)
        setRow(right, 0, reverse(backRow)); // RIGHT recebe a primeira linha de BACK (invertida)
        setRow(back, 0, reverse(leftRow)); // BACK recebe a primeira linha de LEFT (invertida)
        setRow(left, 0, reverse(frontRow)); // LEFT recebe a primeira linha de FRONT (invertida)

    }

    // Rotaciona as bordas ao redor da face UP no sentido anti-horário
    private void rotateUpPrime() {

        // Envolve as faces FRONT, BACK, LEFT, RIGHT (bordas adjacentes de UP)
        Face front = faces.get(FaceType.FRONT);
        Face right = faces.get(FaceType.RIGHT);
        Face back = faces.get(FaceType.BACK);
        Face left = faces.get(FaceType.LEFT);

        // Copia as bordas adjacentes especificadas
        Facelet[] frontRow = getRowCopy(front, 0); // Primeira linha (superior) de FRONT
        Facelet[] rightRow = getRowCopy(right, 0); // Primeira linha (superior) de RIGHT
        Facelet[] backRow = getRowCopy(back, 0); // Primeira linha (superior) de BACK
        Facelet[] leftRow = getRowCopy(left, 0); // Primeira linha (superior) de LEFT

        // Atualizando as bordas adjacentes no sentido anti-horário
        setRow(front, 0, reverse(leftRow)); // FRONT recebe a primeira linha de LEFT (invertida)
        setRow(right, 0, reverse(frontRow)); // RIGHT recebe a primeira linha de FRONT (invertida)
        setRow(back, 0, reverse(rightRow)); // BACK recebe a primeira linha de RIGHT (invertida)
        setRow(left, 0, reverse(backRow)); // LEFT recebe a primeira linha de BACK (invertida)

    }

    // Rotaciona as bordas ao redor da face DOWN no sentido horário
    private void rotateDown() {

        // Envolve as faces FRONT, BACK, LEFT, RIGHT (bordas adjacentes de DOWN)
        Face front = faces.get(FaceType.FRONT);
        Face right = faces.get(FaceType.RIGHT);
        Face back = faces.get(FaceType.BACK);
        Face left = faces.get(FaceType.LEFT);

        // Copia as bordas adjacentes especificadas
        Facelet[] frontRow = getRowCopy(front, 2); // Última linha (inferior) da FRONT
        Facelet[] rightRow = getRowCopy(right, 2); // Última linha (inferior) da RIGHT
        Facelet[] backRow = getRowCopy(back, 2); // Última linha (inferior) da BACK
        Facelet[] leftRow = getRowCopy(left, 2); // Última linha (inferior) da LEFT

        // Atualizando as bordas adjacentes no sentido horário
        setRow(front, 2, reverse(leftRow)); // FRONT recebe a última linha de LEFT (invertida)
        setRow(right, 2, reverse(frontRow)); // RIGHT recebe a última linha de FRONT (invertida)
        setRow(back, 2, reverse(rightRow)); // BACK recebe a última linha de RIGHT (invertida)
        setRow(left, 2, reverse(backRow)); // LEFT recebe a última linha de BACK (invertida)

    }

    // Rotaciona as bordas ao redor da face DOWN no sentido anti-horário
    private void rotateDownPrime() {

        // Envolve as faces FRONT, BACK, LEFT, RIGHT (bordas adjacentes de DOWN)
        Face front = faces.get(FaceType.FRONT);
        Face right = faces.get(FaceType.RIGHT);
        Face back = faces.get(FaceType.BACK);
        Face left = faces.get(FaceType.LEFT);

        // Copia as bordas adjacentes especificadas
        Facelet[] frontRow = getRowCopy(front, 2); // Última linha (inferior) da FRONT
        Facelet[] rightRow = getRowCopy(right, 2); // Última linha (inferior) da RIGHT
        Facelet[] backRow = getRowCopy(back, 2); // Última linha (inferior) da BACK
        Facelet[] leftRow = getRowCopy(left, 2); // Última linha (inferior) da LEFT

        // Atualizando as bordas adjacentes no sentido anti-horário
        setRow(front, 2, reverse(rightRow)); // FRONT recebe a última linha de RIGHT (invertida)
        setRow(right, 2, reverse(backRow)); // RIGHT recebe a última linha de BACK (invertida)
        setRow(back, 2, reverse(leftRow)); // BACK recebe a última linha de LEFT (invertida)
        setRow(left, 2, reverse(frontRow)); // LEFT recebe a última linha de FRONT (invertida)

    }

    // Rotaciona as bordas ao redor da face LEFT no sentido horário
    private void rotateLeft() {
        // Envolve as faces UP, BACK, DOWN, FRONT (bordas adjacentes de LEFT)
        Face up = faces.get(FaceType.UP);
        Face back = faces.get(FaceType.BACK);
        Face down = faces.get(FaceType.DOWN);
        Face front = faces.get(FaceType.FRONT);

        // Copia as bordas adjacentes especificadas
        Facelet[] upCol = getColumnCopy(up, 2); // Última coluna de UP
        Facelet[] frontCol = getColumnCopy(front, 2); // Última coluna de FRONT
        Facelet[] downCol = getColumnCopy(down, 2); // Última coluna de DOWN
        Facelet[] backCol = getColumnCopy(back, 0); // Primeira coluna da BACK (invertida)

        // Atualizando as bordas adjacentes no sentido horário
        setColumn(up, 2, frontCol); // UP recebe a última coluna de FRONT
        setColumn(back, 0, reverse(upCol)); // BACK recebe a última coluna de UP (invertida)
        setColumn(down, 2, reverse(backCol)); // DOWN recebe a primeira coluna de BACK (invertida)
        setColumn(front, 2, downCol); // FRONT recebe a última coluna de DOWN

    }

    // Rotaciona as bordas ao redor da face LEFT no sentido anti-horário
    private void rotateLeftPrime() {

        // Envolve as faces UP, BACK, DOWN, FRONT (bordas adjacentes de LEFT)
        Face up = faces.get(FaceType.UP);
        Face back = faces.get(FaceType.BACK);
        Face down = faces.get(FaceType.DOWN);
        Face front = faces.get(FaceType.FRONT);

        // Copia as bordas adjacentes especificadas
        Facelet[] upCol = getColumnCopy(up, 2); // Última coluna de UP
        Facelet[] frontCol = getColumnCopy(front, 2); // Última coluna de FRONT
        Facelet[] downCol = getColumnCopy(down, 2); // Última coluna de DOWN
        Facelet[] backCol = getColumnCopy(back, 0); // Primeira coluna da BACK (invertida)

        // Atualizando as bordas adjacentes no sentido anti-horário
        setColumn(up, 2, reverse(backCol)); // UP recebe a última coluna de BACK (invertida)
        setColumn(front, 2, upCol); // FRONT recebe a última coluna de UP
        setColumn(down, 2, frontCol); // DOWN recebe a última coluna de FRONT
        setColumn(back, 0, reverse(downCol)); // BACK recebe a primeira coluna de DOWN (invertida)

    }

    // Rotaciona as bordas ao redor da face RIGHT no sentido horário
    private void rotateRight() {

        // Envolve as faces UP, BACK, DOWN, FRONT (bordas adjacentes de RIGHT)
        Face up = faces.get(FaceType.UP);
        Face front = faces.get(FaceType.FRONT);
        Face down = faces.get(FaceType.DOWN);
        Face back = faces.get(FaceType.BACK);

        // Copia as bordas adjacentes especificadas
        Facelet[] upCol = getColumnCopy(up, 0); // Primeira coluna de UP
        Facelet[] frontCol = getColumnCopy(front, 0); // Primeira coluna de FRONT
        Facelet[] downCol = getColumnCopy(down, 0); // Primeira coluna de DOWN
        Facelet[] backCol = getColumnCopy(back, 2); // Última coluna de BACK (invertida)

        // Atualizando as bordas adjacentes no sentido horário
        setColumn(up, 0, reverse(backCol)); // UP recebe a ultima coluna de BACK (invertida)
        setColumn(front, 0, upCol); // FRONT recebe a primeira coluna de UP
        setColumn(down, 0, frontCol); // DOWN recebe a primeira coluna de FRONT
        setColumn(back, 2, reverse(downCol)); // BACK recebe a primeira coluna de DOWN (invertida)

    }

    // Rotaciona as bordas ao redor da face RIGHT no sentido anti-horário
    private void rotateRightPrime() {

        // Envolve as faces UP, BACK, DOWN, FRONT (bordas adjacentes de RIGHT)
        Face up = faces.get(FaceType.UP);
        Face front = faces.get(FaceType.FRONT);
        Face down = faces.get(FaceType.DOWN);
        Face back = faces.get(FaceType.BACK);

        // Copia as bordas adjacentes especificadas
        Facelet[] upCol = getColumnCopy(up, 0); // Primeira coluna de UP
        Facelet[] frontCol = getColumnCopy(front, 0); // Primeira coluna de FRONT
        Facelet[] downCol = getColumnCopy(down, 0); // Primeira coluna de DOWN
        Facelet[] backCol = getColumnCopy(back, 2); // Última coluna de BACK (invertida)

        // Atualizando as bordas adjacentes no sentido anti-horário
        setColumn(up, 0, frontCol); // UP recebe a ultima coluna de FRONT
        setColumn(back, 2, reverse(upCol)); // BACK recebe a primeira coluna de UP (invertida)
        setColumn(down, 0, reverse(backCol)); // DOWN recebe a última coluna de BACK (invertida)
        setColumn(front, 0, downCol); // FRONT recebe a primeira coluna de DOWN

    }

}
