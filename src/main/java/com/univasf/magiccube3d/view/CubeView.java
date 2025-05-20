package com.univasf.magiccube3d.view;

import com.univasf.magiccube3d.model.Cube;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Responsável por criar e gerenciar a representação 3D do Cubo de Rubik.
 * Para uma versão simples, pode começar com uma representação 2D ou
 * placeholders.
 */
public class CubeView {

    private final Group cubeRootNode;

    public CubeView(Cube cubeModel) {
        this.cubeRootNode = new Group();
        buildCubeView();
    }

    /**
     * Constrói a visualização 3D do cubo com peças externas.
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
     * Determina se a peça está na superfície do cubo 3x3x3.
     */
    private boolean isOuterPiece(int x, int y, int z) {
        return (x == 0 || x == 2 || y == 0 || y == 2 || z == 0 || z == 2);
    }

    /**
     * Retorna o nó raiz da visualização do cubo.
     */
    public Group getViewNode() {
        return cubeRootNode;
    }

    /**
     * Atualiza a visualização do cubo com base no modelo.
     */
    public void updateView() {
        System.out.println("Visualização do cubo atualizada.");
    }
}
