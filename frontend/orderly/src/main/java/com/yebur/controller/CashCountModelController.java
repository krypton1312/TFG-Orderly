package com.yebur.controller;

import com.yebur.model.request.CashCountRequest;
import com.yebur.model.response.CashCountResponse;
import com.yebur.model.response.CashSessionResponse;
import com.yebur.service.CashCountService;
import com.yebur.service.CashOperationService;
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
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Collections;
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

    // Denomination TextFields — injected directly for reliable preload()
    @FXML private TextField c1, c2, c5, c10, c20, c50, c100, c200;
    @FXML private TextField b5, b10, b20, b50, b100, b200, b500;

    // текущее вводимое количество (целое)
    private long qty = 0;

    // итоговая сумма
    @Getter
    private BigDecimal total = BigDecimal.ZERO;

    // Сколько штук каждого номинала (denom -> count)
    private final Map<BigDecimal, Long> denomCounts = new LinkedHashMap<>();

    // id existing CashCount if opened for edit (null = create mode)
    private Long existingCashCountId = null;

    @Getter
    private boolean accepted = false;

    @Setter
    private boolean persistCashCountOnAccept = true;

    private final NumberFormat intFmt = NumberFormat.getIntegerInstance(Locale.GERMANY);
    private final NumberFormat eurFmt = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    @Setter
    private CashSessionResponse currentCashSession;

    @FXML
    public void initialize() {
        eurFmt.setCurrency(Currency.getInstance("EUR"));
        eurFmt.setMinimumFractionDigits(2);
        eurFmt.setMaximumFractionDigits(2);

        updateQtyDisplay();
        updateTotalDisplay();

        // Use direct @FXML refs — avoids lookupAll(".text-field") which requires CSS to be
        // applied and returns empty when called before the stage is shown.
        Platform.runLater(() -> {
            for (TextField tf : new TextField[]{c1, c2, c5, c10, c20, c50, c100, c200,
                                               b5, b10, b20, b50, b100, b200, b500}) {
                if (tf == null) continue;
                attachDigitsFilter(tf);
                attachRowAndTotalListener(tf);
                updateFromTextField(tf);
            }
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

    private int getCount(BigDecimal denom) {
        return Math.toIntExact(denomCounts.getOrDefault(denom, 0L));
    }

    public Map<BigDecimal, Long> getDenomCounts() {
        return Collections.unmodifiableMap(denomCounts);
    }

    public CashCountRequest buildCashCountRequest() {
        CashCountRequest ccq = new CashCountRequest();
        ccq.setC001(getCount(new BigDecimal("0.01")));
        ccq.setC002(getCount(new BigDecimal("0.02")));
        ccq.setC005(getCount(new BigDecimal("0.05")));
        ccq.setC010(getCount(new BigDecimal("0.1")));
        ccq.setC020(getCount(new BigDecimal("0.2")));
        ccq.setC050(getCount(new BigDecimal("0.5")));
        ccq.setC100(getCount(new BigDecimal("1")));
        ccq.setC200(getCount(new BigDecimal("2")));
        ccq.setB005(getCount(new BigDecimal("5")));
        ccq.setB010(getCount(new BigDecimal("10")));
        ccq.setB020(getCount(new BigDecimal("20")));
        ccq.setB050(getCount(new BigDecimal("50")));
        ccq.setB100(getCount(new BigDecimal("100")));
        ccq.setB200(getCount(new BigDecimal("200")));
        ccq.setB500(getCount(new BigDecimal("500")));
        return ccq;
    }

    public void preload(CashCountResponse response) {
        this.existingCashCountId = response.getId();
        setFieldValue(c1,   response.getC001());
        setFieldValue(c2,   response.getC002());
        setFieldValue(c5,   response.getC005());
        setFieldValue(c10,  response.getC010());
        setFieldValue(c20,  response.getC020());
        setFieldValue(c50,  response.getC050());
        setFieldValue(c100, response.getC100());
        setFieldValue(c200, response.getC200());
        setFieldValue(b5,   response.getB005());
        setFieldValue(b10,  response.getB010());
        setFieldValue(b20,  response.getB020());
        setFieldValue(b50,  response.getB050());
        setFieldValue(b100, response.getB100());
        setFieldValue(b200, response.getB200());
        setFieldValue(b500, response.getB500());
    }

    public void preloadFromTotal(BigDecimal amount) {
        existingCashCountId = null;

        long remainingCents = normalizeAmountToCents(amount);
        remainingCents = setFieldFromRemaining(b500, remainingCents, 50_000);
        remainingCents = setFieldFromRemaining(b200, remainingCents, 20_000);
        remainingCents = setFieldFromRemaining(b100, remainingCents, 10_000);
        remainingCents = setFieldFromRemaining(b50, remainingCents, 5_000);
        remainingCents = setFieldFromRemaining(b20, remainingCents, 2_000);
        remainingCents = setFieldFromRemaining(b10, remainingCents, 1_000);
        remainingCents = setFieldFromRemaining(b5, remainingCents, 500);
        remainingCents = setFieldFromRemaining(c200, remainingCents, 200);
        remainingCents = setFieldFromRemaining(c100, remainingCents, 100);
        remainingCents = setFieldFromRemaining(c50, remainingCents, 50);
        remainingCents = setFieldFromRemaining(c20, remainingCents, 20);
        remainingCents = setFieldFromRemaining(c10, remainingCents, 10);
        remainingCents = setFieldFromRemaining(c5, remainingCents, 5);
        remainingCents = setFieldFromRemaining(c2, remainingCents, 2);
        setFieldFromRemaining(c1, remainingCents, 1);
    }

    private void setFieldValue(TextField tf, Integer count) {
        if (tf == null) return;
        tf.setText(count != null && count > 0 ? String.valueOf(count) : "0");
    }

    private long setFieldFromRemaining(TextField tf, long remainingCents, long denomCents) {
        int count = (int) (remainingCents / denomCents);
        setFieldValue(tf, count);
        return remainingCents % denomCents;
    }

    private long normalizeAmountToCents(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) return 0L;
        return amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValue();
    }


    // ===== КНОПКИ =====

    @FXML
    private void onAccept() {
        accepted = true;

        if (!persistCashCountOnAccept) {
            onClose();
            return;
        }

        CashCountRequest ccq = buildCashCountRequest();
        ccq.setSessionId(currentCashSession != null ? currentCashSession.getId() : null);

        try {
            if (existingCashCountId != null) {
                CashCountService.updateCashCount(existingCashCountId, ccq);
            } else {
                CashCountService.createCashCount(ccq);
            }
            onClose();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
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
