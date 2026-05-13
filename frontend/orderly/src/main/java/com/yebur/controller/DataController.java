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

    @FXML
    public VBox productVBox;
    @FXML
    public VBox categoryVBox;
    @FXML
    public VBox suplementVBox;
    @FXML
    public VBox tableVBox;
    @FXML
    public VBox employeeVBox;

    private final Map<String, Node> loadedViews = new HashMap<>();

    @FXML
    public void initialize() {
         
        root.getStylesheets().add(getClass().getResource("/com/yebur/portal/views/data.css").toExternalForm());
        root.sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS == null) return;
            String url = getClass().getResource("/com/yebur/portal/portal-dark.css").toExternalForm();
            Runnable sync = () -> {
                boolean dark = newS.getStylesheets().stream().anyMatch(s -> s.contains("portal-dark"));
                if (dark) { if (!root.getStylesheets().contains(url)) root.getStylesheets().add(url); }
                else root.getStylesheets().remove(url);
            };
            sync.run();
            newS.getStylesheets().addListener((javafx.collections.ListChangeListener<String>) c -> sync.run());
        });
        Platform.runLater(() -> {
            if (root.getParent() instanceof AnchorPane anchorPane) {
                parentPane = anchorPane;
            }
        });
    }

    public void showDataOperationView(MouseEvent mouseEvent) {
        Object source = mouseEvent.getSource();

        if (source == productVBox) {
            loadCenterContent("/com/yebur/portal/views/dataOperation.fxml", "product");
        }
        else if (source == categoryVBox) {
            loadCenterContent("/com/yebur/portal/views/dataOperation.fxml", "category");
        }
        else if (source == suplementVBox) {
            loadCenterContent("/com/yebur/portal/views/dataOperation.fxml", "supplement");
        }
        else if (source == tableVBox) {
            loadCenterContent("/com/yebur/portal/views/dataOperation.fxml", "table");
        }
        else if (source == employeeVBox) {
            loadCenterContent("/com/yebur/portal/views/dataOperation.fxml", "employee");
        }
    }

    private void loadCenterContent(String fxmlPath, String elementType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();

            Object controller = loader.getController();

            if (controller instanceof DataOperationController dataOperationController) {
                dataOperationController.setSelectedItem(elementType);
            }

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

    private void clearSelectedStyle(VBox vbox, String styleClassToRemove) {
        for (Node node : vbox.getChildren()) {
            if (node instanceof Button button) {
                button.getStyleClass().remove(styleClassToRemove);
            }
        }
    }
}
