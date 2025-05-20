package com.univasf.magiccube3d.controller;

import com.univasf.magiccube3d.model.Cube;
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

    private double anchorX, anchorY;
    private double anchorAngleX = -20, anchorAngleY = -30;
    private final Rotate rotateX = new Rotate(-20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-30, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    @FXML
    public void initialize() {
        cube = new Cube();

        // Cria a SubScene 3D e adiciona ao centro do BorderPane
        SubScene cube3D = createCube3D(cube);
        cubePane.getChildren().add(cube3D);
        mainPane.setCenter(cubePane);

        // Mouse controls for rotating the 3D view
        cubePane.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });

        cubePane.setOnMouseDragged(event -> {
            rotateX.setAngle(anchorAngleX + (event.getSceneY() - anchorY));
            rotateY.setAngle(anchorAngleY - (event.getSceneX() - anchorX));
        });

        // Controle pelo teclado numérico
        cubePane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case NUMPAD8: // Cima
                    rotateX.setAngle(rotateX.getAngle() - 10);
                    break;
                case NUMPAD2: // Baixo
                    rotateX.setAngle(rotateX.getAngle() + 10);
                    break;
                case NUMPAD4: // Esquerda
                    rotateY.setAngle(rotateY.getAngle() + 10);
                    break;
                case NUMPAD6: // Direita
                    rotateY.setAngle(rotateY.getAngle() - 10);
                    break;
                case NUMPAD7: // Girar Z anti-horário
                    groupRotateZ(-10);
                    break;
                case NUMPAD9: // Girar Z horário
                    groupRotateZ(10);
                    break;
                default:
                    break;
            }
        });

        // Permite foco para receber eventos de teclado
        cubePane.setFocusTraversable(true);

        // Configurar ação do botão para girar a face frontal
        rotateFButton.setOnAction(_ -> {
            cube.rotateFace("FRONT", true);
            updateCube3D();
        });

        System.out.println("RubikController inicializado.");
    }

    // Adiciona rotação em Z ao grupo do cubo
    private void groupRotateZ(double angleDelta) {
        rotateZ.setAngle(rotateZ.getAngle() + angleDelta);
        updateCube3D();
    }

    // Atualiza a visualização do cubo 3D após rotação
    private void updateCube3D() {
        cubePane.getChildren().clear();
        cubePane.getChildren().add(createCube3D(cube));
    }

    // Cria a SubScene 3D do cubo
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

        // Add rotation transforms to the group
        group.getTransforms().addAll(rotateX, rotateY, rotateZ);

        SubScene subScene = new SubScene(group, 500, 500, true, null);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-350); // Move camera further back
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        subScene.setCamera(camera);

        return subScene;
    }
}
