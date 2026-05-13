package com.yebur.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class ListadosController {

    @FXML private VBox root;

    @FXML
    public void initialize() {
        root.sceneProperty().addListener((obs, o, newScene) -> {
            if (newScene == null) return;
            newScene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE)
                    ((Stage) newScene.getWindow()).close();
            });
            String url = getClass().getResource("/com/yebur/portal/portal-dark.css").toExternalForm();
            Runnable sync = () -> {
                boolean dark = newScene.getStylesheets().stream().anyMatch(s -> s.contains("portal-dark"));
                if (dark) { if (!root.getStylesheets().contains(url)) root.getStylesheets().add(url); }
                else root.getStylesheets().remove(url);
            };
            sync.run();
            newScene.getStylesheets().addListener((javafx.collections.ListChangeListener<String>) c -> sync.run());
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

            // Dim the main window
            Parent sceneRoot = root.getScene().getRoot();
            ColorAdjust dim = new ColorAdjust();
            dim.setBrightness(-0.45);
            sceneRoot.setEffect(dim);

            Stage ownerStage = (Stage) root.getScene().getWindow();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(ownerStage);
            stage.initStyle(StageStyle.TRANSPARENT);

            // Clip root to rounded rectangle so children don't overflow card corners
            if (modalRoot instanceof javafx.scene.layout.Region region) {
                Rectangle clip = new Rectangle();
                clip.setArcWidth(36);
                clip.setArcHeight(36);
                region.widthProperty().addListener((obs, o, w) -> clip.setWidth(w.doubleValue()));
                region.heightProperty().addListener((obs, o, h) -> clip.setHeight(h.doubleValue()));
                region.setClip(clip);
            }

            Scene scene = new Scene(modalRoot, Color.TRANSPARENT);
                com.yebur.ui.ThemeSupport.copyTheme(modalRoot, scene, root.getScene());

            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setOnHiding(e -> sceneRoot.setEffect(null));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
