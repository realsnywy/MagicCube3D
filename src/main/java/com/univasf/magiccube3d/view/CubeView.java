package com.univasf.magiccube3d.view;

import com.univasf.magiccube3d.model.Cube;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Classe responsável por criar e gerenciar a visualização 3D do Cubo de Rubik.
 * Esta implementação inicial utiliza peças externas como placeholders.
 */
public class CubeView {

    private final Group cubeRootNode;

    /**
     * Construtor que inicializa a visualização do cubo a partir do modelo.
     *
     * @param cubeModel modelo do cubo (não utilizado nesta versão)
     */
    public CubeView(Cube cubeModel) {
        this.cubeRootNode = new Group();
        buildCubeView();
    }

    /**
     * Constrói a estrutura 3D do cubo, adicionando apenas as peças externas.
     */
    private void buildCubeView() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    if (isOuterPiece(i, j, k)) {
                        Box piece = new Box(0.9, 0.9, 0.9);
                        piece.setMaterial(new PhongMaterial(Color.GRAY));
                        piece.setTranslateX(i - 1);
                        piece.setTranslateY(j - 1);
                        piece.setTranslateZ(k - 1);
                        cubeRootNode.getChildren().add(piece);
                    }
                }
            }
        }
        System.out.println("Visualização 3D do cubo construída (placeholder).");
    }

    /**
     * Verifica se a peça está localizada na superfície do cubo 3x3x3.
     *
     * @param x coordenada X da peça
     * @param y coordenada Y da peça
     * @param z coordenada Z da peça
     * @return true se a peça está na superfície, false caso contrário
     */
    private boolean isOuterPiece(int x, int y, int z) {
        return (x == 0 || x == 2 || y == 0 || y == 2 || z == 0 || z == 2);
    }

    /**
     * Retorna o nó raiz da visualização do cubo para ser adicionado à cena.
     *
     * @return Group contendo a visualização do cubo
     */
    public Group getViewNode() {
        return cubeRootNode;
    }

    /**
     * Atualiza a visualização do cubo de acordo com o modelo.
     * (Implementação placeholder)
     */
    public void updateView() {
        System.out.println("Visualização do cubo atualizada.");
    }
}
