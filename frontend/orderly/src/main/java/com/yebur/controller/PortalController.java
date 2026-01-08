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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PortalController {

    @FXML private ImageView logoImage;
    @FXML private AnchorPane centerContent;
    @FXML private Label titleLabel;
    @FXML private VBox sidebarNavButtonsVBox;

    private final Map<String, Node> loadedViews = new HashMap<>();
    private final Map<String, Object> loadedControllers = new HashMap<>();

    // ✅ overlay (затемнение + модалка) живёт в centerContent и перекрывает только центр
    private Region dimPane;
    private StackPane modalHost;

    public void initialize() {
        logoImage.setImage(new Image(
                getClass().getResourceAsStream("/com/yebur/icons/logo.png"),
                32, 32, true, false
        ));

        initCenterOverlay();
        showStartView();
    }

    private void initCenterOverlay() {
        dimPane = new Region();
        dimPane.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        dimPane.setVisible(false);
        dimPane.setManaged(false);
        dimPane.setPickOnBounds(true);

        modalHost = new StackPane();
        modalHost.setAlignment(javafx.geometry.Pos.CENTER);
        modalHost.setVisible(false);
        modalHost.setManaged(false);
        modalHost.setPickOnBounds(true);

        AnchorPane.setTopAnchor(dimPane, 0.0);
        AnchorPane.setBottomAnchor(dimPane, 0.0);
        AnchorPane.setLeftAnchor(dimPane, 0.0);
        AnchorPane.setRightAnchor(dimPane, 0.0);

        AnchorPane.setTopAnchor(modalHost, 0.0);
        AnchorPane.setBottomAnchor(modalHost, 0.0);
        AnchorPane.setLeftAnchor(modalHost, 0.0);
        AnchorPane.setRightAnchor(modalHost, 0.0);

        // overlay должен быть сверху: добавляем после контента (контента ещё нет — не страшно)
        centerContent.getChildren().addAll(dimPane, modalHost);
    }

    @FXML
    private void showDataView(ActionEvent event) {
        titleLabel.setText("Gestion de datos");

        clearSelectedStyle(sidebarNavButtonsVBox, "nav-item-selected");
        ((Button) event.getSource()).getStyleClass().add("nav-item-selected");

        loadCenterContent("/com/yebur/portal/views/data.fxml");
    }

    @FXML
    private void showStartView(ActionEvent event) {
        titleLabel.setText("Inicio");

        clearSelectedStyle(sidebarNavButtonsVBox, "nav-item-selected");
        ((Button) event.getSource()).getStyleClass().add("nav-item-selected");

        loadCenterContent("/com/yebur/portal/views/start.fxml");
    }

    private void showStartView() {
        titleLabel.setText("Inicio");

        clearSelectedStyle(sidebarNavButtonsVBox, "nav-item-selected");
        for (Node node : sidebarNavButtonsVBox.getChildren()) {
            if (node instanceof Button button && "Inicio".equals(button.getText())) {
                button.getStyleClass().add("nav-item-selected");
                break;
            }
        }

        loadCenterContent("/com/yebur/portal/views/start.fxml");
    }

    private void loadCenterContent(String fxmlPath) {
        try {
            Node content;
            Object controller;

            if (loadedViews.containsKey(fxmlPath)) {
                content = loadedViews.get(fxmlPath);
                controller = loadedControllers.get(fxmlPath);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                content = loader.load();
                controller = loader.getController();

                loadedViews.put(fxmlPath, content);
                loadedControllers.put(fxmlPath, controller);
            }

            // ✅ не удаляем overlay
            centerContent.getChildren().removeIf(n -> n != dimPane && n != modalHost);


            // контент кладём "под" overlay
            centerContent.getChildren().add(0, content);

            ensureOverlayAttached(); // <- ключ

            // (и лучше ещё поднять overlay наверх)
            dimPane.toFront();
            modalHost.toFront();

            // ✅ твои старые отступы 20 со всех сторон — как было
            AnchorPane.setTopAnchor(content, 20.0);
            AnchorPane.setBottomAnchor(content, 20.0);
            AnchorPane.setLeftAnchor(content, 20.0);
            AnchorPane.setRightAnchor(content, 20.0);

            // ✅ передаём overlay в StartController
            if (controller instanceof StartController sc) {
                sc.setOverlay(dimPane, modalHost);
            }

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

    public void handleCloseButton(ActionEvent actionEvent) {
        Stage stage = (Stage) sidebarNavButtonsVBox.getScene().getWindow();
        stage.close();
    }

    private void ensureOverlayAttached() {
        if (!centerContent.getChildren().contains(dimPane)) {
            centerContent.getChildren().add(dimPane);
        }
        if (!centerContent.getChildren().contains(modalHost)) {
            centerContent.getChildren().add(modalHost);
        }
    }
}
