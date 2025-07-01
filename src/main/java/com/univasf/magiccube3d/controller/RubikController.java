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
import javafx.animation.AnimationTimer;

public class RubikController {

    // Elementos da interface definidos no FXML

    // Painel principal da interface
    @FXML
    private BorderPane mainPane;

    // Botões de rotação horária do cubo
    @FXML
    private Button rotateEButton, rotateMButton, rotateFButton, rotateSButton, rotateBButton, rotateUButton,
            rotateDButton,
            rotateLButton, rotateRButton;

    // Botões de rotação anti-horária do cubo
    @FXML
    private Button rotateEPrimeButton, rotateMPrimeButton, rotateFPrimeButton, rotateSPrimeButton, rotateBPrimeButton,
            rotateUPrimeButton,
            rotateDPrimeButton, rotateLPrimeButton, rotateRPrimeButton;

    // Botões de embaralhar e resetar o cubo
    @FXML
    private Button shuffleButton, resetButton;

    // Painel lateral com os controles do usuário
    @FXML
    private VBox controlsPane;

    // Rótulo que mostra a face atual selecionada
    @FXML
    private Label faceLabel;

    // Área com o título em 3D
    @FXML
    private StackPane title3DPane;

    // Botão para redefinir a visualização da câmera para o estado inicial
    @FXML
    private Button resetCameraButton;

    // Botão para abrir o tutorial de resolução do cubo
    @FXML
    private Button tutorialButton;

    // Botão para tocar/pausar música de fundo
    @FXML
    private Button musicButton;

    // Botão para mostrar/esconder os controles na interface
    @FXML
    private Button controlsButton;

    // Painel central onde o cubo 3D será renderizado
    @FXML
    private final StackPane cubePane = new StackPane();

    // Botão para iniciar/parar o cronômetro do cubo
    @FXML
    private Button startTimerButton;

    // Rótulo para exibir o tempo decorrido
    @FXML
    private Label timerLabel;

    // Estado do cubo e elementos auxiliares
    private Cube cube;

    private AnimationTimer timer; // Timer do JavaFX para atualizar o tempo em tempo real
    private long startTime; // Tempo em que o cronômetro foi iniciado
    private boolean timerRunning; // Boolean que indica se o cronômetro está em execução

