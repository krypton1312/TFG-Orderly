package com.yebur.ui;

import javafx.geometry.Pos;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;

/**
 * Applies fully custom cell rendering to a DatePicker.
 * Call DatePickerStyler.apply(picker) from initialize().
 */
public final class DatePickerStyler {

    private DatePickerStyler() {}

    private static final String COLOR_ORANGE       = "#f97316";
    private static final String COLOR_ORANGE_DARK  = "#ea580c";
    private static final String COLOR_ORANGE_LIGHT = "#fff7ed";
    private static final String COLOR_TEXT         = "#374151";
    private static final String COLOR_MUTED        = "#cbd5e1";
    private static final String COLOR_WHITE        = "white";

    public static void apply(DatePicker picker) {
        picker.setShowWeekNumbers(false);
        picker.setDayCellFactory(p -> new DateCell() {

            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setAlignment(Pos.CENTER);
                setGraphic(null);

                if (empty || date == null) {
                    setText(null);
                    setStyle("");
                    setOnMouseEntered(null);
                    setOnMouseExited(null);
                    return;
                }

                setText(String.valueOf(date.getDayOfMonth()));

                LocalDate today    = LocalDate.now();
                LocalDate selected = p.getValue();
                boolean isToday    = date.equals(today);
                boolean isSel      = selected != null && date.equals(selected);
                boolean isOther    = isOtherMonth(date, p);

                applyStyle(this, isToday, isSel, isOther, false);

                setOnMouseEntered(e -> {
                    if (!isSel) applyStyle(this, isToday, false, isOther, true);
                });
                setOnMouseExited(e -> {
                    applyStyle(this, isToday, isSel, isOther, false);
                });
            }
        });
    }

    private static boolean isOtherMonth(LocalDate date, DatePicker p) {
        // Determine the displayed month from picker value or today
        LocalDate ref = p.getValue() != null ? p.getValue() : LocalDate.now();
        return date.getMonthValue() != ref.getMonthValue() ||
               date.getYear()       != ref.getYear();
    }

    private static void applyStyle(DateCell cell,
                                   boolean isToday,
                                   boolean isSel,
                                   boolean isOther,
                                   boolean hover) {
        String base =
            "-fx-font-size: 13px;" +
            "-fx-alignment: center;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 0;" +
            "-fx-text-overrun: clip;" +
            "-fx-min-width: 32;" +
            "-fx-min-height: 32;" +
            "-fx-max-width: 32;" +
            "-fx-max-height: 32;";

        if (isSel) {
            cell.setStyle(base +
                "-fx-background-color: " + COLOR_ORANGE + ";" +
                "-fx-text-fill: " + COLOR_WHITE + ";" +
                "-fx-font-weight: 700;" +
                "-fx-border-color: transparent;");
        } else if (hover) {
            cell.setStyle(base +
                "-fx-background-color: " + COLOR_ORANGE_LIGHT + ";" +
                "-fx-text-fill: " + COLOR_ORANGE_DARK + ";" +
                "-fx-font-weight: 600;" +
                "-fx-border-color: transparent;");
        } else if (isToday) {
            cell.setStyle(base +
                "-fx-background-color: " + COLOR_ORANGE_LIGHT + ";" +
                "-fx-text-fill: " + COLOR_ORANGE_DARK + ";" +
                "-fx-font-weight: 700;" +
                "-fx-border-color: " + COLOR_ORANGE + ";" +
                "-fx-border-width: 1.5;");
        } else if (isOther) {
            cell.setStyle(base +
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + COLOR_MUTED + ";" +
                "-fx-font-weight: 400;" +
                "-fx-border-color: transparent;");
        } else {
            cell.setStyle(base +
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + COLOR_TEXT + ";" +
                "-fx-font-weight: 400;" +
                "-fx-border-color: transparent;");
        }
    }
}
