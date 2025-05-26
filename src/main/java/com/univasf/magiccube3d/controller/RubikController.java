// Controlador principal da interface do cubo mágico 3D

// Importações de modelos, utilitários e JavaFX
package com.univasf.magiccube3d.controller;

import com.univasf.magiccube3d.model.Cube;
import com.univasf.magiccube3d.model.FaceType;
import com.univasf.magiccube3d.util.SoundPlayer;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    // Elementos da interface definidos no FXML
    @FXML
    private BorderPane mainPane;

    @FXML
    private Button rotateEButton, rotateMButton, rotateFButton, rotateSButton, rotateBButton, rotateUButton, rotateDButton,
            rotateLButton, rotateRButton;

    @FXML
    private Button rotateEPrimeButton, rotateMPrimeButton, rotateFPrimeButton, rotateSPrimeButton, rotateBPrimeButton, rotateUPrimeButton,
            rotateDPrimeButton, rotateLPrimeButton, rotateRPrimeButton;

    @FXML
    private Button shuffleButton, resetButton;

    @FXML
    private VBox controlsPane;

    @FXML
    private Label faceLabel;

    // Estado do cubo e elementos auxiliares
    private Cube cube;
    @FXML
    private final StackPane cubePane = new StackPane();


    // Variáveis para controle de rotação via mouse
    private double anchorX, anchorY;
    private double anchorAngleX = 0, anchorAngleY = 180;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(180, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    // Inicialização do controlador e interface
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

        // Animação de entrada dos controles
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

    private void showCongratulationsWindow() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Parabéns!");
        alert.setHeaderText(null);
        alert.setContentText("Você resolveu o Cubo Mágico!");
        alert.showAndWait();
    }

    private void checkSolved() {
        if (cube.isSolved()) {
            showCongratulationsWindow();
        }
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

            String face = getVisibleFace(rotateX.getAngle(), rotateY.getAngle());
            faceLabel.setText("Face atual: " + face);
        });
    }


    // Configura rotação do cubo via teclado numérico
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
                default:
                    break;
            }
            String face = getVisibleFace(rotateX.getAngle(), rotateY.getAngle());
            faceLabel.setText("Face atual: " + face);
        });
    }

    // Define ações dos botões de rotação, embaralhar e resetar
    private void setupButtonActions() {
        rotateEButton.setOnAction(_ -> {
            cube.rotateCenter("X", false); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateEPrimeButton.setOnAction(_ -> {
            cube.rotateCenter("X", true); // was false, now true
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateMButton.setOnAction(_ -> {
            cube.rotateCenter("M", false); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateMPrimeButton.setOnAction(_ -> {
            cube.rotateCenter("M", true); // was false, now true
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateFButton.setOnAction(_ -> {
            cube.rotateFace("FRONT", true); // Rotação da face frontal
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateFPrimeButton.setOnAction(_ -> {
            cube.rotateFace("FRONT", false); // Rotação inversa da face frontal
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateSButton.setOnAction(_ -> {
            cube.rotateCenter("S", false);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateSPrimeButton.setOnAction(_ -> {
            cube.rotateCenter("S", true);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateBButton.setOnAction(_ -> {
            cube.rotateFace("BACK", false); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateBPrimeButton.setOnAction(_ -> {
            cube.rotateFace("BACK", true); // was false, now true
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateUButton.setOnAction(_ -> {
            cube.rotateFace("UP", false); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateUPrimeButton.setOnAction(_ -> {
            cube.rotateFace("UP", true); // was false, now true
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateDButton.setOnAction(_ -> {
            cube.rotateFace("DOWN", true); // was false, now true
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateDPrimeButton.setOnAction(_ -> {
            cube.rotateFace("DOWN", false); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });

        rotateRButton.setOnAction(_ -> {
            cube.rotateFace("LEFT", false); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateRPrimeButton.setOnAction(_ -> {
            cube.rotateFace("LEFT", true); // was false, now true
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateLButton.setOnAction(_ -> {
            cube.rotateFace("RIGHT", true); // Rotação da face direita
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateLPrimeButton.setOnAction(_ -> {
            cube.rotateFace("RIGHT", false); // Rotação inversa da face direita
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        shuffleButton.setOnAction(_ -> {
            // Embaralha o cubo com movimentos aleatórios
            String[] faces = { "FRONT", "BACK", "UP", "DOWN", "LEFT", "RIGHT" };
            java.util.Random rand = new java.util.Random();
            for (int i = 0; i < 20; i++) {
                // 60% de chance de girar uma face, 40% de girar um centro
                if (rand.nextInt(10) < 6) {
                    cube.rotateFace(faces[rand.nextInt(faces.length)], rand.nextBoolean());
                } else {
                    String[] centers = { "X", "Y" };
                    cube.rotateCenter(centers[rand.nextInt(centers.length)], rand.nextBoolean());
                }
            }
            SoundPlayer.playSound("mix.wav");
            updateCube3D();
        });
        resetButton.setOnAction(_ -> {
            // Reseta o cubo para o estado inicial
            cube = new Cube();
            SoundPlayer.playSound("reset.wav");
            updateCube3D();
        });
    }

    // Rotação do grupo em torno do eixo Z
    private void groupRotateZ(double angleDelta) {
        rotateZ.setAngle(rotateZ.getAngle() + angleDelta);
        updateCube3D();
    }

    // Atualiza a visualização 3D do cubo
    private void updateCube3D() {
        cubePane.getChildren().clear();
        cubePane.getChildren().add(createCube3D(cube));
    }

    // Cria a cena 3D do cubo com suas peças e faces coloridas
    private SubScene createCube3D(Cube cube) {
        Group group = new Group();
        double size = 30, gap = 2, offset = (size + gap);
        double faceOffset = 0.5;

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    // Só desenha peças da borda (não o centro invisível)
                    if (x == 0 || x == 2 || y == 0 || y == 2 || z == 0 || z == 2) {
                        Box box = new Box(size, size, size);
                        double boxCenterX = (x - 1) * offset;
                        double boxCenterY = (y - 1) * offset;
                        double boxCenterZ = (z - 1) * offset;

                        box.setTranslateX(boxCenterX);
                        box.setTranslateY(boxCenterY);
                        box.setTranslateZ(boxCenterZ);
                        box.setMaterial(new PhongMaterial(javafx.scene.paint.Color.web("7D7D7D")));

                        // Adiciona as faces coloridas de acordo com a posição
                        if (z == 2) // Face FRONTAL
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.FRONT).getFacelet(y, x).getColor(),
                                    boxCenterX, boxCenterY, boxCenterZ + size / 2 + faceOffset,
                                    0, null));
                        if (z == 0) // Face TRASEIRA
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.BACK).getFacelet(y, 2 - x).getColor(),
                                    boxCenterX, boxCenterY, boxCenterZ - size / 2 - faceOffset,
                                    180, new Point3D(0, 1, 0)));
                        if (y == 0) // Face SUPERIOR
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.UP).getFacelet(z, x).getColor(),
                                    boxCenterX, boxCenterY - size / 2 - faceOffset, boxCenterZ,
                                    -90, new Point3D(1, 0, 0)));
                        if (y == 2) // Face INFERIOR
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.DOWN).getFacelet(2 - z, x).getColor(),
                                    boxCenterX, boxCenterY + size / 2 + faceOffset, boxCenterZ,
                                    90, new Point3D(1, 0, 0)));
                        if (x == 0) // Face ESQUERDA
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.LEFT).getFacelet(y, 2 - z).getColor(),
                                    boxCenterX - size / 2 - faceOffset, boxCenterY, boxCenterZ,
                                    -90, new Point3D(0, 1, 0)));
                        if (x == 2) // Face DIREITA
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

        // Cria a sub-cena 3D com câmera e antialiasing
        SubScene subScene = new SubScene(group, 500, 500, true, javafx.scene.SceneAntialiasing.BALANCED);
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-350);
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        subScene.setCamera(camera);

        return subScene;
    }

    // Cria um retângulo colorido para representar uma face do cubo
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

    private String getVisibleFace(double angleX, double angleY) {
        double ax = (angleX % 360 + 360) % 360;
        double ay = (angleY % 360 + 360) % 360;

        // Câmera está de cabeça para baixo (atrás do cubo)
        boolean flipped = ax > 90 && ax < 270;

        if (ax >= 45 && ax <= 135) {
            return "UP";
        } else if (ax >= 225 && ax <= 315) {
            return "DOWN";
        } else if (ay >= 45 && ay <= 135) {
            return "LEFT";
        } else if (ay >= 225 && ay <= 315) {
            return "RIGHT";
        } else if ((ay <= 45 || ay >= 315)) {
            return flipped ? "FRONT" : "BACK"; // vermelho = FRONT, mesmo com rotação inicial
        } else { // (135 <= ay <= 225)
            return flipped ? "BACK" : "FRONT"; // laranja vira FRONT só se não estiver "por trás"
        }
    }



}
