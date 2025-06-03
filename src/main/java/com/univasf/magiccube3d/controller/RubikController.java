// Controlador principal da interface do cubo mágico 3D

// Importações de modelos, utilitários e JavaFX
package com.univasf.magiccube3d.controller;

import com.univasf.magiccube3d.model.Cube;
import com.univasf.magiccube3d.model.FaceType;
import com.univasf.magiccube3d.util.SoundPlayer;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
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
import javafx.stage.Stage;
import javafx.util.Duration;

public class RubikController {

    // Elementos da interface definidos no FXML
    @FXML
    private BorderPane mainPane;

    @FXML
    private Button rotateEButton, rotateMButton, rotateFButton, rotateSButton, rotateBButton, rotateUButton,
            rotateDButton,
            rotateLButton, rotateRButton;

    @FXML
    private Button rotateEPrimeButton, rotateMPrimeButton, rotateFPrimeButton, rotateSPrimeButton, rotateBPrimeButton,
            rotateUPrimeButton,
            rotateDPrimeButton, rotateLPrimeButton, rotateRPrimeButton;

    @FXML
    private Button shuffleButton, resetButton;

    @FXML
    private VBox controlsPane;

    @FXML
    private Label faceLabel;

    @FXML
    private StackPane title3DPane;

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

        // Cria o texto animado para o título 3D
        javafx.scene.text.Text text3D = new javafx.scene.text.Text("MagicCube3D");
        text3D.setFont(javafx.scene.text.Font.font("Comic Sans MS", javafx.scene.text.FontWeight.BOLD, 48));
        text3D.setFill(javafx.scene.paint.Color.WHITE);
        text3D.setStroke(javafx.scene.paint.Color.BLACK);
        text3D.setStrokeWidth(2);

        // Aplica sombra para dar profundidade ao texto
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setRadius(10);
        shadow.setColor(javafx.scene.paint.Color.BLACK);
        text3D.setEffect(shadow);

        // Mede o texto para centralizar o pivô de rotação
        new javafx.scene.Scene(new javafx.scene.Group(text3D));
        double textWidth = text3D.getLayoutBounds().getWidth();
        double textHeight = text3D.getLayoutBounds().getHeight();

        // Agrupa o texto para aplicar transformações 3D
        javafx.scene.Group textGroup = new javafx.scene.Group(text3D);

        // Define rotações 3D com pivô centralizado
        double pivotX = textWidth / 2;
        double pivotY = textHeight / 2;
        Rotate rx = new Rotate(0, pivotX, pivotY, 0, Rotate.X_AXIS);
        Rotate ry = new Rotate(0, pivotX, pivotY, 0, Rotate.Y_AXIS);
        Rotate rz = new Rotate(0, pivotX, pivotY, 0, Rotate.Z_AXIS);
        textGroup.getTransforms().addAll(rx, ry, rz);

        // Anima o texto rotacionando nos eixos X, Y e Z, além de alterar cor e escala
        javafx.animation.AnimationTimer rotator = new javafx.animation.AnimationTimer() {
            double t = 0;

            @Override
            public void handle(long now) {
                t += 0.035;
                rx.setAngle(Math.sin(t * 1.1) * 25);
                ry.setAngle(Math.sin(t * 0.7) * 65);
                rz.setAngle(Math.cos(t * 0.9) * 18);

                // Animação de cor RGB dinâmica
                double r = (Math.sin(t * 2) + 1) / 2;
                double g = (Math.sin(t * 3 + 2) + 1) / 2;
                double b = (Math.sin(t * 4 + 4) + 1) / 2;
                text3D.setFill(javafx.scene.paint.Color.color(r, g, b));

                // Efeito de escala para dar sensação de profundidade
                double scale = 1 + 0.07 * Math.sin(t * 1.2);
                textGroup.setScaleX(scale);
                textGroup.setScaleY(scale);
            }
        };
        rotator.start();

        // Adiciona o texto animado ao painel do título
        title3DPane.getChildren().add(textGroup);

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

