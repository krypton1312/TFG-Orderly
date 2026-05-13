package com.yebur.ui;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
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

    private record DialogPalette(
            boolean dark,
            String cardBackground,
            String cardBorder,
            String textPrimary,
            String textSecondary,
            String closeText,
            String closeHoverText,
            String infoBoxBackground,
            String infoBoxBorder,
            String secondaryButtonBackground,
            String secondaryButtonHoverBackground,
            String secondaryButtonText,
            String secondaryButtonBorder,
            String secondaryButtonHoverBorder,
            String shadowColor) {
    }

    private static DialogPalette palette(Scene scene) {
        boolean dark = ThemeSupport.isDark(scene != null ? scene : ThemeSupport.findActiveScene());
        if (dark) {
            return new DialogPalette(
                    true,
                    "#111827",
                    "#374151",
                    "#f9fafb",
                    "#d1d5db",
                    "#9ca3af",
                    "#f3f4f6",
                    "#0f172a",
                    "#334155",
                    "#1f2937",
                    "#374151",
                    "#f9fafb",
                    "#4b5563",
                    "#6b7280",
                    "rgba(0,0,0,0.55)");
        }

        return new DialogPalette(
                false,
                "white",
                "#e5e7eb",
                "#111827",
                "#6b7280",
                "#9ca3af",
                "#6b7280",
                "#f9fafb",
                "#e5e7eb",
                "white",
                "#f9fafb",
                "#111827",
                "#d1d5db",
                "#9ca3af",
                "rgba(0,0,0,0.25)");
    }

    private static String cardStyle(DialogPalette palette, String borderColor) {
        return """
                -fx-background-color: %s;
                -fx-background-radius: 18;
                -fx-border-radius: 18;
                -fx-border-color: %s;
                -fx-border-width: 1;
                -fx-effect: dropshadow(gaussian, %s, 28, 0, 0, 10);
            """.formatted(
                palette.cardBackground(),
                borderColor != null ? borderColor : palette.cardBorder(),
                palette.shadowColor());
    }

    private static void styleCloseLabel(Label closeLabel, DialogPalette palette) {
        String normal = """
                -fx-text-fill: %s;
                -fx-font-size: 16px;
                -fx-cursor: hand;
            """.formatted(palette.closeText());
        String hover = """
                -fx-text-fill: %s;
                -fx-font-size: 16px;
                -fx-cursor: hand;
            """.formatted(palette.closeHoverText());
        closeLabel.setStyle(normal);
        closeLabel.setOnMouseEntered(e -> closeLabel.setStyle(hover));
        closeLabel.setOnMouseExited(e -> closeLabel.setStyle(normal));
    }

    private static String titleStyle(DialogPalette palette) {
        return textStyle(palette.textPrimary(), 20, "800");
    }

    private static String bodyStyle(DialogPalette palette) {
        return textStyle(palette.textSecondary(), 14, "400");
    }

    private static String mutedStyle(DialogPalette palette) {
        return textStyle(palette.textSecondary(), 13, "400");
    }

    private static String strongStyle(DialogPalette palette) {
        return textStyle(palette.textPrimary(), 13, "700");
    }

    private static String mediumStyle(DialogPalette palette) {
        return textStyle(palette.textSecondary(), 13, "600");
    }

    private static String infoBoxStyle(DialogPalette palette) {
        return """
                -fx-background-color: %s;
                -fx-background-radius: 12;
                -fx-border-radius: 12;
                -fx-border-color: %s;
                -fx-border-width: 1;
                -fx-padding: 12 14 12 14;
            """.formatted(palette.infoBoxBackground(), palette.infoBoxBorder());
    }

    private static void styleSecondaryButton(Button button, DialogPalette palette) {
        String normal = """
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-weight: 700;
                -fx-background-radius: 12;
                -fx-border-radius: 12;
                -fx-border-color: %s;
                -fx-border-width: 1;
                -fx-cursor: hand;
                -fx-font-size: 13px;
            """.formatted(
                palette.secondaryButtonBackground(),
                palette.secondaryButtonText(),
                palette.secondaryButtonBorder());
        String hover = """
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-weight: 700;
                -fx-background-radius: 12;
                -fx-border-radius: 12;
                -fx-border-color: %s;
                -fx-border-width: 1;
                -fx-cursor: hand;
                -fx-font-size: 13px;
            """.formatted(
                palette.secondaryButtonHoverBackground(),
                palette.secondaryButtonText(),
                palette.secondaryButtonHoverBorder());
        button.setStyle(normal);
        button.setOnMouseEntered(e -> button.setStyle(hover));
        button.setOnMouseExited(e -> button.setStyle(normal));
    }

    private static void stylePrimaryButton(Button button, String normalBackground, String hoverBackground) {
        String normal = """
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-weight: 800;
                -fx-background-radius: 12;
                -fx-cursor: hand;
                -fx-font-size: 13px;
            """.formatted(normalBackground);
        String hover = """
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-weight: 800;
                -fx-background-radius: 12;
                -fx-cursor: hand;
                -fx-font-size: 13px;
            """.formatted(hoverBackground);
        button.setStyle(normal);
        button.setOnMouseEntered(e -> button.setStyle(hover));
        button.setOnMouseExited(e -> button.setStyle(normal));
    }

    private static String textStyle(String color, int size, String weight) {
        return """
                -fx-text-fill: %s;
                -fx-font-size: %dpx;
                -fx-font-weight: %s;
            """.formatted(color, size, weight);
    }

    private static String accent(DialogPalette palette, String lightColor, String darkColor) {
        return palette.dark() ? darkColor : lightColor;
    }

    private static Color accentFill(DialogPalette palette, String lightColor, String darkColor) {
        return Color.web(accent(palette, lightColor, darkColor));
    }

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
        ThemeSupport.copyTheme(scene, parentStage.getScene());

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
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        StackPane overlay = new StackPane();
        DialogPalette dialogPalette = palette(null);

        StackPane card = new StackPane();
        card.setMaxWidth(440);
        card.setPrefWidth(440);
        card.setStyle(cardStyle(dialogPalette, accent(dialogPalette, "#fee2e2", "#7f1d1d")));

        VBox content = new VBox(16);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(28, 28, 24, 28));

        Circle iconBg = new Circle(26, accentFill(dialogPalette, "#FEF2F2", "#4c1d1d"));
        Label icon = new Label("⚠");
        icon.setStyle("""
            -fx-font-size: 18px;
            -fx-text-fill: %s;
        """.formatted(accent(dialogPalette, "#ef4444", "#f87171")));
        StackPane iconHolder = new StackPane(iconBg, icon);
        iconHolder.setMinSize(52, 52);
        iconHolder.setMaxSize(52, 52);

        Label title = new Label("Error");
        title.setStyle(titleStyle(dialogPalette));

        Label messageL = new Label(message);
        messageL.setWrapText(true);
        messageL.setMaxWidth(380);
        messageL.setAlignment(Pos.CENTER);
        messageL.setStyle(bodyStyle(dialogPalette));

        Button okBtn = new Button("OK");
        okBtn.setPrefHeight(42);
        okBtn.setPrefWidth(140);
        stylePrimaryButton(okBtn, "#ef4444", "#dc2626");
        okBtn.setOnAction(e -> dialog.close());

        content.getChildren().addAll(iconHolder, title, messageL, okBtn);
        card.getChildren().add(content);
        overlay.getChildren().add(card);

        Scene scene = new Scene(overlay, 440, 260);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        double sw = Screen.getPrimary().getVisualBounds().getWidth();
        double sh = Screen.getPrimary().getVisualBounds().getHeight();
        dialog.setX(sw / 2 - 220);
        dialog.setY(sh / 2 - 130);

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
        dimPane.setVisible(true);
        dimPane.setManaged(true);
        dimPane.setMouseTransparent(false);

        modalHost.setVisible(true);
        modalHost.setManaged(true);
        modalHost.setMouseTransparent(false);
        modalHost.getChildren().clear();
        DialogPalette dialogPalette = palette(modalHost.getScene());

        // ===== Card =====
        StackPane card = new StackPane();
        card.setMaxWidth(520);
        card.setPrefWidth(520);
        card.setMaxHeight(420);
        card.setPrefHeight(420);
        card.setStyle(cardStyle(dialogPalette, accent(dialogPalette, "#ffedd5", "#9a3412")));

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(22, 28, 22, 28));
        content.setFillWidth(true);
        content.setMaxHeight(Region.USE_PREF_SIZE);


        Label closeX = new Label("✕");
        styleCloseLabel(closeX, dialogPalette);

        Circle iconBg = new Circle(26, accentFill(dialogPalette, "#FFF4E6", "#4a2b10"));
        Label icon = new Label("🔒");
        icon.setStyle("-fx-font-size: 18px;");
        StackPane iconHolder = new StackPane(iconBg, icon);
        iconHolder.setMinSize(52, 52);
        iconHolder.setMaxSize(52, 52);

        Label title = new Label(messageTitle);
        title.setStyle(titleStyle(dialogPalette));
        title.setPadding(new Insets(6, 0, 0, 0));

        Label desc = new Label(messageText);
        desc.setWrapText(true);
        desc.setMaxWidth(440);
        desc.setAlignment(Pos.CENTER);
        desc.setStyle(mutedStyle(dialogPalette));

        Label question = new Label(questionText);
        question.setWrapText(true);
        question.setMaxWidth(440);
        question.setAlignment(Pos.CENTER);
        question.setStyle(strongStyle(dialogPalette));
        question.setPadding(new Insets(4, 0, 0, 0));

        Button openBtn = new Button("↪  Abrir turno");
        openBtn.setPrefHeight(42);
        openBtn.setPrefWidth(170);
        stylePrimaryButton(openBtn, "#22c55e", "#16a34a");

        Button cancelBtn = new Button("Cancelar");
        cancelBtn.setPrefHeight(42);
        cancelBtn.setPrefWidth(140);
        styleSecondaryButton(cancelBtn, dialogPalette);

        HBox buttons = new HBox(12, openBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(14, 0, 0, 0));

        content.getChildren().addAll(iconHolder, title, desc, question, buttons);

        card.getChildren().addAll(content, closeX);
        StackPane.setAlignment(closeX, Pos.TOP_RIGHT);
        StackPane.setMargin(closeX, new Insets(12, 14, 0, 0));

        modalHost.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        Runnable close = () -> {
            modalHost.getChildren().clear();
            modalHost.setVisible(false);
            modalHost.setManaged(false);

            dimPane.setVisible(false);
            dimPane.setManaged(false);

            // чистим обработчики, чтобы не накапливались
            dimPane.setOnMouseClicked(null);
            modalHost.setOnKeyPressed(null);
        };

        java.util.function.Consumer<Boolean> finish = ok -> {
            close.run();
            onResult.accept(ok);
        };

        openBtn.setOnAction(e -> finish.accept(true));
        cancelBtn.setOnAction(e -> finish.accept(false));
        closeX.setOnMouseClicked(e -> finish.accept(false));

        dimPane.setOnMouseClicked(e -> finish.accept(false));

        // ESC
        modalHost.requestFocus();
        modalHost.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) finish.accept(false);
        });

        // анимация
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

    public static void showCashSessionAlreadyOpenInPlace(
            StackPane modalHost,
            Region dimPane,
            int shiftNo,
            String openedAt,
            java.util.function.Consumer<Boolean> onResult // true = Abrir TPV, false = OK/Close
    ) {
        dimPane.setVisible(true);
        dimPane.setManaged(true);
        dimPane.setMouseTransparent(false);

        modalHost.setVisible(true);
        modalHost.setManaged(true);
        modalHost.setMouseTransparent(false);
        modalHost.getChildren().clear();
        DialogPalette dialogPalette = palette(modalHost.getScene());

        StackPane card = new StackPane();
        card.setPrefWidth(520);
        card.setMaxWidth(520);
        card.setPrefHeight(360);
        card.setMaxHeight(360);
        card.setStyle(cardStyle(dialogPalette, accent(dialogPalette, "#dcfce7", "#166534")));

        VBox content = new VBox(10);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(22, 28, 22, 28));

        Label closeX = new Label("✕");
        styleCloseLabel(closeX, dialogPalette);

        Circle iconBg = new Circle(26, accentFill(dialogPalette, "#ECFDF3", "#14532d"));
        Label icon = new Label("✅");
        icon.setStyle("-fx-font-size: 18px;");
        StackPane iconHolder = new StackPane(iconBg, icon);
        iconHolder.setMinSize(52, 52);
        iconHolder.setMaxSize(52, 52);

        Label title = new Label("Turno ya está abierto");
        title.setStyle(titleStyle(dialogPalette));
        title.setPadding(new Insets(6, 0, 0, 0));

        Label desc = new Label("Ya existe un turno activo. Puedes continuar trabajando con este turno.");
        desc.setWrapText(true);
        desc.setMaxWidth(440);
        desc.setAlignment(Pos.CENTER);
        desc.setStyle(mutedStyle(dialogPalette));

        VBox infoBox = new VBox(8);
        infoBox.setMaxWidth(440);
        infoBox.setStyle(infoBoxStyle(dialogPalette));

        Label line1 = new Label("🧾  Turno:  " + shiftNo);
        line1.setStyle(strongStyle(dialogPalette));

        Label line2 = new Label("🕒  Apertura:  " + openedAt);
        line2.setStyle(mediumStyle(dialogPalette));

        infoBox.getChildren().addAll(line1, line2);

        Button openBtn = new Button("↪  Abrir TPV");
        openBtn.setPrefHeight(42);
        openBtn.setPrefWidth(170);
        stylePrimaryButton(openBtn, "#22c55e", "#16a34a");

        Button okBtn = new Button("OK");
        okBtn.setPrefHeight(42);
        okBtn.setPrefWidth(140);
        styleSecondaryButton(okBtn, dialogPalette);

        HBox buttons = new HBox(12, openBtn, okBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(14, 0, 0, 0));

        content.getChildren().addAll(iconHolder, title, desc, infoBox, buttons);

        card.getChildren().addAll(content, closeX);
        StackPane.setAlignment(closeX, Pos.TOP_RIGHT);
        StackPane.setMargin(closeX, new Insets(12, 14, 0, 0));

        modalHost.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        Runnable close = () -> {
            modalHost.getChildren().clear();
            modalHost.setVisible(false);
            modalHost.setManaged(false);

            dimPane.setVisible(false);
            dimPane.setManaged(false);

            dimPane.setOnMouseClicked(null);
            modalHost.setOnKeyPressed(null);
        };

        java.util.function.Consumer<Boolean> finish = v -> {
            close.run();
            onResult.accept(v);
        };

        openBtn.setOnAction(e -> finish.accept(true));
        okBtn.setOnAction(e -> finish.accept(false));
        closeX.setOnMouseClicked(e -> finish.accept(false));
        dimPane.setOnMouseClicked(e -> finish.accept(false));

        modalHost.requestFocus();
        modalHost.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) finish.accept(false);
            if (e.getCode() == KeyCode.ENTER) finish.accept(true);
        });

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

    /**
     * Prompt shown after opening a new shift when no CashCount exists yet.
     * Consumer receives: true = "Registrar arqueo", false = "Continuar con 0"
     */
    public static void showCashCountPromptInPlace(
            StackPane modalHost,
            Region dimPane,
            java.util.function.Consumer<Boolean> onResult
    ) {
        dimPane.setVisible(true);
        dimPane.setManaged(true);
        dimPane.setMouseTransparent(false);

        modalHost.setVisible(true);
        modalHost.setManaged(true);
        modalHost.setMouseTransparent(false);
        modalHost.getChildren().clear();
        DialogPalette dialogPalette = palette(modalHost.getScene());

        StackPane card = new StackPane();
        card.setMaxWidth(520);
        card.setPrefWidth(520);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setStyle(cardStyle(dialogPalette, accent(dialogPalette, "#ffedd5", "#9a3412")));

        VBox content = new VBox(18);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(28, 28, 24, 28));

        Label closeX = new Label("✕");
        styleCloseLabel(closeX, dialogPalette);

        Circle iconBg = new Circle(26, accentFill(dialogPalette, "#FFF4E6", "#4a2b10"));
        Label icon = new Label("💰");
        icon.setStyle("-fx-font-size: 18px;");
        StackPane iconHolder = new StackPane(iconBg, icon);
        iconHolder.setMinSize(52, 52);
        iconHolder.setMaxSize(52, 52);

        Label title = new Label("Arqueo inicial");
        title.setStyle(titleStyle(dialogPalette));

        Label desc = new Label("No hay un arqueo registrado para este turno.");
        desc.setWrapText(true);
        desc.setMaxWidth(440);
        desc.setAlignment(Pos.CENTER);
        desc.setStyle(mutedStyle(dialogPalette));

        Label question = new Label("¿Quieres registrar el efectivo en caja ahora?");
        question.setWrapText(true);
        question.setMaxWidth(440);
        question.setAlignment(Pos.CENTER);
        question.setStyle(strongStyle(dialogPalette));

        Button registerBtn = new Button("📋  Registrar arqueo");
        registerBtn.setPrefHeight(42);
        registerBtn.setPrefWidth(200);
        stylePrimaryButton(registerBtn, "#f97316", "#ea6c08");

        Button skipBtn = new Button("Continuar con 0");
        skipBtn.setPrefHeight(42);
        skipBtn.setPrefWidth(160);
        styleSecondaryButton(skipBtn, dialogPalette);

        HBox buttons = new HBox(12, registerBtn, skipBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        content.getChildren().addAll(iconHolder, title, desc, question, buttons);
        card.getChildren().addAll(content, closeX);
        StackPane.setAlignment(closeX, Pos.TOP_RIGHT);
        StackPane.setMargin(closeX, new Insets(12, 14, 0, 0));

        modalHost.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        Runnable close = () -> {
            modalHost.getChildren().clear();
            modalHost.setVisible(false);
            modalHost.setManaged(false);
            dimPane.setVisible(false);
            dimPane.setManaged(false);
            dimPane.setOnMouseClicked(null);
            modalHost.setOnKeyPressed(null);
        };

        java.util.function.Consumer<Boolean> finish = v -> {
            close.run();
            onResult.accept(v);
        };

        registerBtn.setOnAction(e -> finish.accept(true));
        skipBtn.setOnAction(e -> finish.accept(false));
        closeX.setOnMouseClicked(e -> finish.accept(false));
        dimPane.setOnMouseClicked(e -> finish.accept(false));

        modalHost.requestFocus();
        modalHost.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) finish.accept(false);
            if (e.getCode() == KeyCode.ENTER) finish.accept(true);
        });

        card.setOpacity(0);
        card.setTranslateY(18);
        FadeTransition fade = new FadeTransition(Duration.millis(180), card);
        fade.setFromValue(0); fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(180), card);
        slide.setFromY(18); slide.setToY(0);
        fade.play();
        slide.play();
    }

    /**
     * Phase 10 — D-04: Simple info dialog (green accent).
     * Blocks until the user dismisses it.
     */
    public static void showInfo(String message) {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        StackPane overlay = new StackPane();
        DialogPalette dialogPalette = palette(null);

        StackPane card = new StackPane();
        card.setMaxWidth(440);
        card.setPrefWidth(440);
        card.setStyle(cardStyle(dialogPalette, accent(dialogPalette, "#dcfce7", "#166534")));

        VBox content = new VBox(16);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(28, 28, 24, 28));

        Circle iconBg = new Circle(26, accentFill(dialogPalette, "#DCFCE7", "#14532d"));
        Label icon = new Label("✓");
        icon.setStyle("""
            -fx-font-size: 18px;
            -fx-text-fill: %s;
        """.formatted(accent(dialogPalette, "#16a34a", "#4ade80")));
        StackPane iconHolder = new StackPane(iconBg, icon);
        iconHolder.setMinSize(52, 52);
        iconHolder.setMaxSize(52, 52);

        Label title = new Label("Éxito");
        title.setStyle(titleStyle(dialogPalette));

        Label messageL = new Label(message);
        messageL.setWrapText(true);
        messageL.setMaxWidth(380);
        messageL.setAlignment(Pos.CENTER);
        messageL.setStyle(bodyStyle(dialogPalette));

        Button okBtn = new Button("OK");
        okBtn.setPrefHeight(42);
        okBtn.setPrefWidth(140);
        stylePrimaryButton(okBtn, "#16a34a", "#15803d");
        okBtn.setOnAction(e -> dialog.close());

        content.getChildren().addAll(iconHolder, title, messageL, okBtn);
        card.getChildren().add(content);
        overlay.getChildren().add(card);

        Scene scene = new Scene(overlay, 440, 260);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        double sw = Screen.getPrimary().getVisualBounds().getWidth();
        double sh = Screen.getPrimary().getVisualBounds().getHeight();
        dialog.setX(sw / 2 - 220);
        dialog.setY(sh / 2 - 130);

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

        dialog.showAndWait();
    }

    /**
     * Phase 10 — D-04: Two-button confirmation dialog.
     * Returns true if the user clicks confirmText, false if cancelText or closes.
     *
     * @param title       Dialog title
     * @param message     Dialog body text
     * @param confirmText Label for the confirm/affirmative button
     * @param cancelText  Label for the cancel button
     */
    public static boolean showConfirm(String title, String message, String confirmText, String cancelText) {
        AtomicBoolean result = new AtomicBoolean(false);

        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        StackPane overlay = new StackPane();
        DialogPalette dialogPalette = palette(null);

        StackPane card = new StackPane();
        card.setMaxWidth(480);
        card.setPrefWidth(480);
        card.setStyle(cardStyle(dialogPalette, accent(dialogPalette, "#ffedd5", "#9a3412")));

        VBox content = new VBox(18);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(28, 28, 24, 28));

        Circle iconBg = new Circle(26, accentFill(dialogPalette, "#FFF4E6", "#4a2b10"));
        Label iconL = new Label("↪");
        iconL.setStyle("""
            -fx-font-size: 18px;
            -fx-text-fill: %s;
        """.formatted(accent(dialogPalette, "#f97316", "#fb923c")));
        StackPane iconHolder = new StackPane(iconBg, iconL);
        iconHolder.setMinSize(52, 52);
        iconHolder.setMaxSize(52, 52);

        Label titleL = new Label(title);
        titleL.setStyle(titleStyle(dialogPalette));

        Label messageL = new Label(message);
        messageL.setWrapText(true);
        messageL.setMaxWidth(400);
        messageL.setAlignment(Pos.CENTER);
        messageL.setStyle(bodyStyle(dialogPalette));

        Button confirmBtn = new Button(confirmText);
        confirmBtn.setPrefHeight(42);
        confirmBtn.setPrefWidth(160);
        stylePrimaryButton(confirmBtn, "#f97316", "#ea6c08");
        confirmBtn.setOnAction(e -> { result.set(true); dialog.close(); });

        Button cancelBtn = new Button(cancelText);
        cancelBtn.setPrefHeight(42);
        cancelBtn.setPrefWidth(140);
        styleSecondaryButton(cancelBtn, dialogPalette);
        cancelBtn.setOnAction(e -> { result.set(false); dialog.close(); });

        HBox buttons = new HBox(12, confirmBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        content.getChildren().addAll(iconHolder, titleL, messageL, buttons);
        card.getChildren().add(content);
        overlay.getChildren().add(card);

        Scene scene = new Scene(overlay, 480, 280);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        double sw = Screen.getPrimary().getVisualBounds().getWidth();
        double sh = Screen.getPrimary().getVisualBounds().getHeight();
        dialog.setX(sw / 2 - 240);
        dialog.setY(sh / 2 - 140);

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

        dialog.showAndWait();

        return result.get();
    }

    /**
     * Confirmation dialog before saving arqueo / closing shift.
     * Dims the owner scene in-place, shows a card with Guardar/Cancelar buttons.
     * onConfirm runs if user confirms, onCancel runs if user cancels/closes.
     */
    public static void confirmGuardarArqueoInScene(
            Scene ownerScene,
            Runnable onConfirm,
            Runnable onCancel) {

        Parent originalRoot = ownerScene.getRoot();
        DialogPalette dialogPalette = palette(ownerScene);
        StackPane dimWrapper = new StackPane(originalRoot);
        Region dimOverlay = new Region();
        dimOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        dimOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        dimWrapper.getChildren().add(dimOverlay);
        ownerScene.setRoot(dimWrapper);

        StackPane card = new StackPane();
        card.setMaxWidth(480);
        card.setPrefWidth(480);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setStyle(cardStyle(dialogPalette, accent(dialogPalette, "#dcfce7", "#166534")));

        VBox content = new VBox(18);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(28, 28, 24, 28));

        Circle iconBg = new Circle(26, accentFill(dialogPalette, "#F0FDF4", "#14532d"));
        Label icon = new Label("💾");
        icon.setStyle("-fx-font-size: 18px;");
        StackPane iconHolder = new StackPane(iconBg, icon);
        iconHolder.setMinSize(52, 52);
        iconHolder.setMaxSize(52, 52);

        Label title = new Label("Confirmar arqueo");
        title.setStyle(titleStyle(dialogPalette));

        Label desc = new Label("¿Estás seguro de que quieres guardar el arqueo y cerrar el turno?");
        desc.setWrapText(true);
        desc.setMaxWidth(400);
        desc.setAlignment(Pos.CENTER);
        desc.setStyle(mutedStyle(dialogPalette));

        Label warning = new Label("Esta acción cerrará el turno actual y no podrá deshacerse.");
        warning.setWrapText(true);
        warning.setMaxWidth(400);
        warning.setAlignment(Pos.CENTER);
        warning.setStyle(strongStyle(dialogPalette));

        Button confirmBtn = new Button("Guardar arqueo");
        confirmBtn.setPrefHeight(42);
        confirmBtn.setPrefWidth(180);
        stylePrimaryButton(confirmBtn, "#16a34a", "#15803d");

        Button cancelBtn = new Button("Cancelar");
        cancelBtn.setPrefHeight(42);
        cancelBtn.setPrefWidth(140);
        styleSecondaryButton(cancelBtn, dialogPalette);

        HBox buttons = new HBox(12, cancelBtn, confirmBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        content.getChildren().addAll(iconHolder, title, desc, warning, buttons);
        card.getChildren().add(content);

        dimWrapper.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        Runnable restore = () -> {
            dimWrapper.getChildren().remove(originalRoot);
            ownerScene.setRoot(originalRoot);
        };

        confirmBtn.setOnAction(e -> { restore.run(); onConfirm.run(); });
        cancelBtn.setOnAction(e -> { restore.run(); onCancel.run(); });
        dimOverlay.setOnMouseClicked(e -> { restore.run(); onCancel.run(); });

        card.setOpacity(0);
        card.setTranslateY(18);
        FadeTransition fade2 = new FadeTransition(Duration.millis(180), card);
        fade2.setFromValue(0); fade2.setToValue(1);
        TranslateTransition slide2 = new TranslateTransition(Duration.millis(180), card);
        slide2.setFromY(18); slide2.setToY(0);
        fade2.play();
        slide2.play();
    }


}
