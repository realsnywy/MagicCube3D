package com.univasf.magiccube3d.controller;

import com.example.rubikfx.model.Cube;
import com.example.rubikfx.view.CubeView;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;

public class RubikController {

    @FXML
    private BorderPane mainPane; // O painel principal onde o cubo 3D será exibido

    @FXML
    private Button rotateFButton; // Exemplo de botão para girar a face Frontal

    private Cube cubeModel;
    private CubeView cubeView;

    public void initialize() {
        cubeModel = new Cube();
        // cubeView = new CubeView(cubeModel); // CubeView precisaria ser adaptado

        // mainPane.setCenter(cubeView.getViewNode()); // Adiciona a visualização do
        // cubo ao painel

        // Configurar ações dos botões
        // rotateFButton.setOnAction(event -> {
        // cubeModel.rotateFace("F", true);
        // cubeView.updateView();
        // });

        System.out.println("RubikController inicializado.");
        // Para uma versão simples, a visualização 3D pode ser mais complexa
        // e pode ser integrada diretamente ou com placeholders.
    }
}
