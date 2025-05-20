package com.univasf.magiccube3d.controller;

import com.univasf.magiccube3d.model.Cube;
import com.univasf.magiccube3d.model.Face;
import com.univasf.magiccube3d.model.FaceType;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.SubScene;
import javafx.scene.Group;
import javafx.scene.shape.Box;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

public class RubikController {

    @FXML
    private BorderPane mainPane; // O painel principal onde o cubo 3D será exibido

    @FXML
    private Button rotateFButton; // Exemplo de botão para girar a face Frontal

    private Cube cube;

    private StackPane cubePane = new StackPane();

    @FXML
    public void initialize() {
        cube = new Cube();

        // Cria a SubScene 3D e adiciona ao centro do BorderPane
        SubScene cube3D = createCube3D(cube);
        cubePane.getChildren().add(cube3D);
        mainPane.setCenter(cubePane);

        // Configurar ação do botão para girar a face frontal
        rotateFButton.setOnAction(_ -> {
            cube.rotateFace("FRONT", true);
            updateCube3D();
        });

        System.out.println("RubikController inicializado.");
    }

    // Atualiza a visualização do cubo 3D após rotação
    private void updateCube3D() {
        cubePane.getChildren().clear();
        cubePane.getChildren().add(createCube3D(cube));
    }

    // Cria a SubScene 3D do cubo
    private SubScene createCube3D(Cube cube) {
        Group group = new Group();
        double size = 30; // tamanho de cada cubo
        double gap = 2; // espaço entre cubos

        // Para cada face, desenhe apenas os cubos da superfície
        for (FaceType faceType : FaceType.values()) {
            Face face = cube.getFace(faceType);
            int[][] positions = getFacePositions(faceType);
            int idx = 0;
            for (int i = 0; i < Face.SIZE; i++) {
                for (int j = 0; j < Face.SIZE; j++) {
                    int[] pos = positions[idx++];
                    Box box = new Box(size, size, size);
                    box.setTranslateX(pos[0] * (size + gap));
                    box.setTranslateY(pos[1] * (size + gap));
                    box.setTranslateZ(pos[2] * (size + gap));
                    PhongMaterial mat = new PhongMaterial(face.getFacelet(i, j).getColor());
                    box.setMaterial(mat);
                    group.getChildren().add(box);
                }
            }
        }

        SubScene subScene = new SubScene(group, 400, 400, true, null);
        subScene.setCamera(new PerspectiveCamera());
        group.getTransforms().addAll(new Rotate(-20, Rotate.X_AXIS), new Rotate(-30, Rotate.Y_AXIS));
        return subScene;
    }

    // Retorna as posições 3D para cada face (apenas superfície)
    private int[][] getFacePositions(FaceType faceType) {
        int[][] positions = new int[9][3];
        int idx = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                switch (faceType) {
                    case FRONT -> positions[idx++] = new int[] { j - 1, i - 1, 1 };
                    case BACK -> positions[idx++] = new int[] { 1 - j, i - 1, -1 };
                    case UP -> positions[idx++] = new int[] { j - 1, -1, 1 - i };
                    case DOWN -> positions[idx++] = new int[] { j - 1, 1, i - 1 };
                    case LEFT -> positions[idx++] = new int[] { -1, i - 1, 1 - j };
                    case RIGHT -> positions[idx++] = new int[] { 1, i - 1, j - 1 };
                }
            }
        }
        return positions;
    }
}
