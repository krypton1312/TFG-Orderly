package com.yebur.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class CashCountModelController {

    @FXML private BorderPane modalCard;

    @FXML private Label modalDisplayLabel;
    @FXML private Label totalLabel;

    private final StringBuilder input = new StringBuilder("0");

    @FXML
    public void initialize() {
        updateDisplay();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) modalCard.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onDigit(javafx.event.ActionEvent e) {
        String d = ((Button) e.getSource()).getText();
        if (input.toString().equals("0") && !input.toString().contains(".")) {
            input.setLength(0);
            input.append(d);
        } else if (input.length() < 12) {
            input.append(d);
        }
        updateDisplay();
    }

    @FXML
    private void onDoubleZero() {
        if (input.toString().equals("0") && !input.toString().contains(".")) return;
        if (input.length() <= 10) input.append("00");
        updateDisplay();
    }

    @FXML
    private void onDot() {
        if (!input.toString().contains(".")) input.append(".");
        updateDisplay();
    }

    @FXML
    private void onBackspace() {
        if (input.length() <= 1) {
            input.setLength(0);
            input.append("0");
        } else {
            input.deleteCharAt(input.length() - 1);
            if (input.length() == 0) input.append("0");
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
        BigDecimal v = parse().setScale(2, RoundingMode.HALF_UP);
        input.setLength(0);
        input.append(v.toPlainString());
        updateDisplay();
    }

    private BigDecimal parse() {
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
        BigDecimal v = parse().setScale(2, RoundingMode.HALF_UP);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        modalDisplayLabel.setText(nf.format(v));
        totalLabel.setText(nf.format(v));
    }

    @FXML private void onAccept() { onClose(); }
    @FXML private void onClearAll() { onClear(); }
    @FXML private void onPrint() { System.out.println("print..."); }
}
