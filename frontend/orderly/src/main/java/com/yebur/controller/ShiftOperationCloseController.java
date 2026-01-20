package com.yebur.controller;

import com.yebur.model.response.CashSessionResponse;
import com.yebur.service.CashSessionService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ShiftOperationCloseController {

    @FXML private VBox cashRoot;

    @FXML private Label dateValueLabel;
    @FXML private Label shiftValueLabel;
    @FXML private Label cashStartLabel;
    @FXML private Label displayLabel;
    @FXML private Label cashAmountLabel;
    @FXML private Label cardAmountLabel;

    @FXML private HBox cashHBox;
    @FXML private HBox cardHBox;

    private PauseTransition singleClickTimer;


    private CashSessionResponse cashSession;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    // калькулятор хранит строку ввода (без валюты)
    private final StringBuilder input = new StringBuilder("0");

    @FXML
    public void initialize() {
        cashRoot.getStylesheets().add(
                getClass().getResource("/com/yebur/portal/views/shiftOperationClose.css").toExternalForm()
        );

        getLastCashSession();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (cashSession != null) {
            dateValueLabel.setText(cashSession.getBusinessDate().format(formatter));
            shiftValueLabel.setText(String.valueOf(cashSession.getShiftNo()));
            cashStartLabel.setText(currencyFormatter.format(cashSession.getCashStart()));
        }

        // init display
        updateDisplay();
    }

    private void getLastCashSession() {
        try {
            this.cashSession = CashSessionService.findCashSessionByStatus("OPEN");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /* =========================
       CALCULATOR HANDLERS
       ========================= */

    @FXML
    private void onDigit(javafx.event.ActionEvent e) {
        String d = ((Button) e.getSource()).getText();

        // если "0" и нет точки — заменяем, чтобы не было "0000"
        if (input.toString().equals("0") && !input.toString().contains(".")) {
            input.setLength(0);
            input.append(d);
        } else {
            // ограничим длину (чтобы не улетало)
            if (input.length() < 12) {
                input.append(d);
            }
        }
        updateDisplay();
    }

    @FXML
    private void onDoubleZero() {
        // если сейчас "0" без точки — смысла нет добавлять "00"
        if (input.toString().equals("0") && !input.toString().contains(".")) {
            return;
        }
        if (input.length() <= 10) {
            input.append("00");
        }
        updateDisplay();
    }

    @FXML
    private void onDot() {
        if (!input.toString().contains(".")) {
            input.append(".");
        }
        updateDisplay();
    }

    @FXML
    private void onBackspace() {
        if (input.length() <= 1) {
            input.setLength(0);
            input.append("0");
        } else {
            input.deleteCharAt(input.length() - 1);
            // если стало пусто или "": вернём 0
            if (input.length() == 0 || input.toString().equals("-")) {
                input.setLength(0);
                input.append("0");
            }
        }
        updateDisplay();
    }

    @FXML
    private void onClear() {
        input.setLength(0);
        input.append("0");
        updateDisplay();
    }

    @FXML
    private void onEquals() {
        // тут можно, например, зафиксировать значение "как посчитано"
        // пока просто нормализуем до 2 знаков и обновляем дисплей
        BigDecimal value = parseInputToBigDecimal();
        value = value.setScale(2, RoundingMode.HALF_UP);

        input.setLength(0);
        input.append(value.toPlainString());

        updateDisplay();

        // Если хочешь сразу записывать, например, в TOTAL CAJA:
        // totalCashLabel.setText(currencyFormatter.format(value));
    }

    private BigDecimal parseInputToBigDecimal() {
        try {
            String s = input.toString();

            if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
            if (s.isBlank()) s = "0";

            return new BigDecimal(s);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private void updateDisplay() {
        BigDecimal value = parseInputToBigDecimal().setScale(2, RoundingMode.HALF_UP);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        displayLabel.setText(nf.format(value));
    }

    @FXML
    private void onPayRowClick(MouseEvent e) {
        Node node = (Node) e.getTarget();
        while (node != null && !(node instanceof HBox)) node = node.getParent();
        if (!(node instanceof HBox row)) return;

        BigDecimal value = parseInputToBigDecimal().setScale(2, RoundingMode.HALF_UP);
        String formatted = currencyFormatter.format(value);

        if (e.getClickCount() == 2) {
            if (singleClickTimer != null) singleClickTimer.stop();

            if (row == cashHBox) {
                openCashCountModal();
            }
            return;
        }
        if (singleClickTimer != null) singleClickTimer.stop();
        singleClickTimer = new PauseTransition(Duration.millis(220));
        singleClickTimer.setOnFinished(ev -> {
            if (row == cashHBox) {
                cashAmountLabel.setText(formatted);
                onClear();
            } else if (row == cardHBox) {
                cardAmountLabel.setText(formatted);
                onClear();
            }
        });
        singleClickTimer.playFromStart();
    }

    private void openCashCountModal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/yebur/portal/views/cashCountModel.fxml")
            );

            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/yebur/portal/views/cashCountModel.css").toExternalForm()
            );

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(cashRoot.getScene().getWindow());
            stage.setResizable(false);
            stage.setScene(scene);
            /*
            stage.setWidth(980);
            stage.setHeight(700); // <= 768
            */
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
