package com.yebur.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class PortalController {

    @FXML
    private ImageView logoImage;

    @FXML
    private BorderPane mainPane;

    @FXML
    private AnchorPane centerContent;

    @FXML
    private Label titleLabel;

    @FXML
    private VBox sidebarNavButtonsVBox;

    private final Map<String, Node> loadedViews = new HashMap<>();

    public void initialize() {
        logoImage.setImage(new Image(
                getClass().getResourceAsStream("/com/yebur/icons/logo.png"),
                32, 32, true, false // width, height, preserveRatio, smooth
        ));

    }

    @FXML
    private void showDataView(ActionEvent event) {
        titleLabel.setText("Gestion de datos");

        clearSelectedStyle(sidebarNavButtonsVBox, "nav-item-selected");

        Button clickedButton = (Button) event.getSource();
        clickedButton.getStyleClass().add("nav-item-selected");

        loadCenterContent("/com/yebur/portal/views/data.fxml");
    }

    @FXML
    private void showStartView(ActionEvent event) {
        titleLabel.setText("Inicio");

        clearSelectedStyle(sidebarNavButtonsVBox, "nav-item-selected");

        Button clickedButton = (Button) event.getSource();
        clickedButton.getStyleClass().add("nav-item-selected");

        loadCenterContent("/com/yebur/portal/views/start.fxml");
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

            centerContent.getChildren().setAll(content);
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
