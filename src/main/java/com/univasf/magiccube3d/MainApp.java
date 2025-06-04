package com.univasf.magiccube3d;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.IOException;

/**
 * Classe principal da aplicação MagicCube3D.
 * Responsável por inicializar a interface gráfica usando JavaFX.
 */
public class MainApp extends Application {

    /**
     * Método chamado ao iniciar a aplicação JavaFX.
     * Carrega a fonte personalizada, o arquivo FXML da interface e aplica o CSS.
     *
     * @param primaryStage Janela principal da aplicação.
     * @throws IOException Se houver erro ao carregar recursos.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Carrega a fonte personalizada para uso na interface
        Font.loadFont(getClass().getResourceAsStream("/com/univasf/magiccube3d/fonts/ModernDOS8x16.ttf"), 14);

        // Carrega o layout da interface a partir do arquivo FXML
        Parent root = FXMLLoader.load(getClass().getResource("view/RubikView.fxml"));

        // Cria a cena principal com tamanho definido
        Scene scene = new Scene(root, 800, 600);

        // Aplica o arquivo de estilos CSS à cena
        scene.getStylesheets().add(getClass().getResource("styles/style.css").toExternalForm());

        // Configura e exibe a janela principal
        primaryStage.setTitle("MagicCube3D");

        // Adiciona o ícone à janela
        primaryStage.getIcons().add(
                new javafx.scene.image.Image(
                        getClass().getResourceAsStream("/com/univasf/magiccube3d/icons/icon.png")));

        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Método principal. Inicia a aplicação JavaFX.
     *
     * @param args Argumentos da linha de comando.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