        // Define o ícone do alerta igual ao da aplicação
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(
                new javafx.scene.image.Image(
                        getClass().getResourceAsStream("/com/univasf/magiccube3d/icons/icon.png")));

        alert.showAndWait();
    }

    private void checkSolved() {
        if (cube.isSolved()) {
            SoundPlayer.playSound("solved.wav");
            showCongratulationsWindow();
            cube = new Cube();
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
                // Atalhos para rotação das faces
                case U: // U
                    rotateUButton.fire();
                    break;
                case E: // E
                    rotateEButton.fire();
                    break;
                case F: // F
                    rotateFButton.fire();
                    break;
                case S: // S
                    rotateSButton.fire();
                    break;
                case B: // B
                    rotateBButton.fire();
                    break;
                case D: // D
                    rotateDButton.fire();
                    break;
                case L: // L
                    rotateLButton.fire();
                    break;
                case M: // M
                    rotateMButton.fire();
                    break;
                case R: // R
                    rotateRButton.fire();
                    break;
                // Atalhos para rotação inversa (Shift + letra)
                case Q: // U'
                    rotateUPrimeButton.fire();
                    break;
                case W: // E'
                    rotateEPrimeButton.fire();
                    break;
                case G: // F'
                    rotateFPrimeButton.fire();
                    break;
                case A: // S'
                    rotateSPrimeButton.fire();
                    break;
                case V: // B'
                    rotateBPrimeButton.fire();
                    break;
                case X: // D'
                    rotateDPrimeButton.fire();
                    break;
                case K: // L'
                    rotateLPrimeButton.fire();
                    break;
                case N: // M'
                    rotateMPrimeButton.fire();
                    break;
                case T: // R'
                    rotateRPrimeButton.fire();
                    break;
                // Shuffle (embaralhar)
                case SPACE:
                    shuffleButton.fire();
                    break;
                // Reset
                case BACK_SPACE:
                    resetButton.fire();
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
        rotateUButton.setOnAction(_ -> {
            cube.rotateFace("UP", true); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateUPrimeButton.setOnAction(_ -> {
            cube.rotateFace("UP", false); // was false, now true
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateEButton.setOnAction(_ -> {
            cube.rotateCenter("X", true); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateEPrimeButton.setOnAction(_ -> {
            cube.rotateCenter("X", false); // was false, now true
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
        rotateLButton.setOnAction(_ -> {
            cube.rotateFace("LEFT", false); // Rotação da face direita
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateLPrimeButton.setOnAction(_ -> {
            cube.rotateFace("LEFT", true); // Rotação inversa da face direita
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateMButton.setOnAction(_ -> {
            cube.rotateCenter("M", true); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateMPrimeButton.setOnAction(_ -> {
            cube.rotateCenter("M", false); // was false, now true
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateRButton.setOnAction(_ -> {
            cube.rotateFace("RIGHT", false); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateRPrimeButton.setOnAction(_ -> {
            cube.rotateFace("RIGHT", true); // was false, now true
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
            cube.rotateCenter("S", true);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateSPrimeButton.setOnAction(_ -> {
            cube.rotateCenter("S", false);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateBButton.setOnAction(_ -> {
            cube.rotateFace("BACK", true); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
        });
        rotateBPrimeButton.setOnAction(_ -> {
            cube.rotateFace("BACK", false); // was false, now true
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
                        if (x == 2) // Face ESQUERDA
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.LEFT).getFacelet(y, z).getColor(),
                                    boxCenterX + size / 2 + faceOffset, boxCenterY, boxCenterZ,
                                    90, new Point3D(0, 1, 0)));
                        if (x == 0) // Face DIREITA
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.RIGHT).getFacelet(y, 2 - z).getColor(),
                                    boxCenterX - size / 2 - faceOffset, boxCenterY, boxCenterZ,
                                    -90, new Point3D(0, 1, 0)));
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
