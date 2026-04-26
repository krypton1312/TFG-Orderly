package com.yebur.controller;

import com.yebur.service.CashSessionService;
import com.yebur.ui.CustomDialog;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class ShiftOperationsController {

    @FXML private VBox closeShiftVBox;
    @FXML private VBox root;
    private AnchorPane parentPane;

    @FXML
    public void initialize() {
        root.getStylesheets().add(getClass().getResource("/com/yebur/portal/views/shiftOperations.css").toExternalForm());
        Platform.runLater(() -> {
            if (root.getParent() instanceof AnchorPane anchorPane) {
                parentPane = anchorPane;
            }
        });
    }

    public void showShiftOperationView(MouseEvent mouseEvent) {
        Object source = mouseEvent.getSource();

        if (source == closeShiftVBox) {
            if (!hasOpenCashSession()) {
                CustomDialog.showError("No hay un turno abierto.");
                return;
            }
            loadCenterContent("/com/yebur/portal/views/shiftOperationClose.fxml");
        }

    }

    private boolean hasOpenCashSession() {
        try {
            return CashSessionService.findCashSessionByStatus("OPEN") != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void loadCenterContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();

            Object controller = loader.getController();

            parentPane.getChildren().setAll(content);
            AnchorPane.setTopAnchor(content, 20.0);
            AnchorPane.setBottomAnchor(content, 20.0);
            AnchorPane.setLeftAnchor(content, 20.0);
            AnchorPane.setRightAnchor(content, 20.0);

            applyFadeTransition(content);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Ошибка загрузки FXML: " + fxmlPath);
        }
    }


    private void applyFadeTransition(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(250), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}
