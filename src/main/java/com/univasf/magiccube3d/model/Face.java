package com.univasf.magiccube3d.model;
// Importa a classe Color do pacote javafx.scene.paint, utilizada para representar e manipular cores no JavaFX.
import javafx.scene.paint.Color;

// Classe que representa uma face do cubo mágico (3x3)
public class Face {
    public static final int SIZE = 3; // Tamanho padrão da face (3x3)

    private Facelet[][] facelets; // Matriz de objetos facelets da face
    private FaceType faceType; // Tipo da face (ex: UP, FRONT, etc.)

    // Retorna o facelet na posição especificada(linha e coluna de parâmetro)
    public Facelet getFacelet(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE)
            // Validação dos índices fornecidos (verifica se estão dentro dos limites da matriz) e Retorna o facelet correspondente à posição especificada.
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
        // Define o tipo da face e Inicializa a matriz de facelets com o tamanho padrão SIZE x SIZE.
        this.faceType = faceType;
        facelets = new Facelet[SIZE][SIZE];

        // Preenche todos os facelets da matriz com a cor inicial fornecida.
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                facelets[i][j] = new Facelet(initialColor);
            }
        }
    }

    // Rotaciona a face 90° no sentido horário
    public void rotateClockwise() {

        // Salva o centro e cria uma nova matriz para armazenar a face após a rotação
        Facelet center = facelets[1][1];
        Facelet[][] rotated = new Facelet[SIZE][SIZE];

        // Realiza a rotação, movendo cada facelet para sua nova posição
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                rotated[j][SIZE - 1 - i] = facelets[i][j];
            }
        }

        // Mantém o centro fixo e substitui a matriz atual pela nova matriz rotacionada
        rotated[1][1] = center;
        facelets = rotated;
    }

    // Rotaciona a face 90° no sentido anti-horário
    public void rotateCounterClockwise() {

        //Mesma lógica de salvar o centro da rotação em sentido horário
        Facelet center = facelets[1][1];
        Facelet[][] rotated = new Facelet[SIZE][SIZE];

        // Executa a rotação no sentido anti-horário, ajustando a posição de cada facelet
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                rotated[SIZE - 1 - j][i] = facelets[i][j];
            }
        }

        // Mantém o centro fixo e substitui a matriz atual pela nova matriz rotacionada
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
        if (color.equals(Color.web("FFFFFF"))) // Branco
            return "W";
        if (color.equals(Color.web("FFD600"))) // Amarelo
            return "Y";
        if (color.equals(Color.web("E53935"))) // Vermelho
            return "R";
        if (color.equals(Color.web("FF9800"))) // Laranja
            return "O";
        if (color.equals(Color.web("43A047"))) // Verde
            return "G";
        if (color.equals(Color.web("1E88E5"))) // Azul
            return "B";
        return "?"; // Cor não reconhecida
    }
}
