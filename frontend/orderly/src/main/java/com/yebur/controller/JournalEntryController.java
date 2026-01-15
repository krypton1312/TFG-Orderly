package com.yebur.controller;

import com.yebur.model.request.CashOperationRequest;
import com.yebur.service.CashOperationService;
import com.yebur.ui.CustomDialog;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class JournalEntryController {

    @FXML private HBox amountHBOX;
    @FXML private Label dateValueLabel;
    @FXML private Label shiftValueLabel;
    @FXML private TextField conceptField;
    @FXML private TextField amountField;
    @FXML private Label titleLabel;
    @FXML private ComboBox<String> paymentCombo;
    @FXML private GridPane dataGP;
    private boolean shift = false;
    private TextField activeField;
    private static final boolean AMOUNT_ALLOW_DOT = true;
    @Getter private String paymentType;

    @FXML
    private void initialize() {
        if (paymentCombo != null && paymentCombo.getValue() == null) {
            paymentCombo.setValue("Efectivo");
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateValueLabel.setText(StartController.getCashSession().getBusinessDate().format(formatter));

        shiftValueLabel.setText(StartController.getCashSession().getShiftNo().toString());

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

        amountField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null) return;

            String cleaned = AMOUNT_ALLOW_DOT
                    ? newText.replaceAll("[^0-9.]", "")
                    : newText.replaceAll("[^0-9]", "");

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

    @FXML
    private void onKey(javafx.event.ActionEvent e) {
        Button b = (Button) e.getSource();
        String t = b.getText();
        if (t == null || t.isEmpty()) return;

        if (activeField == null) return;

        if (t.length() == 1) {
            char ch = t.charAt(0);

            if (isAmountActive()) {
                if (Character.isDigit(ch) || (AMOUNT_ALLOW_DOT && ch == '.' && !activeField.getText().contains("."))) {
                    appendRight(String.valueOf(ch));
                }
                return;
            }

            String toAdd = shift ? String.valueOf(ch).toUpperCase() : String.valueOf(ch);
            appendRight(toAdd);
        }
    }

    @FXML
    private void onSpace() {
        if (activeField == null) return;
        if (isAmountActive()) return;
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

    @FXML private void onExit()   {
        Stage stage = (Stage) paymentCombo.getScene().getWindow();
        stage.close();
    }
    @FXML private void onAccept() {
        if(verifyNotBlank()) {
            try {
                CashOperationService.createCashOperation(createPaymentRequest());
                onExit();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private CashOperationRequest createPaymentRequest(){
        return new CashOperationRequest(
            StartController.getCashSession().getId(),
                paymentType,
                paymentCombo.getValue().equals("Efectivo") ? "CASH" : "CARD",
                conceptField.getText(),
                new BigDecimal(amountField.getText())
        );
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
        applyPaymentType();
    }

    private void applyPaymentType() {
        if (titleLabel == null) return;
        if (paymentType == null) return;

        if ("DEPOSIT".equalsIgnoreCase(paymentType)) {
            titleLabel.setText("Apuntar entrada de dinero");
        } else if ("WITHDRAW".equalsIgnoreCase(paymentType)) {
            titleLabel.setText("Apuntar salida de dinero");
        } else {
            titleLabel.setText("Apuntar operación");
        }
    }

    private boolean verifyNotBlank() {
        boolean ok = true;

        conceptField.getStyleClass().remove("blank-element");
        amountField.getStyleClass().remove("blank-element");
        paymentCombo.getStyleClass().remove("blank-element");

        String concept = conceptField.getText();
        if (concept == null || concept.trim().isEmpty()) {
            conceptField.getStyleClass().add("blank-element");
            ok = false;
        }
        if (paymentCombo.getSelectionModel().getSelectedItem() == null) {
            paymentCombo.getStyleClass().add("blank-element");
            ok = false;
        }
        String amountTxt = amountField.getText();
        if (amountTxt == null || amountTxt.trim().isEmpty()) {
            amountField.getStyleClass().add("blank-element");
            ok = false;
        } else {
            try {
                BigDecimal amount = new BigDecimal(amountTxt.trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    amountHBOX.getStyleClass().add("blank-element");
                    ok = false;
                }
            } catch (NumberFormatException ex) {
                amountField.getStyleClass().add("blank-element");
                ok = false;
            }
        }

        return ok;
    }

    private void addClickHandler(Node node) {
        node.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> node.getStyleClass().remove("blank-element"));
    }
}
