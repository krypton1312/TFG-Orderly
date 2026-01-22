package com.yebur.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

public class CashCountModelController {

    @FXML private VBox coinsVBox;
    @FXML private VBox banknoteVBox;
    @FXML private BorderPane modalCard;

    @FXML private Label modalDisplayLabel; // текущее вводимое количество
    @FXML private Label totalLabel;        // итоговая сумма

    // текущее вводимое количество (целое)
    private long qty = 0;

    // итоговая сумма
    private BigDecimal total = BigDecimal.ZERO;

    // Сколько штук каждого номинала (denom -> count)
    private final Map<BigDecimal, Long> denomCounts = new LinkedHashMap<>();

    private final NumberFormat intFmt = NumberFormat.getIntegerInstance(Locale.GERMANY);
    private final NumberFormat eurFmt = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    @FXML
    public void initialize() {
        eurFmt.setCurrency(Currency.getInstance("EUR"));
        eurFmt.setMinimumFractionDigits(2);
        eurFmt.setMaximumFractionDigits(2);

        updateQtyDisplay();
        updateTotalDisplay();

        // Важно: banknoteVBox/coinsVBox должны быть не null (fx:id должен совпадать)
        Platform.runLater(() -> {
            if (banknoteVBox != null) hookDenomTextFields(banknoteVBox);
            if (coinsVBox != null) hookDenomTextFields(coinsVBox);

            // на всякий случай: после первичного сканирования
            recalcTotalFromAllDenoms();
            updateTotalDisplay();
        });
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) modalCard.getScene().getWindow();
        stage.close();
    }

    // ===== ВВОД ЦИФР В "ДИСПЛЕЙ" =====

    @FXML
    private void onDigit(ActionEvent e) {
        int d = Integer.parseInt(((Button) e.getSource()).getText());

        // ограничим кол-во цифр
        if (String.valueOf(qty).length() >= 12) return;

        qty = qty * 10 + d;
        updateQtyDisplay();
    }

    @FXML
    private void onDoubleZero() {
        if (qty == 0) return;
        if (String.valueOf(qty).length() <= 10) qty = qty * 100;
        updateQtyDisplay();
    }

    @FXML
    private void onBackspace() {
        qty = qty / 10;
        updateQtyDisplay();
    }

    @FXML
    private void onClear() {
        qty = 0;
        updateQtyDisplay();
    }

    // точка запрещена
    @FXML
    private void onDot() {
        // ничего
    }

    // ===== КЛИК ПО "СТРОКЕ НОМИНАЛА" =====
    // userData у HBox должен быть номиналом: "50", "20", "0.5", ...
    @FXML
    private void registerCuantity(MouseEvent mouseEvent) {
        HBox row = (HBox) mouseEvent.getSource();

        BigDecimal denom = parseDenom(String.valueOf(row.getUserData()));
        if (denom == null) return;

        TextField tf = findFirstTextField(row);
        if (tf != null) {
            // добавляем qty к тому, что уже есть в поле
            long current = parseLongSafe(tf.getText());
            tf.setText(String.valueOf(current + qty)); // listener сам обновит строку + total
        } else {
            // fallback (если вдруг нет поля) — обновим карту вручную
            denomCounts.merge(denom, qty, Long::sum);
            recalcTotalFromAllDenoms();
            updateTotalDisplay();

            // и обновим amount label
            Label amountLabel = findAmountLabel(row);
            if (amountLabel != null) {
                long count = denomCounts.getOrDefault(denom, 0L);
                BigDecimal rowSum = denom.multiply(BigDecimal.valueOf(count));
                amountLabel.setText(eurFmt.format(rowSum));
            }
        }

        // очистить дисплей
        qty = 0;
        updateQtyDisplay();
    }

    // ===== ПОДПИСКА НА ВСЕ TextField НОМИНАЛОВ =====

    private void hookDenomTextFields(Parent root) {
        for (Node node : root.lookupAll(".text-field")) {
            if (node instanceof TextField tf) {
                attachDigitsFilter(tf);
                attachRowAndTotalListener(tf);

                // учесть стартовое значение (у тебя text="0")
                updateFromTextField(tf);
            }
        }
    }

    private void attachDigitsFilter(TextField tf) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null; // только цифры
        };
        tf.setTextFormatter(new TextFormatter<>(filter));
    }

    private void attachRowAndTotalListener(TextField tf) {
        tf.textProperty().addListener((obs, oldText, newText) -> updateFromTextField(tf));
    }

    private void updateFromTextField(TextField tf) {
        HBox row = findParentRow(tf);
        if (row == null) return;

        BigDecimal denom = parseDenom(String.valueOf(row.getUserData()));
        if (denom == null) return;

        long count = parseLongSafe(tf.getText());

        // обновляем карту номиналов
        if (count <= 0) denomCounts.remove(denom);
        else denomCounts.put(denom, count);

        // обновляем amount label в этой строке
        Label amountLabel = findAmountLabel(row);
        if (amountLabel != null) {
            BigDecimal rowSum = denom.multiply(BigDecimal.valueOf(count));
            amountLabel.setText(eurFmt.format(rowSum));
        }

        // пересчёт total
        recalcTotalFromAllDenoms();
        updateTotalDisplay();
    }

    private HBox findParentRow(Node node) {
        Node cur = node;
        while (cur != null) {
            if (cur instanceof HBox h && h.getUserData() != null) {
                return h;
            }
            cur = cur.getParent();
        }
        return null;
    }

    /**
     * В каждой row-line: Label (название), TextField (кол-во), Label (сумма).
     * Берём последний Label.
     */
    private Label findAmountLabel(HBox row) {
        for (int i = row.getChildren().size() - 1; i >= 0; i--) {
            Node n = row.getChildren().get(i);
            if (n instanceof Label lbl) return lbl;
        }
        return null;
    }

    private TextField findFirstTextField(Parent root) {
        for (Node node : root.getChildrenUnmodifiable()) {
            if (node instanceof TextField tf) return tf;
            if (node instanceof Parent p) {
                TextField nested = findFirstTextField(p);
                if (nested != null) return nested;
            }
        }
        return null;
    }

    // ===== ПЕРЕСЧЁТ ИТОГА =====

    private void recalcTotalFromAllDenoms() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<BigDecimal, Long> e : denomCounts.entrySet()) {
            sum = sum.add(e.getKey().multiply(BigDecimal.valueOf(e.getValue())));
        }
        total = sum;
    }

    private BigDecimal parseDenom(String s) {
        try {
            if (s == null) return null;
            s = s.trim().replace(',', '.');
            return new BigDecimal(s);
        } catch (Exception ex) {
            return null;
        }
    }

    private long parseLongSafe(String s) {
        if (s == null || s.isBlank()) return 0L;
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return 0L;
        }
    }

    private void updateQtyDisplay() {
        modalDisplayLabel.setText(intFmt.format(qty)); // qty всегда целое
    }

    private void updateTotalDisplay() {
        totalLabel.setText(eurFmt.format(total)); // евро формат
    }

    // ===== КНОПКИ =====

    @FXML
    private void onAccept() {
        onClose();
    }

    @FXML
    private void onClearAll() {
        qty = 0;
        total = BigDecimal.ZERO;
        denomCounts.clear();

        // очистить все TextField номиналов и их amount labels
        if (banknoteVBox != null) clearAllRows(banknoteVBox);
        if (coinsVBox != null) clearAllRows(coinsVBox);

        updateQtyDisplay();
        updateTotalDisplay();
    }

    private void clearAllRows(Parent root) {
        for (Node node : root.lookupAll(".row-line")) {
            if (node instanceof HBox row) {
                // TextField -> "0"
                TextField tf = findFirstTextField(row);
                if (tf != null) tf.setText("0");

                // amount label -> "0,00 €"
                Label amountLabel = findAmountLabel(row);
                if (amountLabel != null) amountLabel.setText(eurFmt.format(BigDecimal.ZERO));
            }
        }
    }

    @FXML
    private void onPrint() {
        System.out.println("print...");
    }
}
