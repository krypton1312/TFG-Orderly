package com.yebur.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class ListadosController {

    @FXML private VBox root;

    @FXML
    public void initialize() {
        root.sceneProperty().addListener((obs, o, newScene) -> {
            if (newScene != null) newScene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE)
                    ((Stage) newScene.getWindow()).close();
            });
        });
    }

    @FXML
    private void onTurnosCard() {
        openModal("/com/yebur/portal/views/turnosList.fxml");
    }

    @FXML
    private void onPedidosCard() {
        openModal("/com/yebur/portal/views/pedidosList.fxml");
    }

    @FXML
    private void onClientesCard() {
        openModal("/com/yebur/portal/views/clientesList.fxml");
    }

    @FXML
    private void onProductosCard() {
        openModal("/com/yebur/portal/views/productosList.fxml");
    }

    @FXML
    private void onMesasCard() {
        openModal("/com/yebur/portal/views/mesasList.fxml");
    }

    @FXML
    private void onCategoriasCard() {
        openModal("/com/yebur/portal/views/categoriasList.fxml");
    }

    @FXML
    private void onEmpleadosCard() {
        openModal("/com/yebur/portal/views/empleadosList.fxml");
    }

    @FXML
    private void onPagosCard() {
        openModal("/com/yebur/portal/views/pagosList.fxml");
    }

    private void openModal(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent modalRoot = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(modalRoot));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
