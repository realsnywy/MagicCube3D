// Controlador principal da interface do cubo mágico 3D

// Importações de modelos, utilitários e JavaFX
package com.univasf.magiccube3d.controller;

import com.univasf.magiccube3d.model.Cube;
import com.univasf.magiccube3d.model.FaceType;
import com.univasf.magiccube3d.util.SoundPlayer;
import com.univasf.magiccube3d.util.MusicPlayer;
import com.univasf.magiccube3d.util.AudioConfig;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
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
import javafx.stage.Modality;
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

    @FXML
    private Button resetCameraButton; // Adicione no seu FXML

    @FXML
    private Button musicButton;

    @FXML
    private Button controlsButton; // Botão para abrir a aba de controles

    // Estado do cubo e elementos auxiliares
    private Cube cube;
    @FXML
    private final StackPane cubePane = new StackPane();

    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(180, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    // Adicione estas variáveis de câmera:
    private PerspectiveCamera camera;
    private double cameraDistance = -350;
    private double cameraFov = 45;
    private double cameraPanX = 0;
    private double cameraPanY = 0;

    // Limites para o zoom da câmera
    private final double CAMERA_DISTANCE_MIN = -800;
    private final double CAMERA_DISTANCE_MAX = -120;

    private boolean isMusicPlaying = false;

    // Inicialização do controlador e interface
    @FXML
    public void initialize() {
        cube = new Cube();
        SubScene cube3D = createCube3D(cube);
        cubePane.getChildren().add(cube3D);
        mainPane.setCenter(cubePane);

        // Inicializa os ícones
        initializeButtonIcons();

        setupKeyboardControls();
        cubePane.setFocusTraversable(true);
        // Solicita o foco após a interface estar pronta
        javafx.application.Platform.runLater(() -> cubePane.requestFocus());
        setupButtonActions();

        // Botão para resetar a câmera
        if (resetCameraButton != null) {
            resetCameraButton.setOnAction(_ -> resetCameraPosition());
        }

        AudioConfig.setGlobalVolume(0.6); // Define o volume global para 60%

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
                ry.setAngle(Math.sin(-t * 0.7) * 65);
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

        if (controlsButton != null) {
            controlsButton.setOnAction(_ -> showControlsWindow());

        }

    }

    private void showControlsWindow() {
        // Criar uma nova janela para exibir os controles
        Stage controlsStage = new Stage();
        controlsStage.setTitle("Controles");

        // Adiciona o ícone à janela
        controlsStage.getIcons().add(new javafx.scene.image.Image(
                getClass().getResourceAsStream("/com/univasf/magiccube3d/icons/control_icon.png")));

        // Layout da janela
        VBox layout = new VBox(10);
        layout.setPadding(new javafx.geometry.Insets(15));
        layout.getChildren().add(new Label("Atalhos de Teclado:"));

        // Lista de atalhos
        String controlsInfo = """
                Q: Rotacionar para cima (U)
                W: Rotação do eixo X (E)
                E: Rotacionar para baixo (D)
                A: Rotacionar para a esquerda (L)
                S: Rotação do eixo Y (M)
                D: Rotacionar para a direita (R)
                Z: Rotacionar frente (F)
                X: Rotação do eixo Z (S)
                C: Rotacionar parte traseira (B)
                Y: Rotacionar para cima (inverso)
                U: Rotação do eixo X (inverso)
                I: Rotacionar para baixo (inverso)
                SPACE: Embaralhar cubo
                BACKSPACE: Resetar cubo
                """;

        Label controlsLabel = new Label(controlsInfo);
        controlsLabel.setStyle("-fx-font-family: Consolas; -fx-font-size: 14px;");
        layout.getChildren().add(controlsLabel);

        // Scene e Stage
        Scene controlsScene = new Scene(layout, 400, 400);
        controlsStage.setScene(controlsScene);
        controlsStage.initModality(Modality.APPLICATION_MODAL); // Janela modal
        controlsStage.showAndWait();
        cubePane.requestFocus();
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

    // Configura rotação do cubo via teclado numérico
    private void setupKeyboardControls() {
        cubePane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                // Q, W, E: Up, rotate X center, Down
                case Q:
                    rotateUButton.fire();
                    break;
                case W:
                    rotateEButton.fire();
                    break;
                case E:
                    rotateDButton.fire();
                    break;
                // A, S, D: Left, rotate Y center, Right
                case A:
                    rotateLButton.fire();
                    break;
                case S:
                    rotateMButton.fire();
                    break;
                case D:
                    rotateRButton.fire();
                    break;
                // Z, X, C: Front, rotate Z center, Back
                case Z:
                    rotateFButton.fire();
                    break;
                case X:
                    rotateSButton.fire();
                    break;
                case C:
                    rotateBButton.fire();
                    break;
                // Inverses: Y, U, I, H, J, K, B, N, M
                case Y:
                    rotateUPrimeButton.fire();
                    break;
                case U:
                    rotateEPrimeButton.fire();
                    break;
                case I:
                    rotateDPrimeButton.fire();
                    break;
                case H:
                    rotateLPrimeButton.fire();
                    break;
                case J:
                    rotateMPrimeButton.fire();
                    break;
                case K:
                    rotateRPrimeButton.fire();
                    break;
                case B:
                    rotateFPrimeButton.fire();
                    break;
                case N:
                    rotateSPrimeButton.fire();
                    break;
                case M:
                    rotateBPrimeButton.fire();
                    break;
                // Reset camera: R
                case R:
                    resetCameraButton.fire();
                    break;
                // Shuffle and reset
                case SPACE:
                    shuffleButton.fire();
                    break;
                case BACK_SPACE:
                    resetButton.fire();
                    break;
                // Optional: keep numpad for view rotation if desired
                case NUMPAD8:
                    rotateX.setAngle(rotateX.getAngle() - 10);
                    break;
                case NUMPAD5:
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
                case P:
                    musicButton.fire();
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
        // In setupButtonActions()
        rotateUButton.setOnAction(_ -> {
            cube.rotateFace("UP", true); // right/clockwise
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateUPrimeButton.setOnAction(_ -> {
            cube.rotateFace("UP", false); // left/counterclockwise
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateEButton.setOnAction(_ -> {
            cube.rotateCenter("X", true); // right/clockwise
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateEPrimeButton.setOnAction(_ -> {
            cube.rotateCenter("X", false); // left/counterclockwise
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateDButton.setOnAction(_ -> {
            cube.rotateFace("DOWN", true); // was false, now true
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateDPrimeButton.setOnAction(_ -> {
            cube.rotateFace("DOWN", false); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // LEFT (A) - should be counterclockwise (true)
        rotateLButton.setOnAction(_ -> {
            cube.rotateFace("LEFT", false); // counterclockwise: top moves up
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateLPrimeButton.setOnAction(_ -> {
            cube.rotateFace("LEFT", true); // clockwise: top moves down
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateMButton.setOnAction(_ -> {
            cube.rotateCenter("M", true); // right/clockwise
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateMPrimeButton.setOnAction(_ -> {
            cube.rotateCenter("M", false); // left/counterclockwise
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateRButton.setOnAction(_ -> {
            cube.rotateFace("RIGHT", false); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateRPrimeButton.setOnAction(_ -> {
            cube.rotateFace("RIGHT", true); // was false, now true
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateFButton.setOnAction(_ -> {
            cube.rotateFace("FRONT", true); // Rotação da face frontal
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateFPrimeButton.setOnAction(_ -> {
            cube.rotateFace("FRONT", false); // Rotação inversa da face frontal
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateSButton.setOnAction(_ -> {
            cube.rotateCenter("S", true);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateSPrimeButton.setOnAction(_ -> {
            cube.rotateCenter("S", false);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateBButton.setOnAction(_ -> {
            cube.rotateFace("BACK", true); // right/clockwise
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        rotateBPrimeButton.setOnAction(_ -> {
            cube.rotateFace("BACK", false); // left/counterclockwise
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
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
            cubePane.requestFocus();
        });
        resetButton.setOnAction(_ -> {
            // Reseta o cubo para o estado inicial
            cube = new Cube();
            SoundPlayer.playSound("reset.wav");
            updateCube3D();
            cubePane.requestFocus();
        });

        musicButton.setOnAction(_ -> {
            if (!isMusicPlaying) {
                String modFile = getRandomModFile();
                System.out.println("Arquivo escolhido: " + modFile); // Debug
                if (modFile != null) {
                    MusicPlayer.playMusic(modFile);
                    isMusicPlaying = true;
                }
            } else {
                MusicPlayer.stopMusic();
                isMusicPlaying = false;
            }
            cubePane.requestFocus();
        });
    }

    // Método para inicializar ícones para todos os botões
    private void initializeButtonIcons() {
        // Exemplo: Adiciona ícones aos botões de rotação
        setButtonIcon(rotateUButton, "/com/univasf/magiccube3d/icons/rotateU.png");
        setButtonIcon(rotateUPrimeButton, "/com/univasf/magiccube3d/icons/rotateUPrime.png");
        setButtonIcon(rotateEButton, "/com/univasf/magiccube3d/icons/rotateE.png");
        setButtonIcon(rotateEPrimeButton, "/com/univasf/magiccube3d/icons/rotateEPrime.png");
        setButtonIcon(rotateDButton, "/com/univasf/magiccube3d/icons/rotateD.png");
        setButtonIcon(rotateDPrimeButton, "/com/univasf/magiccube3d/icons/rotateDPrime.png");
        setButtonIcon(rotateLButton, "/com/univasf/magiccube3d/icons/rotateL.png");
        setButtonIcon(rotateLPrimeButton, "/com/univasf/magiccube3d/icons/rotateLPrime.png");
        setButtonIcon(rotateMButton, "/com/univasf/magiccube3d/icons/rotateM.png");
        setButtonIcon(rotateMPrimeButton, "/com/univasf/magiccube3d/icons/rotateMPrime.png");
        setButtonIcon(rotateRButton, "/com/univasf/magiccube3d/icons/rotateR.png");
        setButtonIcon(rotateRPrimeButton, "/com/univasf/magiccube3d/icons/rotateRPrime.png");
        setButtonIcon(rotateFButton, "/com/univasf/magiccube3d/icons/rotateF.png");
        setButtonIcon(rotateFPrimeButton, "/com/univasf/magiccube3d/icons/rotateFPrime.png");
        setButtonIcon(rotateSButton, "/com/univasf/magiccube3d/icons/rotateS.png");
        setButtonIcon(rotateSPrimeButton, "/com/univasf/magiccube3d/icons/rotateSPrime.png");
        setButtonIcon(rotateBButton, "/com/univasf/magiccube3d/icons/rotateB.png");
        setButtonIcon(rotateBPrimeButton, "/com/univasf/magiccube3d/icons/rotateBPrime.png");
        setButtonIcon(shuffleButton, "/com/univasf/magiccube3d/icons/shuffle.png");
        setButtonIcon(resetButton, "/com/univasf/magiccube3d/icons/reset.png");
        setButtonIcon(resetCameraButton, "/com/univasf/magiccube3d/icons/resetCamera.png");
        setButtonIcon(musicButton, "/com/univasf/magiccube3d/icons/music.png");
        setButtonIcon(controlsButton, "/com/univasf/magiccube3d/icons/controls.png");
    }

    // Define um método para adicionar uma imagem ao botão com largura e altura
    // fixas
    private void setButtonIcon(Button button, String iconPath) {
        javafx.scene.image.Image image = new javafx.scene.image.Image(
                getClass().getResourceAsStream(iconPath) // Caminho do ícone
        );
        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
        imageView.setFitWidth(60); // Define largura do ícone (fixa)
        imageView.setFitHeight(60); // Define altura do ícone (fixa)
        button.setGraphic(imageView); // Associa a imagem ao botão
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

    // Atualize o método createCube3D:
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

        // Revamp da câmera
        camera = new PerspectiveCamera(true);
        camera.setFieldOfView(cameraFov);
        camera.setTranslateZ(cameraDistance);
        camera.setTranslateX(cameraPanX);
        camera.setTranslateY(cameraPanY);
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        subScene.setCamera(camera);

        // Eventos de zoom e pan
        subScene.setOnScroll(event -> {
            double delta = event.getDeltaY();
            cameraDistance += delta * 0.7;
            // Aplica limites ao zoom
            cameraDistance = Math.max(CAMERA_DISTANCE_MIN, Math.min(CAMERA_DISTANCE_MAX, cameraDistance));
            camera.setTranslateZ(cameraDistance);
        });

        final double[] lastMouse = new double[2];
        final double[] anchor = new double[4]; // [0]=X, [1]=Y, [2]=angleX, [3]=angleY

        subScene.setOnMousePressed(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                anchor[0] = event.getSceneX();
                anchor[1] = event.getSceneY();
                anchor[2] = rotateX.getAngle();
                anchor[3] = rotateY.getAngle();
            } else if (event.getButton() == javafx.scene.input.MouseButton.MIDDLE) {
                lastMouse[0] = event.getSceneX();
                lastMouse[1] = event.getSceneY();
            }
        });

        subScene.setOnMouseDragged(event -> {
            if (event.isSecondaryButtonDown()) {
                rotateX.setAngle(anchor[2] + (event.getSceneY() - anchor[1]));
                rotateY.setAngle(anchor[3] - (event.getSceneX() - anchor[0]));
                String face = getVisibleFace(rotateX.getAngle(), rotateY.getAngle());
                faceLabel.setText("Face atual: " + face);
            } else if (event.isMiddleButtonDown()) {
                double dx = event.getSceneX() - lastMouse[0];
                double dy = event.getSceneY() - lastMouse[1];
                cameraPanX -= dx * 0.5;
                cameraPanY -= dy * 0.5;
                cameraPanX = Math.max(-200, Math.min(200, cameraPanX));
                cameraPanY = Math.max(-200, Math.min(200, cameraPanY));
                camera.setTranslateX(cameraPanX);
                camera.setTranslateY(cameraPanY);
                lastMouse[0] = event.getSceneX();
                lastMouse[1] = event.getSceneY();
            }
        });

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

    @FXML
    private void resetCameraPosition() {
        cameraDistance = -350;
        cameraFov = 45;
        cameraPanX = 0;
        cameraPanY = 0;
        rotateX.setAngle(0);
        rotateY.setAngle(180);
        rotateZ.setAngle(0);
        updateCube3D();
    }

    // Método utilitário para pegar um arquivo .mod aleatório da pasta music
    private String getRandomModFile() {
        try {
            String musicPath = "/com/univasf/magiccube3d/music";
            java.net.URL url = getClass().getResource(musicPath);
            java.io.File musicDir = new java.io.File(url.toURI());
            String[] modFiles = musicDir.list((_, name) -> name.toLowerCase().endsWith(".mod"));
            if (modFiles != null && modFiles.length > 0) {
                int idx = new java.util.Random().nextInt(modFiles.length);
                return modFiles[idx];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
