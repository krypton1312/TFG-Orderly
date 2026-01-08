package com.yebur.ui;

import javafx.animation.FadeTransition;
import javafx.scene.layout.Region;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicBoolean;

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
        messageLabel.setAlignment(Pos.CENTER);
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
        if(cancelText != null) {
            Button cancelButton = new Button(cancelText);
            cancelButton.getStyleClass().addAll("dialog-button", "dialog-button-cancel");
            cancelButton.setOnAction(e -> {
                result[0] = -1;
                dialog.close();
            });
            buttons.getChildren().addAll(yesButton, noButton, cancelButton);
        }else{
            buttons.getChildren().addAll(yesButton, noButton);
        }

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

    public static void showError(String message) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Error");

        VBox dialogVBox = new VBox(15);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 10;");

        Label messageL = new Label(message);
        messageL.setStyle("-fx-font-size: 16px; -fx-text-fill: #1f2937; -fx-font-weight: bold;");

        Button closeButton = new Button("OK");
        closeButton.setStyle("""
                    -fx-background-color: #f63b3bff;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                    -fx-background-radius: 8;
                    -fx-cursor: hand;
                    -fx-padding: 6 20;
                """);
        closeButton.setPrefSize(60, 40);
        closeButton.setOnAction(e -> dialog.close());

        dialogVBox.getChildren().addAll(messageL, closeButton);

        Scene dialogScene = new Scene(dialogVBox, 400, 100);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    public static void confirmOpenCashSessionModernInPlace(
            StackPane modalHost,
            Region dimPane,
            String messageTitle,
            String messageText,
            String questionText,
            java.util.function.Consumer<Boolean> onResult
    ) {
        // показать слои
        dimPane.setVisible(true);
        dimPane.setManaged(true);
        dimPane.setMouseTransparent(false);

        modalHost.setVisible(true);
        modalHost.setManaged(true);
        modalHost.setMouseTransparent(false);
        modalHost.getChildren().clear();

        // ====== Card ======
        StackPane card = new StackPane();
        card.setMaxWidth(520);
        card.setMinWidth(520);
        card.setStyle("""
        -fx-background-color: white;
        -fx-background-radius: 18;
        -fx-border-radius: 18;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 28, 0, 0, 10);
    """);

        VBox content = new VBox(10);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(22, 28, 22, 28));

        // X (закрыть)
        Label closeX = new Label("✕");
        closeX.setStyle("""
        -fx-text-fill: #9ca3af;
        -fx-font-size: 16px;
        -fx-cursor: hand;
    """);
        closeX.setOnMouseEntered(e -> closeX.setStyle("""
        -fx-text-fill: #6b7280;
        -fx-font-size: 16px;
        -fx-cursor: hand;
    """));
        closeX.setOnMouseExited(e -> closeX.setStyle("""
        -fx-text-fill: #9ca3af;
        -fx-font-size: 16px;
        -fx-cursor: hand;
    """));

        // Иконка
        Circle iconBg = new Circle(26, Color.web("#FFF4E6"));
        Label icon = new Label("🔒");
        icon.setStyle("-fx-font-size: 18px;");
        StackPane iconHolder = new StackPane(iconBg, icon);
        iconHolder.setMinSize(52, 52);
        iconHolder.setMaxSize(52, 52);

        // Текст
        Label title = new Label(messageTitle);
        title.setStyle("""
        -fx-text-fill: #111827;
        -fx-font-size: 20px;
        -fx-font-weight: 800;
    """);
        title.setPadding(new Insets(6, 0, 0, 0));

        Label desc = new Label(messageText);
        desc.setWrapText(true);
        desc.setMaxWidth(440);
        desc.setAlignment(Pos.CENTER);
        desc.setStyle("""
        -fx-text-fill: #6b7280;
        -fx-font-size: 13px;
    """);

        Label question = new Label(questionText);
        question.setWrapText(true);
        question.setMaxWidth(440);
        question.setAlignment(Pos.CENTER);
        question.setStyle("""
        -fx-text-fill: #111827;
        -fx-font-size: 13px;
        -fx-font-weight: 700;
    """);
        question.setPadding(new Insets(4, 0, 0, 0));

        // Кнопки
        Button openBtn = new Button("↪  Abrir turno");
        openBtn.setPrefHeight(42);
        openBtn.setPrefWidth(170);

        String openNormal = """
        -fx-background-color: #22c55e;
        -fx-text-fill: white;
        -fx-font-weight: 800;
        -fx-background-radius: 12;
        -fx-cursor: hand;
        -fx-font-size: 13px;
    """;
        String openHover = """
        -fx-background-color: #16a34a;
        -fx-text-fill: white;
        -fx-font-weight: 800;
        -fx-background-radius: 12;
        -fx-cursor: hand;
        -fx-font-size: 13px;
    """;
        openBtn.setStyle(openNormal);
        openBtn.setOnMouseEntered(e -> openBtn.setStyle(openHover));
        openBtn.setOnMouseExited(e -> openBtn.setStyle(openNormal));

        Button cancelBtn = new Button("Cancelar");
        cancelBtn.setPrefHeight(42);
        cancelBtn.setPrefWidth(140);

        String cancelNormal = """
        -fx-background-color: white;
        -fx-text-fill: #111827;
        -fx-font-weight: 700;
        -fx-background-radius: 12;
        -fx-border-radius: 12;
        -fx-border-color: #d1d5db;
        -fx-border-width: 1;
        -fx-cursor: hand;
        -fx-font-size: 13px;
    """;
        String cancelHover = """
        -fx-background-color: #f9fafb;
        -fx-text-fill: #111827;
        -fx-font-weight: 700;
        -fx-background-radius: 12;
        -fx-border-radius: 12;
        -fx-border-color: #cbd5e1;
        -fx-border-width: 1;
        -fx-cursor: hand;
        -fx-font-size: 13px;
    """;
        cancelBtn.setStyle(cancelNormal);
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(cancelHover));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(cancelNormal));

        HBox buttons = new HBox(12, openBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(14, 0, 0, 0));

        content.getChildren().addAll(iconHolder, title, desc, question, buttons);

        // собрать card
        card.getChildren().addAll(content, closeX);
        StackPane.setAlignment(closeX, Pos.TOP_RIGHT);
        StackPane.setMargin(closeX, new Insets(12, 14, 0, 0));

        modalHost.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        // ===== закрытие/результат =====
        Runnable close = () -> {
            modalHost.getChildren().clear();
            modalHost.setVisible(false);
            modalHost.setManaged(false);

            dimPane.setVisible(false);
            dimPane.setManaged(false);
        };

        java.util.function.Consumer<Boolean> finish = ok -> {
            close.run();
            onResult.accept(ok);
        };

        openBtn.setOnAction(e -> finish.accept(true));
        cancelBtn.setOnAction(e -> finish.accept(false));
        closeX.setOnMouseClicked(e -> finish.accept(false));

        // клик по затемнению — отмена
        dimPane.setOnMouseClicked(e -> finish.accept(false));

        // ESC — отмена (ловим на сцене modalHost)
        modalHost.requestFocus();
        modalHost.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) finish.accept(false);
        });

        // анимация появления
        card.setOpacity(0);
        card.setTranslateY(18);

        FadeTransition fade = new FadeTransition(Duration.millis(180), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(180), card);
        slide.setFromY(18);
        slide.setToY(0);

        fade.play();
        slide.play();
    }


}
