package com.yebur.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataController {

    @FXML
    private VBox root;

    private AnchorPane parentPane;

    private final Map<String, Node> loadedViews = new HashMap<>();

    @FXML
    public void initialize() {
         
        root.getStylesheets().add(getClass().getResource("/com/yebur/portal/views/data.css").toExternalForm());
        Platform.runLater(() -> {
            if (root.getParent() instanceof AnchorPane anchorPane) {
                parentPane = anchorPane;
            }
        });
    }

    public void showDataOperationView(MouseEvent mouseEvent) {
        loadCenterContent("/com/yebur/portal/views/dataOperation.fxml");
    }

    private void loadCenterContent(String fxmlPath) {
        try {
            Node content;

            if (loadedViews.containsKey(fxmlPath)) {
                content = loadedViews.get(fxmlPath);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                content = loader.load();
                loadedViews.put(fxmlPath, content);
            }

            parentPane.getChildren().setAll(content);
            AnchorPane.setTopAnchor(content, 20.0);
            AnchorPane.setBottomAnchor(content, 20.0);
            AnchorPane.setLeftAnchor(content, 20.0);
            AnchorPane.setRightAnchor(content, 20.0);

            applyFadeTransition(content);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyFadeTransition(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(250), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void clearSelectedStyle(VBox vbox, String styleClassToRemove) {
        for (Node node : vbox.getChildren()) {
            if (node instanceof Button button) {
                button.getStyleClass().remove(styleClassToRemove);
            }
        }
    }
}
