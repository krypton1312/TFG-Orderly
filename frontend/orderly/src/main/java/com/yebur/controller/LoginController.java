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

    @FXML
    private void initialize() {
        var logoStream = getClass().getResourceAsStream("/com/yebur/icons/logo.png");
        if (logoStream != null) logoView.setImage(new Image(logoStream, 52, 52, true, true));
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        errorLabel.setText("");
        loginButton.setDisable(true);

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
                });
            }
        }).start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}