    // Transforms para manipulação 3D do cubo (rotação em X, Y e Z)
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(180, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    // Variáveis de câmera: define a câmera 3D, a distância em relação ao cubo, seu
    // campo de visão e deslocamento horizontal e vertical
    private PerspectiveCamera camera;
    private double cameraDistance = -350;
    private double cameraFov = 45;
    private double cameraPanX = 0;
    private double cameraPanY = 0;

    // Limites para o zoom da câmera
    private final double CAMERA_DISTANCE_MIN = -800;
    private final double CAMERA_DISTANCE_MAX = -120;

    // Indica se a música de fundo está sendo reproduzida (true) ou pausada (false)
    private boolean isMusicPlaying = false;

    // Inicialização do controlador e interface
    @FXML
    public void initialize() {
        // Cria uma nova instância lógica do cubo mágico, a cena 3D onde o cubo será
        // renderizado, adiciona a cena no painel principal e define o painel central do
        // layout como o local onde o cubo será exibido
        try {
            cube = new Cube();
            SubScene cube3D = createCube3D(cube);
            cubePane.getChildren().add(cube3D);
            mainPane.setCenter(cubePane);
        } catch (Exception e) {
            showError("Erro ao inicializar o cubo 3D", e);
        }

        // Inicializa os ícones, configura os controles de teclado para manipular o cubo
        // e permite que o painel do cubo receba o foco do teclado
        initializeButtonIcons();
        setupKeyboardControls();
        cubePane.setFocusTraversable(true);

        // Solicita o foco após a interface estar pronta
        javafx.application.Platform.runLater(() -> cubePane.requestFocus());
        setupButtonActions();

        // Botão para resetar a câmera
        if (resetCameraButton != null) {
            resetCameraButton.setOnAction(_ -> {
                try {
                    resetCameraPosition();
                } catch (Exception e) {
                    showError("Erro ao resetar a câmera", e);
                }
            });
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
            // Define a ação do botão "Controles" para abrir a janela com instruções de uso
            controlsButton.setOnAction(_ -> showControlsWindow());
        }
        initTimer(); // Inicializa o cronômetro
    }

    // Método que redefine a posição inicial da câmera
    @FXML
    private void resetCameraPosition() {
        cameraDistance = -350;
        cameraFov = 45;
        cameraPanX = 0;
        cameraPanY = 0;
        rotateX.setAngle(0);
        rotateY.setAngle(180);
        rotateZ.setAngle(0);
        updateCube3D(); // Atualiza a visualização 3D com os novos parâmetros da câmera
    }

    // Cria um retângulo colorido para representar uma face do cubo
    private Rectangle createFaceRect(
            // Tamanho do lado do quadrado (facelet), cor da face, posições em x, y e z e
            // também seus angulos e eixos de rotação
            double size,
            javafx.scene.paint.Paint color,
            double tx, double ty, double tz,
            double angle, Point3D axis) {
        // Cria um quadrado com tamanho e cor definidos
        Rectangle face = new Rectangle(size, size);
        face.setFill(color);

        // Centraliza o retângulo no plano X e Y, e posiciona no eixo Z
        face.setTranslateX(tx - size / 2);
        face.setTranslateY(ty - size / 2);
        face.setTranslateZ(tz);

        // Se um eixo de rotação for definido, aplica a rotação correspondente
        if (axis != null) {
            face.setRotationAxis(axis);
            face.setRotate(angle);
        }
        return face; // Retorna o retângulo configurado
    }

    // Retorna o nome da face visível do cubo com base nos ângulos de rotação em X e
    // Y
    private String getVisibleFace(double angleX, double angleY) {
        // Normaliza os ângulos para ficarem no intervalo [0, 360)
        double ax = (angleX % 360 + 360) % 360;
        double ay = (angleY % 360 + 360) % 360;

        // Verifica se a câmera está "de cabeça para baixo", ou seja, visualizando o
        // cubo por trás
        boolean flipped = ax > 90 && ax < 270;

        // Define qual face está visível com base nos ângulos de rotação:
        if (ax >= 45 && ax <= 135) {
            return "UP"; // Visão de cima do cubo
        } else if (ax >= 225 && ax <= 315) {
            return "DOWN"; // Visão de baixo do cubo
        } else if (ay >= 45 && ay <= 135) {
            return "LEFT"; // Visão do lado esquerdo
        } else if (ay >= 225 && ay <= 315) {
            return "RIGHT"; // Visão do lado direito
        } else if ((ay <= 45 || ay >= 315)) {
            return flipped ? "FRONT" : "BACK"; // Inverte entre frente e trás se a câmera estiver "por trás"
        } else { // (135 <= ay <= 225)
            return flipped ? "BACK" : "FRONT"; // Inverte também neste intervalo se estiver "virado"
        }
    }

    // Cria a sub-cena 3D do cubo mágico
    private SubScene createCube3D(Cube cube) {

        // Define o grupo que conterá todos os elementos 3D do cubo, tamanho da peça,
        // espaçamento e distância das faces em relação a peça
        Group group = new Group();
        double size = 30, gap = 2, offset = (size + gap);
        double faceOffset = 0.5;

        // Loop para montar o cubo 3x3x3
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    // Só desenha peças da borda (não o centro invisível)
                    if (x == 0 || x == 2 || y == 0 || y == 2 || z == 0 || z == 2) {
                        Box box = new Box(size, size, size); // Cria uma peça do cubo

                        // Calcula a posição central da peça
                        double boxCenterX = (x - 1) * offset;
                        double boxCenterY = (y - 1) * offset;
                        double boxCenterZ = (z - 1) * offset;

                        box.setTranslateX(boxCenterX);
                        box.setTranslateY(boxCenterY);
                        box.setTranslateZ(boxCenterZ);
                        // Cor base (cinza) da peça
                        box.setMaterial(new PhongMaterial(javafx.scene.paint.Color.web("7D7D7D")));

                        // Adiciona as faces coloridas de acordo com a posição
                        if (z == 2) // Face FRONTAL(vermelho)
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.FRONT).getFacelet(y, x).getColor(),
                                    boxCenterX, boxCenterY, boxCenterZ + size / 2 + faceOffset,
                                    0, null));
                        if (z == 0) // Face TRASEIRA(laranja)
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.BACK).getFacelet(y, 2 - x).getColor(),
                                    boxCenterX, boxCenterY, boxCenterZ - size / 2 - faceOffset,
                                    180, new Point3D(0, 1, 0)));
                        if (y == 0) // Face SUPERIOR(amarelo)
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.UP).getFacelet(z, x).getColor(),
                                    boxCenterX, boxCenterY - size / 2 - faceOffset, boxCenterZ,
                                    -90, new Point3D(1, 0, 0)));
                        if (y == 2) // Face INFERIOR(branco)
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.DOWN).getFacelet(2 - z, x).getColor(),
                                    boxCenterX, boxCenterY + size / 2 + faceOffset, boxCenterZ,
                                    90, new Point3D(1, 0, 0)));
                        if (x == 2) // Face ESQUERDA(azul)
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.LEFT).getFacelet(y, z).getColor(),
                                    boxCenterX + size / 2 + faceOffset, boxCenterY, boxCenterZ,
                                    90, new Point3D(0, 1, 0)));
                        if (x == 0) // Face DIREITA(verde)
                            group.getChildren().add(createFaceRect(
                                    size,
                                    cube.getFace(FaceType.RIGHT).getFacelet(y, 2 - z).getColor(),
                                    boxCenterX - size / 2 - faceOffset, boxCenterY, boxCenterZ,
                                    -90, new Point3D(0, 1, 0)));
                        // Adiciona a peça ao grupo
                        group.getChildren().add(box);
                    }
                }
            }
        }

        // Aplica rotações globais do cubo
        group.getTransforms().addAll(rotateX, rotateY, rotateZ);

        // Cria a sub-cena 3D com câmera e antialiasing
        SubScene subScene = new SubScene(group, 500, 500, true, javafx.scene.SceneAntialiasing.BALANCED);

        // Inicializa e configura a câmera
        try {
            camera = new PerspectiveCamera(true);
            camera.setFieldOfView(cameraFov);
            camera.setTranslateZ(cameraDistance);
            camera.setTranslateX(cameraPanX);
            camera.setTranslateY(cameraPanY);
            camera.setNearClip(0.1);
            camera.setFarClip(1000.0);
            subScene.setCamera(camera);
        } catch (Exception e) {
            showError("Erro ao configurar a câmera", e);
        }

        // Eventos de zoom e pan
        subScene.setOnScroll(event -> {
            double delta = event.getDeltaY();
            cameraDistance += delta * 0.7;
            // Aplica limites ao zoom
            cameraDistance = Math.max(CAMERA_DISTANCE_MIN, Math.min(CAMERA_DISTANCE_MAX, cameraDistance));
            camera.setTranslateZ(cameraDistance);
        });

        // Variáveis auxiliares para rotação e pan
        final double[] lastMouse = new double[2];
        final double[] anchor = new double[4]; // [0]=X, [1]=Y, [2]=angleX, [3]=angleY

        // Quando o botão do mouse é pressionado
        subScene.setOnMousePressed(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                // Inicia rotação (botão direito)
                anchor[0] = event.getSceneX();
                anchor[1] = event.getSceneY();
                anchor[2] = rotateX.getAngle();
                anchor[3] = rotateY.getAngle();
            } else if (event.getButton() == javafx.scene.input.MouseButton.MIDDLE) {
                // Inicia pan (botão do meio)
                lastMouse[0] = event.getSceneX();
                lastMouse[1] = event.getSceneY();
            }
        });

        // Enquanto o mouse é arrastado
        subScene.setOnMouseDragged(event -> {
            if (event.isSecondaryButtonDown()) {
                // Atualiza a rotação com base na movimentação do mouse
                rotateX.setAngle(anchor[2] + (event.getSceneY() - anchor[1]));
                rotateY.setAngle(anchor[3] - (event.getSceneX() - anchor[0]));
                String face = getVisibleFace(rotateX.getAngle(), rotateY.getAngle());
                // Atualiza o rótulo da face visível
                faceLabel.setText("Face atual: " + face);
            } else if (event.isMiddleButtonDown()) {
                // Atualiza o pan com base no movimento do mouse
                double dx = event.getSceneX() - lastMouse[0];
                double dy = event.getSceneY() - lastMouse[1];
                cameraPanX -= dx * 0.5;
                cameraPanY -= dy * 0.5;

                // Aplica limites ao pan
                cameraPanX = Math.max(-200, Math.min(200, cameraPanX));
                cameraPanY = Math.max(-200, Math.min(200, cameraPanY));

                // Atualiza a posição da câmera
                camera.setTranslateX(cameraPanX);
                camera.setTranslateY(cameraPanY);
                lastMouse[0] = event.getSceneX();
                lastMouse[1] = event.getSceneY();
            }
        });

        return subScene; // Retorna a sub-cena pronta para ser exibida na interface
    }

    // Atualiza a visualização 3D do cubo
    private void updateCube3D() {
        try {
            cubePane.getChildren().clear();
            cubePane.getChildren().add(createCube3D(cube));
        } catch (Exception e) {
            showError("Erro ao atualizar o cubo", e);
        }
    }

    // Rotação do grupo em torno do eixo Z
    private void groupRotateZ(double angleDelta) {
        rotateZ.setAngle(rotateZ.getAngle() + angleDelta);
        updateCube3D();
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
        setButtonIcon(tutorialButton, "/com/univasf/magiccube3d/icons/tutorial.png");
        setButtonIcon(tutorialButton, "/com/univasf/magiccube3d/icons/tutorial.png");
        setButtonIcon(controlsButton, "/com/univasf/magiccube3d/icons/controls.png");
        setButtonIcon(startTimerButton, "/com/univasf/magiccube3d/icons/start_timer.png");

    }

    // Método para adicionar uma imagem ao botão com largura e altura fixas
    private void setButtonIcon(Button button, String iconPath) {
        try {
            javafx.scene.image.Image image = new javafx.scene.image.Image(
                    getClass().getResourceAsStream(iconPath));
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
            imageView.setFitWidth(60);
            imageView.setFitHeight(60);
            button.setGraphic(imageView);
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone: " + iconPath);
            e.printStackTrace();
        }
    }

    private void initTimer() {
        timerRunning = false; // Estado inicial desligado

        // Configura o cronômetro
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Calcula o tempo decorrido em segundos
                long elapsedMillis = (System.currentTimeMillis() - startTime);
                int minutes = (int) (elapsedMillis / 1000) / 60;
                int seconds = (int) (elapsedMillis / 1000) % 60;
                timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
            }
        };

        // Define ação no botão
        startTimerButton.setOnAction(_ -> {
            if (timerRunning) {
                stopTimer(false); // Para o cronômetro se já estiver rodando
            } else {
                resetTimer(); // Reinicia valores
                startTimer(); // Inicia o cronômetro
                // Altera o ícone do botão para indicar "parar"
                setButtonIcon(startTimerButton, "/com/univasf/magiccube3d/icons/stop_timer.png");
            }
        });
    }

    // Método para iniciar o cronômetro
    private void startTimer() {
        startTime = System.currentTimeMillis(); // Registra o momento exato em que o cronômetro começou
        timer.start(); // Atualiza o tempo em tela a cada frame através do AnimationTimer
        timerRunning = true; // Indica que o cronômetro está em execução
    }

    // Método para parar o cronômetro
    private void stopTimer(boolean displayMessage) {

        timer.stop(); // Interrompe a animação de contagem de tempo
        timerRunning = false; // Para o cronômetro
        // Altera o ícone do botão para indicar "reiniciar"
        setButtonIcon(startTimerButton, "/com/univasf/magiccube3d/icons/restart_timer.png");

        // Verifica se é para exibir mensagem ao parar (quando o cubo for resolvido ou
        // não)
        if (displayMessage) {
            // Cria um alerta informativo, sinalizando que o cubo foi resolvido, exibe a
            // mensagem de parabéns e o tempo decorrido caso o timer tenha sido ativado
            try {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);

                alert.setTitle("Parabéns");
                alert.setHeaderText("Você completou o cubo mágico!");
                alert.setContentText("Tempo final: " + timerLabel.getText());
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(
                        new javafx.scene.image.Image(
                                getClass().getResourceAsStream("/com/univasf/magiccube3d/icons/icon.png")));
                alert.showAndWait();

            } catch (Exception e) {
                System.err.println("Erro ao exibir alerta de resolução:");
                e.printStackTrace();
            }
            resetTimer(); // Reinicia o cronômetro
        }
    }

    // Método para reiniciar o cronômetro
    private void resetTimer() {
        timerLabel.setText("00:00"); // Redefine o rótulo do cronômetro para o tempo inicial
        // Altera o ícone do botão para indicar "iniciar"
        setButtonIcon(startTimerButton, "/com/univasf/magiccube3d/icons/start_timer.png");
    }

    // Método para mostrar os controles de manipulação do cubo no teclado
    private void showControlsWindow() {
        // Criar uma nova janela para exibir os controles
        Stage controlsStage = new Stage();
        controlsStage.setTitle("Controles");

        // Adiciona o ícone à janela
        try {
            controlsStage.getIcons().add(new javafx.scene.image.Image(
                    getClass().getResourceAsStream("/com/univasf/magiccube3d/icons/control_icon.png")));
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone da janela de controles");
            e.printStackTrace();
        }
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
                H: Rotacionar para a esquerda (inverso)
                J: Rotação do eixo Y (inverso)
                K: Rotacionar para a direita (inverso)
                B: Rotacionar frente (inverso)
                N: Rotação do eixo Z (inverso)
                M: Rotacionar parte traseira (inverso)

                R: Resetar câmera
                SPACE: Embaralhar cubo
                BACKSPACE: Resetar cubo
                """;

        // Label que exibe a lista de atalhos
        Label controlsLabel = new Label(controlsInfo);
        controlsLabel.setStyle("-fx-font-family: Consolas; -fx-font-size: 14px;");
        layout.getChildren().add(controlsLabel);

        // Scene e Stage
        Scene controlsScene = new Scene(layout, 400, 500);
        controlsStage.setScene(controlsScene);
        controlsStage.initModality(Modality.APPLICATION_MODAL); // Janela modal
        controlsStage.showAndWait();
        cubePane.requestFocus(); // Retorna o foco para o painel principal do cubo após fechar a janela
    }

    private void checkSolved() {
        try {
            if (cube.isSolved()) {
                SoundPlayer.playSound("solved.wav");
                stopTimer(true);
                cube = new Cube();
            }
        } catch (Exception e) {
            showError("Erro ao verificar resolução do cubo", e);
        }
    }

    // Configura rotação do cubo via teclado numérico
    private void setupKeyboardControls() {
        cubePane.setOnKeyPressed(event -> {
            try {
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
                    // Inversos: Y, U, I, H, J, K, B, N, M
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
                    // Resetar camera: R
                    case R:
                        resetCameraButton.fire();
                        break;
                    // Embaralhar e resetar cubo
                    case SPACE:
                        shuffleButton.fire();
                        break;
                    case BACK_SPACE:
                        resetButton.fire();
                        break;
                    // MOver a camera com o numpad
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
                    // Tocar musica: P
                    case P:
                        musicButton.fire();
                        break;
                    default:
                        break;
                }
                // Mostra a face atual que está virada para a câmera
                String face = getVisibleFace(rotateX.getAngle(), rotateY.getAngle());
                if (faceLabel != null) {
                    faceLabel.setText("Face atual: " + face);
                }
            } catch (Exception e) {
                System.err.println("Erro ao processar entrada de teclado:");
                e.printStackTrace();
            }
        });
    }

    // Define ações dos botões de rotação, embaralhar e resetar
    // Define a ação do botão que rotaciona a face, seu respectivo sentido(Horário
    // ou anti-horário)
    // Toda vez que algum botão de movimento, embaralhar e resetar é clicado, toca
    // um som do formato .wav através de uma função da classe utilitaria
    // SOundPlayer.java
    // Atualiza a visualização 3d na interface
    // Verifica se o cubo foi resolvido e garante que o painel continue com o foco
    // (para atalhos de teclado)
    private void setupButtonActions() {
        // Rotação UP(sentido horário)
        rotateUButton.setOnAction(_ -> {
            cube.rotateFace("UP", true);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação UP(sentido anti-horário)
        rotateUPrimeButton.setOnAction(_ -> {
            cube.rotateFace("UP", false);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação MEIO(horizontal)(sentido horário)
        rotateEButton.setOnAction(_ -> {
            cube.rotateCenter("X", true);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação MEIO(horizontal)(sentido anti-horário)
        rotateEPrimeButton.setOnAction(_ -> {
            cube.rotateCenter("X", false); // left/counterclockwise
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação DOWN(sentido horário)
        rotateDButton.setOnAction(_ -> {
            cube.rotateFace("DOWN", true); // was false, now true
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação DOWN(sentido anti-horário)
        rotateDPrimeButton.setOnAction(_ -> {
            cube.rotateFace("DOWN", false); // was true, now false
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação LEFT(sentido horário)
        rotateLButton.setOnAction(_ -> {
            cube.rotateFace("LEFT", false);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação LEFT(sentido anti-horário)
        rotateLPrimeButton.setOnAction(_ -> {
            cube.rotateFace("LEFT", true);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação MEIO(vertical)(sentido horário)
        rotateMButton.setOnAction(_ -> {
            cube.rotateCenter("M", true); // right/clockwise
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação MEIO(vertical)(sentido anti-horário)
        rotateMPrimeButton.setOnAction(_ -> {
            cube.rotateCenter("M", false); // left/counterclockwise
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação RIGHT(sentido horário)
        rotateRButton.setOnAction(_ -> {
            cube.rotateFace("RIGHT", false);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação RIGHT(sentido anti-horário)
        rotateRPrimeButton.setOnAction(_ -> {
            cube.rotateFace("RIGHT", true);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação FRONT(sentido horário)
        rotateFButton.setOnAction(_ -> {
            cube.rotateFace("FRONT", true);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação FRONT(sentido anti-horário)
        rotateFPrimeButton.setOnAction(_ -> {
            cube.rotateFace("FRONT", false);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação MEIO(horizontal a UP)(sentido horário)
        rotateSButton.setOnAction(_ -> {
            cube.rotateCenter("S", true);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação MEIO(horizontal a UP)(sentido anti-horário)
        rotateSPrimeButton.setOnAction(_ -> {
            cube.rotateCenter("S", false);
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação BACK(sentido horário)
        rotateBButton.setOnAction(_ -> {
            cube.rotateFace("BACK", true); // right/clockwise
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Rotação BACK(sentido anti-horário)
        rotateBPrimeButton.setOnAction(_ -> {
            cube.rotateFace("BACK", false); // left/counterclockwise
            SoundPlayer.playSound("move.wav");
            updateCube3D();
            checkSolved();
            cubePane.requestFocus();
        });
        // Embaralhar o cubo com movimentos aleatórios
        shuffleButton.setOnAction(_ -> {
            // Define as faces possíveis do cubo para rotação
            String[] faces = { "FRONT", "BACK", "UP", "DOWN", "LEFT", "RIGHT" };
            java.util.Random rand = new java.util.Random();

            // Executa 20 movimentos aleatórios para embaralhar o cubo
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
        // Abre o link do tutorial quando o botão for clicado
        tutorialButton.setOnAction(_ -> {
            try {
                // Usa a API Desktop do Java para abrir o navegador padrão e acessar o link do
                // tutorial (YouTube)
                java.awt.Desktop.getDesktop().browse(new java.net.URI(
                        "https://youtube.com/playlist?list=PLYjrJH3e_wDO9Myj0dpQAr5TvfhGzrSCb"));
            } catch (Exception e) {
                // Em caso de erro (URI inválida ou ambiente sem suporte), imprime a exceção no
                // console
                e.printStackTrace();
            }
            cubePane.requestFocus();
        });
        // Define a ação para o botão de música (play/pause)
        musicButton.setOnAction(_ -> {
            if (!isMusicPlaying) {
                // Se a música não está tocando, seleciona um arquivo aleatório
                String modFile = getRandomModFile();
                System.out.println("Arquivo escolhido: " + modFile); // Debug

                // Se encontrou um arquivo válido, inicia a reprodução
                if (modFile != null) {
                    MusicPlayer.playMusic(modFile);
                    isMusicPlaying = true;
                }
                // Se já está tocando, para a música
            } else {
                MusicPlayer.stopMusic();
                isMusicPlaying = false; // Atualiza o estado para "parado"
            }
            cubePane.requestFocus();
        });
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

    private void showError(String message, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

}
