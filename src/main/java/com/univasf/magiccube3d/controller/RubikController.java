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
    // Java
    // Java
    private SubScene createCube3D(Cube cube) {
        Group group = new Group();
        double size = 30;
        double gap = 2;
        double offset = (size + gap);

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    if (x == 0 || x == 2 || y == 0 || y == 2 || z == 0 || z == 2) {
                        Box box = new Box(size, size, size);
                        box.setTranslateX((x - 1) * offset);
                        box.setTranslateY((y - 1) * offset);
                        box.setTranslateZ((z - 1) * offset);

                        PhongMaterial mat = new PhongMaterial();
                        // Example: color the box with the FRONT facelet color if on the front
                        if (z == 2) {
                            mat.setDiffuseColor(cube.getFace(FaceType.FRONT).getFacelet(y, x).getColor());
                        } else if (z == 0) {
                            mat.setDiffuseColor(cube.getFace(FaceType.BACK).getFacelet(y, 2 - x).getColor());
                        } else if (y == 0) {
                            mat.setDiffuseColor(cube.getFace(FaceType.UP).getFacelet(z, x).getColor());
                        } else if (y == 2) {
                            mat.setDiffuseColor(cube.getFace(FaceType.DOWN).getFacelet(z, 2 - x).getColor());
                        } else if (x == 0) {
                            mat.setDiffuseColor(cube.getFace(FaceType.LEFT).getFacelet(y, 2 - z).getColor());
                        } else if (x == 2) {
                            mat.setDiffuseColor(cube.getFace(FaceType.RIGHT).getFacelet(y, z).getColor());
                        }
                        box.setMaterial(mat);
                        group.getChildren().add(box);
                    }
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
