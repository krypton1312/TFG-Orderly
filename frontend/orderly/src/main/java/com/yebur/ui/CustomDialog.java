package com.yebur.ui;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class CustomDialog {

    public static int show(Stage parentStage, String title, String message, String yesText, String noText,
            String cancelText) {
        final int[] result = { -1 };

        GaussianBlur blur = new GaussianBlur(15);
        ColorAdjust darken = new ColorAdjust();
        darken.setBrightness(-0.4);
        darken.setInput(blur);
        parentStage.getScene().getRoot().setEffect(darken);

        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);
        dialog.initOwner(parentStage);

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("dialog-overlay");

        VBox box = new VBox(25);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("dialog-box");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dialog-title");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("dialog-message");

        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);

        Button yesButton = new Button(yesText);
        yesButton.getStyleClass().addAll("dialog-button", "dialog-button-yes");
        yesButton.setOnAction(e -> {
            result[0] = 1;
            dialog.close();
        });

        Button noButton = new Button(noText);
        noButton.getStyleClass().addAll("dialog-button", "dialog-button-no");
        noButton.setOnAction(e -> {
            result[0] = 0;
            dialog.close();
        });

        Button cancelButton = new Button(cancelText);
        cancelButton.getStyleClass().addAll("dialog-button", "dialog-button-cancel");
        cancelButton.setOnAction(e -> {
            result[0] = -1;
            dialog.close();
        });

        buttons.getChildren().addAll(yesButton, noButton, cancelButton);
        box.getChildren().addAll(titleLabel, messageLabel, buttons);
        overlay.getChildren().add(box);

        Scene scene = new Scene(overlay, Color.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        try {
            scene.getStylesheets()
                    .add(CustomDialog.class.getResource("/com/yebur/pos/customdialog.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("⚠️ Не удалось загрузить customdialog.css");
        }

        dialog.setScene(scene);

        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        dialog.setY(screenHeight / 2 + 100);
        dialog.centerOnScreen();
        box.setOpacity(0);
        box.setTranslateY(30);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), box);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(250), box);
        slideUp.setFromY(30);
        slideUp.setToY(0);

        fadeIn.play();
        slideUp.play();

        dialog.showAndWait();

        parentStage.getScene().getRoot().setEffect(null);

        return result[0];
    }
}
