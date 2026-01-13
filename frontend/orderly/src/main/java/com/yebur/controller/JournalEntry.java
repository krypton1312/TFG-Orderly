package com.yebur.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class JournalEntry {

    @FXML private TextField conceptField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> paymentCombo;

    private boolean shift = false;

    // Последний активный TextField (куда печатаем)
    private TextField activeField;

    // если хочешь разрешить точку в сумме (например 12.50) — true
    private static final boolean AMOUNT_ALLOW_DOT = true;

    @FXML
    private void initialize() {
        if (paymentCombo != null && paymentCombo.getValue() == null) {
            paymentCombo.setValue("Efectivo");
        }

        // стартовый активный — concept
        setActiveField(conceptField);

        conceptField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (newV) setActiveField(conceptField);
        });

        amountField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                setActiveField(amountField);

                // если было 0.00 — очищаем
                String txt = amountField.getText();
                if (txt == null || txt.isBlank() || txt.equals("0.00")) {
                    amountField.clear();
                }
            }
        });

        // Физическая клавиатура: фильтрация для amount
        amountField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null) return;

            String cleaned = AMOUNT_ALLOW_DOT
                    ? newText.replaceAll("[^0-9.]", "")
                    : newText.replaceAll("[^0-9]", "");

            // если разрешаем точку — оставляем только одну
            if (AMOUNT_ALLOW_DOT) {
                int firstDot = cleaned.indexOf('.');
                if (firstDot >= 0) {
                    cleaned = cleaned.substring(0, firstDot + 1) +
                            cleaned.substring(firstDot + 1).replace(".", "");
                }
            }

            if (!cleaned.equals(newText)) {
                amountField.setText(cleaned);
            }
        });

        // начальное значение
        if (amountField.getText() == null || amountField.getText().isBlank()) {
            amountField.setText("0.00");
        }
    }

    private void setActiveField(TextField tf) {
        activeField = tf;
        if (activeField != null) {
            activeField.requestFocus();
            activeField.positionCaret(activeField.getText().length());
        }
    }

    private boolean isAmountActive() {
        return activeField == amountField;
    }

    // ====== Keyboard handlers ======

    @FXML
    private void onKey(javafx.event.ActionEvent e) {
        Button b = (Button) e.getSource();
        String t = b.getText();
        if (t == null || t.isEmpty()) return;

        if (activeField == null) return;

        // одна буква/цифра
        if (t.length() == 1) {
            char ch = t.charAt(0);

            if (isAmountActive()) {
                // amount: только цифры (и точка, если разрешено)
                if (Character.isDigit(ch) || (AMOUNT_ALLOW_DOT && ch == '.' && !activeField.getText().contains("."))) {
                    appendRight(String.valueOf(ch));
                }
                return;
            }

            // concept: обычный ввод
            String toAdd = shift ? String.valueOf(ch).toUpperCase() : String.valueOf(ch);
            appendRight(toAdd);
        }
    }

    @FXML
    private void onSpace() {
        if (activeField == null) return;
        if (isAmountActive()) return; // пробел в сумме не нужен
        appendRight(" ");
    }

    @FXML
    private void onCom() {
        if (activeField == null) return;
        if (isAmountActive()) return;
        appendRight(".com");
    }

    @FXML
    private void onBackspace() {
        if (activeField == null) return;

        String s = activeField.getText();
        if (s != null && !s.isEmpty()) {
            activeField.setText(s.substring(0, s.length() - 1));
            activeField.positionCaret(activeField.getText().length());
        }
    }

    @FXML private void onLeft()  { if (activeField != null) activeField.backward(); }
    @FXML private void onRight() { if (activeField != null) activeField.forward(); }

    @FXML
    private void onShift() {
        shift = !shift;
    }

    @FXML private void onKeyboard() { }

    private void appendRight(String text) {
        if (activeField == null) return;
        String current = activeField.getText() == null ? "" : activeField.getText();
        activeField.setText(current + text);
        activeField.positionCaret(activeField.getText().length());
    }

    @FXML private void onExit()   { }
    @FXML private void onAccept() { }
}
