package com.yebur.controller;

import com.yebur.app.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class StartController {
    @FXML
    private VBox root;

    @FXML
    public void initialize() {
        root.getStylesheets().add(getClass().getResource("/com/yebur/portal/views/data.css").toExternalForm());
    }

    @FXML
    public void openPOS(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/yebur/pos/pos.fxml"));
            Parent posRoot = loader.load();

            Scene scene = new Scene(posRoot);
            String css = App.class.getResource("/com/yebur/pos/pos.css").toExternalForm();
            scene.getStylesheets().add(css);

            Stage stage = new Stage();
            stage.setTitle("Orderly POS");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/yebur/icons/icon.png")));
            stage.setScene(scene);

            // Скрываем стартовое окно
            Stage currentStage = (Stage) root.getScene().getWindow();
            currentStage.hide();

            // Показываем и ждём закрытия
            stage.showAndWait();

            // Возвращаем стартовое окно
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
