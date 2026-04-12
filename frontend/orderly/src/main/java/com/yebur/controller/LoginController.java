package com.yebur.controller;

import com.yebur.app.App;
import com.yebur.model.response.ApiException;
import com.yebur.service.AuthService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ImageView logoView;

    private static final String BTN_NORMAL  = "-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 12 0; -fx-cursor: hand; -fx-border-width: 0; -fx-font-family: 'Inter', 'Segoe UI', sans-serif;";
    private static final String BTN_HOVER   = "-fx-background-color: #ea580c; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 12 0; -fx-cursor: hand; -fx-border-width: 0; -fx-font-family: 'Inter', 'Segoe UI', sans-serif;";
    private static final String BTN_DISABLED = "-fx-background-color: #fdba74; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 12 0; -fx-border-width: 0; -fx-font-family: 'Inter', 'Segoe UI', sans-serif;";

    @FXML
    private void initialize() {
        var logoStream = getClass().getResourceAsStream("/com/yebur/icons/logo.png");
        if (logoStream != null) logoView.setImage(new Image(logoStream, 52, 52, true, true));
        loginButton.setOnMouseEntered(e -> { if (!loginButton.isDisabled()) loginButton.setStyle(BTN_HOVER); });
        loginButton.setOnMouseExited(e  -> { if (!loginButton.isDisabled()) loginButton.setStyle(BTN_NORMAL); });
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        errorLabel.setText("");
        loginButton.setDisable(true);
        loginButton.setStyle(BTN_DISABLED);

        new Thread(() -> {
            try {
                AuthService.login(email, password);
                Platform.runLater(() -> {
                    try {
                        App.setRoot("portal");
                    } catch (IOException e) {
                        showError("Error al cargar la aplicación.");
                    }
                });
            } catch (ApiException e) {
                if (e.getStatusCode() == 401) {
                    Platform.runLater(() -> showError("Credenciales incorrectas."));
                } else {
                    Platform.runLater(() -> showError("Error del servidor (" + e.getStatusCode() + ")."));
                }
            } catch (IOException e) {
                Platform.runLater(() -> showError("No se puede conectar con el servidor."));
            } finally {
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    loginButton.setStyle(BTN_NORMAL);
                });
            }
        }).start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}
