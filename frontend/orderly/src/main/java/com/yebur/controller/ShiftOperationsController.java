package com.yebur.controller;

import com.yebur.model.response.ApiException;
import com.yebur.model.response.CashSessionResponse;
import com.yebur.service.CashSessionService;
import com.yebur.ui.CustomDialog;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class ShiftOperationsController {

    @FXML private VBox closeShiftVBox;
    @FXML private VBox reOpenShiftVBOX;
    @FXML private VBox root;
    private AnchorPane parentPane;

    @FXML
    public void initialize() {
        root.getStylesheets().add(getClass().getResource("/com/yebur/portal/views/shiftOperations.css").toExternalForm());
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
        refreshButtonState();
    }

    /**
     * Phase 10 — D-04: toggle disabled state on both cards based on current
     * session status. Called from initialize() and after every reopen attempt.
     */
    private void refreshButtonState() {
        boolean hasOpen = false;
        try {
            CashSessionResponse session = CashSessionService.findCashSessionByStatus("OPEN");
            hasOpen = session != null;
        } catch (ApiException e) {
            if (e.getStatusCode() != 404) {
                System.err.println("[ShiftOps] refreshButtonState failed: HTTP " + e.getStatusCode() + ": " + e.getMessage());
            }
            hasOpen = false;
        } catch (Exception e) {
            System.err.println("[ShiftOps] refreshButtonState failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            hasOpen = false;
        }
        closeShiftVBox.setDisable(!hasOpen);
        reOpenShiftVBOX.setDisable(hasOpen);
    }

    /**
     * Phase 10 — D-04 + D-05 client side. Shows a confirm modal, then calls the
     * backend reopen endpoint. Refreshes button state on success and on either
     * error path (409 already-open / generic).
     */
    @FXML
    public void onReopenShift(MouseEvent event) {
        // Step 1: confirmation modal
        boolean confirmed = CustomDialog.showConfirm(
                "Confirmar reapertura",
                "¿Reabrir el último turno cerrado?",
                "Reabrir",
                "Cancelar"
        );
        if (!confirmed) {
            return;
        }

        // Step 2: prevent double-click while request is in flight (T-10-16)
        reOpenShiftVBOX.setDisable(true);
        closeShiftVBox.setDisable(true);

        // Step 3: call backend
        try {
            CashSessionService.reopenCashSession();
            CustomDialog.showInfo("Turno reabierto correctamente.");
        } catch (Exception e) {
            if (e instanceof ApiException apiEx && apiEx.getStatusCode() == 409) {
                CustomDialog.showError("Ya hay un turno abierto. Ciérralo antes de reabrir otro.");
            } else {
                CustomDialog.showError("No se pudo reabrir el turno. Inténtalo de nuevo.");
            }
        } finally {
            // Step 4: resync (sets the correct card disabled based on the new state)
            refreshButtonState();
        }
    }

    public void showShiftOperationView(MouseEvent mouseEvent) {
        Object source = mouseEvent.getSource();

        if (source == closeShiftVBox) {
            if (!hasOpenCashSession()) {
                CustomDialog.showError("No hay un turno abierto.");
                return;
            }
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/yebur/portal/views/shiftOperationClose.fxml"));
                Node content = loader.load();
                ShiftOperationCloseController ctrl = loader.getController();
                ctrl.setOnAfterClose(() ->
                        loadCenterContent("/com/yebur/portal/views/shiftOperations.fxml"));
                parentPane.getChildren().setAll(content);
                AnchorPane.setTopAnchor(content, 20.0);
                AnchorPane.setBottomAnchor(content, 20.0);
                AnchorPane.setLeftAnchor(content, 20.0);
                AnchorPane.setRightAnchor(content, 20.0);
                applyFadeTransition(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean hasOpenCashSession() {
        try {
            return CashSessionService.findCashSessionByStatus("OPEN") != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void loadCenterContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();

            Object controller = loader.getController();

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
}
