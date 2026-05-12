package com.yebur.controller;

import com.yebur.model.request.ConfigRequest;
import com.yebur.model.response.ConfigResponse;
import com.yebur.model.response.SmtpTestResult;
import com.yebur.service.ConfigService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.print.Printer;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ConfiguracionController {

    @FXML private ToggleGroup themeGroup;
    @FXML private RadioButton lightThemeRadio;
    @FXML private RadioButton darkThemeRadio;

    @FXML private TextField smtpHostField;
    @FXML private TextField smtpPortField;
    @FXML private TextField smtpUserField;
    @FXML private PasswordField smtpPassField;
    @FXML private TextField smtpReceiverField;
    @FXML private CheckBox smtpTlsCheck;
    @FXML private Button testSmtpBtn;
    @FXML private Label smtpStatusLabel;

    @FXML private ComboBox<Printer> printerCombo;
    @FXML private Button refreshPrintersBtn;

    @FXML private Label saveStatusLabel;
    @FXML private Button saveBtn;

    @FXML private VBox rootVbox;

    private static final String DARK_CSS = "/com/yebur/portal/portal-dark.css";

    public void initialize() {
        populatePrinters();

        themeGroup.selectedToggleProperty().addListener((obs, old, newToggle) -> {
            if (newToggle == darkThemeRadio) {
                applyTheme(true);
            } else if (newToggle == lightThemeRadio) {
                applyTheme(false);
            }
        });

        new Thread(() -> {
            try {
                ConfigResponse config = ConfigService.getConfig();
                Platform.runLater(() -> populateForm(config));
            } catch (Exception ignored) {
                // First launch or backend unavailable — leave fields blank
            }
        }).start();
    }

    private void populateForm(ConfigResponse config) {
        if ("dark".equals(config.getTheme())) {
            darkThemeRadio.setSelected(true);
        } else {
            lightThemeRadio.setSelected(true);
        }

        smtpHostField.setText(config.getSmtpHost() != null ? config.getSmtpHost() : "");
        smtpPortField.setText(config.getSmtpPort() != null ? config.getSmtpPort().toString() : "");
        smtpUserField.setText(config.getSmtpUsername() != null ? config.getSmtpUsername() : "");
        smtpPassField.setText("");  // password never returned from API (D-05)
        smtpReceiverField.setText(config.getSmtpReceiverEmail() != null ? config.getSmtpReceiverEmail() : "");
        smtpTlsCheck.setSelected(Boolean.TRUE.equals(config.getSmtpUseTls()));

        if (config.getPrinterName() != null) {
            printerCombo.getItems().stream()
                    .filter(p -> config.getPrinterName().equals(p.getName()))
                    .findFirst()
                    .ifPresent(p -> printerCombo.getSelectionModel().select(p));
        }
    }

    private void populatePrinters() {
        List<Printer> printerList = new ArrayList<>(Printer.getAllPrinters());
        printerCombo.setItems(FXCollections.observableArrayList(printerList));
        printerCombo.setConverter(new StringConverter<Printer>() {
            @Override public String toString(Printer p) { return p != null ? p.getName() : ""; }
            @Override public Printer fromString(String s) { return null; }
        });
        if (printerList.isEmpty()) {
            printerCombo.setPromptText("No se encontraron impresoras");
        }
    }

    @FXML
    private void handleRefreshPrinters() {
        refreshPrintersBtn.setDisable(true);
        populatePrinters();
        refreshPrintersBtn.setDisable(false);
    }

    @FXML
    private void handleTestSmtp() {
        testSmtpBtn.setDisable(true);
        setSmtpStatus("Probando…", "smtp-status-testing");

        new Thread(() -> {
            try {
                SmtpTestResult result = ConfigService.testSmtp();
                Platform.runLater(() -> {
                    if (Boolean.TRUE.equals(result.getSuccess())) {
                        setSmtpStatus("Conexión correcta", "smtp-status-success");
                    } else {
                        String msg = result.getError() != null ? result.getError() : "Error desconocido";
                        setSmtpStatus("Error: " + msg, "smtp-status-error");
                    }
                    testSmtpBtn.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setSmtpStatus("Error: " + e.getMessage(), "smtp-status-error");
                    testSmtpBtn.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleSave() {
        saveBtn.setDisable(true);
        ConfigRequest request = buildRequest();

        new Thread(() -> {
            try {
                ConfigService.saveConfig(request);
                Platform.runLater(() -> {
                    setSaveStatus("Cambios guardados", "smtp-status-success");
                    saveBtn.setDisable(false);
                    new Thread(() -> {
                        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                        Platform.runLater(() -> setSaveStatus("", "smtp-status-idle"));
                    }).start();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setSaveStatus("Error al guardar", "smtp-status-error");
                    saveBtn.setDisable(false);
                });
            }
        }).start();
    }

    private ConfigRequest buildRequest() {
        ConfigRequest req = new ConfigRequest();
        req.setTheme(darkThemeRadio.isSelected() ? "dark" : "light");
        req.setSmtpHost(nullIfBlank(smtpHostField.getText()));
        String portText = smtpPortField.getText();
        if (portText != null && !portText.isBlank()) {
            try { req.setSmtpPort(Integer.parseInt(portText.trim())); }
            catch (NumberFormatException ignored) {}
        }
        req.setSmtpUsername(nullIfBlank(smtpUserField.getText()));
        req.setSmtpPassword(nullIfBlank(smtpPassField.getText()));
        req.setSmtpReceiverEmail(nullIfBlank(smtpReceiverField.getText()));
        req.setSmtpUseTls(smtpTlsCheck.isSelected());
        Printer selected = printerCombo.getSelectionModel().getSelectedItem();
        req.setPrinterName(selected != null ? selected.getName() : null);
        return req;
    }

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private void applyTheme(boolean dark) {
        Scene scene = rootVbox.getScene();
        if (scene == null) return;
        URL url = getClass().getResource(DARK_CSS);
        if (url == null) return;
        String darkCssUrl = url.toExternalForm();
        if (dark) {
            if (!scene.getStylesheets().contains(darkCssUrl)) {
                scene.getStylesheets().add(darkCssUrl);
            }
        } else {
            scene.getStylesheets().remove(darkCssUrl);
        }
    }

    private void setSmtpStatus(String text, String styleClass) {
        smtpStatusLabel.setText(text);
        smtpStatusLabel.getStyleClass().removeIf(s -> s.startsWith("smtp-status-"));
        smtpStatusLabel.getStyleClass().add(styleClass);
    }

    private void setSaveStatus(String text, String styleClass) {
        saveStatusLabel.setText(text);
        saveStatusLabel.getStyleClass().removeIf(s -> s.startsWith("smtp-status-"));
        saveStatusLabel.getStyleClass().add(styleClass);
    }
}
