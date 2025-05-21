package com.univasf.magiccube3d;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Font.loadFont(getClass().getResourceAsStream("/com/univasf/magiccube3d/fonts/ModernDOS8x16.ttf"), 14);

        Parent root = FXMLLoader.load(getClass().getResource("view/RubikView.fxml"));
        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(getClass().getResource("styles/style.css").toExternalForm());

        primaryStage.setTitle("MagicCube3D");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
