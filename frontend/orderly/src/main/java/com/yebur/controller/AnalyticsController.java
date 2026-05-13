package com.yebur.controller;

import com.yebur.model.response.MonthlySummaryResponse;
import com.yebur.model.response.TopProductEntry;
import com.yebur.service.AnalyticsService;
import com.yebur.ui.ThemeSupport;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AnalyticsController {

    @FXML private VBox root;
    @FXML private Button prevMonthBtn;
    @FXML private Button nextMonthBtn;
    @FXML private Label monthLabel;
    @FXML private Label revenueLabel, revPrevLabel, revDeltaLabel;
    @FXML private Label ordersLabel, ordersPrevLabel, ordersDeltaLabel;
    @FXML private Label avgLabel, avgPrevLabel, avgDeltaLabel;
    @FXML private Label cashPctLabel;
    @FXML private Label cardPctLabel;
    @FXML private VBox topProductsVBox;

    private YearMonth selectedMonth = YearMonth.now();
    private final DecimalFormat moneyFmt = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));

    @FXML
    public void initialize() {
        ThemeSupport.bindRootStylesheet(root);
        prevMonthBtn.setTooltip(new javafx.scene.control.Tooltip("Mes anterior"));
        prevMonthBtn.setAccessibleText("Mes anterior");
        nextMonthBtn.setTooltip(new javafx.scene.control.Tooltip("Mes siguiente"));
        nextMonthBtn.setAccessibleText("Mes siguiente");
        nextMonthBtn.setDisable(true);
        updateMonthLabel();
        loadStats();
    }

    @FXML
    private void prevMonth() {
        selectedMonth = selectedMonth.minusMonths(1);
        nextMonthBtn.setDisable(false);
        updateMonthLabel();
        loadStats();
    }

    @FXML
    private void nextMonth() {
        selectedMonth = selectedMonth.plusMonths(1);
        if (selectedMonth.equals(YearMonth.now())) nextMonthBtn.setDisable(true);
        updateMonthLabel();
        loadStats();
    }

    private void updateMonthLabel() {
        String raw = selectedMonth.format(MONTH_FMT);
        monthLabel.setText(Character.toUpperCase(raw.charAt(0)) + raw.substring(1));
    }

    private void loadStats() {
        setAllLabels("—");
        YearMonth current = selectedMonth;
        YearMonth previous = current.minusMonths(1);
        new Thread(() -> {
            try {
                MonthlySummaryResponse curr = AnalyticsService.getMonthlySummary(
                        current.getYear(), current.getMonthValue());
                MonthlySummaryResponse prev = AnalyticsService.getMonthlySummary(
                        previous.getYear(), previous.getMonthValue());
                Platform.runLater(() -> populateStats(curr, prev));
            } catch (Exception e) {
                Platform.runLater(() -> showError());
            }
        }).start();
    }

    private void setAllLabels(String value) {
        revenueLabel.setText(value);
        ordersLabel.setText(value);
        avgLabel.setText(value);
        cashPctLabel.setText(value);
        cardPctLabel.setText(value);
    }

    private void populateStats(MonthlySummaryResponse curr, MonthlySummaryResponse prev) {
        // Revenue
        revenueLabel.setText(moneyFmt.format(curr.getTotalRevenue()) + " €");
        revPrevLabel.setText("Mes anterior: " + moneyFmt.format(prev.getTotalRevenue()) + " €");
        applyDelta(revDeltaLabel, curr.getTotalRevenue(), prev.getTotalRevenue());

        // Orders
        ordersLabel.setText(String.valueOf(curr.getOrderCount()));
        ordersPrevLabel.setText("Mes anterior: " + prev.getOrderCount());
        applyDelta(ordersDeltaLabel,
                BigDecimal.valueOf(curr.getOrderCount()), BigDecimal.valueOf(prev.getOrderCount()));

        // Avg ticket
        avgLabel.setText(moneyFmt.format(curr.getAvgOrderValue()) + " €");
        avgPrevLabel.setText("Mes anterior: " + moneyFmt.format(prev.getAvgOrderValue()) + " €");
        applyDelta(avgDeltaLabel, curr.getAvgOrderValue(), prev.getAvgOrderValue());

        // Payment breakdown
        BigDecimal total = curr.getTotalRevenue();
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            cashPctLabel.setText("0% — 0,00 €");
            cardPctLabel.setText("0% — 0,00 €");
        } else {
            int cashPct = curr.getTotalSalesCash().multiply(BigDecimal.valueOf(100))
                    .divide(total, 0, RoundingMode.HALF_UP).intValue();
            cashPctLabel.setText(cashPct + "% — " + moneyFmt.format(curr.getTotalSalesCash()) + " €");
            cardPctLabel.setText((100 - cashPct) + "% — " + moneyFmt.format(curr.getTotalSalesCard()) + " €");
        }

        // Top products
        topProductsVBox.getChildren().clear();
        List<TopProductEntry> products = curr.getTopProducts();
        if (products == null || products.isEmpty()) {
            Label empty = new Label("Sin datos para este mes");
            empty.getStyleClass().add("analytics-empty-state");
            topProductsVBox.getChildren().add(empty);
        } else {
            for (int i = 0; i < products.size(); i++) {
                topProductsVBox.getChildren().add(buildProductRow(i + 1, products.get(i)));
            }
        }
    }

    private HBox buildProductRow(int rank, TopProductEntry entry) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("product-row");
        Label rankLbl = new Label("#" + rank);
        rankLbl.setMinWidth(28);
        rankLbl.getStyleClass().add("product-rank");
        Label nameLbl = new Label(entry.getName());
        nameLbl.getStyleClass().add("product-name");
        HBox.setHgrow(nameLbl, Priority.ALWAYS);
        Label qtyLbl = new Label(entry.getQuantity() + " uds");
        qtyLbl.getStyleClass().add("product-qty");
        row.getChildren().addAll(rankLbl, nameLbl, qtyLbl);
        return row;
    }

    private void applyDelta(Label label, BigDecimal current, BigDecimal prev) {
        if (prev == null || prev.compareTo(BigDecimal.ZERO) == 0) {
            label.setText("= sin cambio");
            label.getStyleClass().setAll("delta-neutral");
            return;
        }
        BigDecimal pct = current.subtract(prev)
                .divide(prev, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
        if (pct.compareTo(BigDecimal.ZERO) > 0) {
            label.setText("▲ +" + pct + "%");
            label.getStyleClass().setAll("delta-positive");
        } else if (pct.compareTo(BigDecimal.ZERO) < 0) {
            label.setText("▼ " + pct + "%");
            label.getStyleClass().setAll("delta-negative");
        } else {
            label.setText("= sin cambio");
            label.getStyleClass().setAll("delta-neutral");
        }
    }

    private void showError() {
        setAllLabels("Error al cargar los datos. Comprueba la conexión.");
        topProductsVBox.getChildren().clear();
    }
}
