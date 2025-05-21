package com.univasf.magiccube3d.controller;

import com.univasf.magiccube3d.model.Cube;
import com.univasf.magiccube3d.model.FaceType;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

public class RubikController {

    @FXML
    private BorderPane mainPane;

    @FXML
    private Button rotateFButton, rotateBButton, rotateUButton, rotateDButton, rotateLButton, rotateRButton;
    @FXML
    private Button shuffleButton, resetButton;

    @FXML
    private VBox controlsPane;

    private Cube cube;
    private final StackPane cubePane = new StackPane();

    private double anchorX, anchorY;
    private double anchorAngleX = -20, anchorAngleY = -30;
    private final Rotate rotateX = new Rotate(-20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-30, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    @FXML
    public void initialize() {
        cube = new Cube();
        SubScene cube3D = createCube3D(cube);
        cubePane.getChildren().add(cube3D);
        mainPane.setCenter(cubePane);

        setupMouseControls();
        setupKeyboardControls();
        cubePane.setFocusTraversable(true);
        setupButtonActions();

        controlsPane.setOpacity(0);
        controlsPane.setTranslateX(50);
        FadeTransition fade = new FadeTransition(Duration.millis(700), controlsPane);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(300));
        fade.play();

        TranslateTransition slide = new TranslateTransition(Duration.millis(700), controlsPane);
        slide.setFromX(50);
        slide.setToX(0);
        slide.setDelay(Duration.millis(300));
        slide.play();

        System.out.println("RubikController inicializado.");
    }

    private void setupMouseControls() {
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
    }

    private void setupKeyboardControls() {
        cubePane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case NUMPAD8:
                    rotateX.setAngle(rotateX.getAngle() - 10);
                    break;
                case NUMPAD2:
                    rotateX.setAngle(rotateX.getAngle() + 10);
                    break;
                case NUMPAD4:
                    rotateY.setAngle(rotateY.getAngle() + 10);
                    break;
                case NUMPAD6:
                    rotateY.setAngle(rotateY.getAngle() - 10);
                    break;
                case NUMPAD7:
                    groupRotateZ(-10);
                    break;
                case NUMPAD9:
                    groupRotateZ(10);
                    break;
                /*
                 * Anotações do Gabriel!
                 * Tô pra bindar as teclas abaixo, para as mesmas teclas que estão definidas nos
                 * botões para girar o cubo e tal.
                 * Enfim, a base tá aí.
                 */
                case F:
                    // Front
                    break;
                case B:
                    // Back
                    break;
                case U:
                    // Up
                    break;
                case D:
                    // Down
                    break;
                case L:
                    // Left
                    break;
                case R:
                    // Right
                    break;
                default:
                    break;
            }
        });
    }

    private void setupButtonActions() {
        rotateFButton.setOnAction(_ -> {
            cube.rotateFace("FRONT", true);
            updateCube3D();
        });
        rotateBButton.setOnAction(_ -> {
            cube.rotateFace("BACK", true);
            updateCube3D();
        });
        rotateUButton.setOnAction(_ -> {
            cube.rotateFace("UP", true);
            updateCube3D();
        });
        rotateDButton.setOnAction(_ -> {
            cube.rotateFace("DOWN", true);
            updateCube3D();
        });
        rotateLButton.setOnAction(_ -> {
            cube.rotateFace("LEFT", true);
            updateCube3D();
        });
        rotateRButton.setOnAction(_ -> {
            cube.rotateFace("RIGHT", true);
            updateCube3D();
        });
        shuffleButton.setOnAction(_ -> {
            String[] faces = { "FRONT", "BACK", "UP", "DOWN", "LEFT", "RIGHT" };
            java.util.Random rand = new java.util.Random();
            for (int i = 0; i < 20; i++) {
                cube.rotateFace(faces[rand.nextInt(6)], rand.nextBoolean());
            }
            updateCube3D();
        });
        resetButton.setOnAction(_ -> {
            cube = new Cube();
            updateCube3D();
        });
    }

    private void groupRotateZ(double angleDelta) {
        rotateZ.setAngle(rotateZ.getAngle() + angleDelta);
        updateCube3D();
    }

    private void updateCube3D() {
        cubePane.getChildren().clear();
        cubePane.getChildren().add(createCube3D(cube));
    }

    private SubScene createCube3D(Cube cube) {
        Group group = new Group();
        double size = 30, gap = 2, offset = (size + gap);
        double faceOffset = 0.5;

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    if (x == 0 || x == 2 || y == 0 || y == 2 || z == 0 || z == 2) {
                        Box box = new Box(size, size, size);
                        double boxCenterX = (x - 1) * offset;
                        double boxCenterY = (y - 1) * offset;
                        double boxCenterZ = (z - 1) * offset;

                        box.setTranslateX(boxCenterX);
                        box.setTranslateY(boxCenterY);
                        box.setTranslateZ(boxCenterZ);
                        box.setMaterial(new PhongMaterial(javafx.scene.paint.Color.GREY));

                        // Faces
                        if (z == 2) // FRONT face
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.FRONT).getFacelet(y, x).getColor(),
                                    boxCenterX, boxCenterY, boxCenterZ + size / 2 + faceOffset,
                                    0, null));
                        if (z == 0) // BACK face
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.BACK).getFacelet(y, 2 - x).getColor(),
                                    boxCenterX, boxCenterY, boxCenterZ - size / 2 - faceOffset,
                                    180, new Point3D(0, 1, 0)));
                        if (y == 0) // UP face
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.UP).getFacelet(z, x).getColor(),
                                    boxCenterX, boxCenterY - size / 2 - faceOffset, boxCenterZ,
                                    -90, new Point3D(1, 0, 0)));
                        if (y == 2) // DOWN face
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.DOWN).getFacelet(2 - z, x).getColor(),
                                    boxCenterX, boxCenterY + size / 2 + faceOffset, boxCenterZ,
                                    90, new Point3D(1, 0, 0)));
                        if (x == 0) // LEFT face
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.LEFT).getFacelet(y, 2 - z).getColor(),
                                    boxCenterX - size / 2 - faceOffset, boxCenterY, boxCenterZ,
                                    -90, new Point3D(0, 1, 0)));
                        if (x == 2) // RIGHT face
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.RIGHT).getFacelet(y, z).getColor(),
                                    boxCenterX + size / 2 + faceOffset, boxCenterY, boxCenterZ,
                                    90, new Point3D(0, 1, 0)));

                        group.getChildren().add(box);
                    }
                }
            }
        }

        group.getTransforms().addAll(rotateX, rotateY, rotateZ);

        SubScene subScene = new SubScene(group, 500, 500, true, javafx.scene.SceneAntialiasing.BALANCED);
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-350);
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        subScene.setCamera(camera);

        return subScene;
    }

    private Rectangle createFaceRect(
            double size,
            javafx.scene.paint.Paint color,
            double tx, double ty, double tz,
            double angle, Point3D axis) {
        Rectangle face = new Rectangle(size, size);
        face.setFill(color);

        face.setTranslateX(tx - size / 2);
        face.setTranslateY(ty - size / 2);
        face.setTranslateZ(tz);

        if (axis != null) {
            face.setRotationAxis(axis);
            face.setRotate(angle);
        }
        return face;
    }
}
