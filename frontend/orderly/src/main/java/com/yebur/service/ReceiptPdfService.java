package com.yebur.service;

import com.yebur.model.response.OrderDetailResponse;
import com.yebur.model.response.OrderResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReceiptPdfService {

    private static final float MM_TO_PT = 2.834f;
    private static final float RECEIPT_WIDTH_MM = 58f;
    private static final float PAGE_WIDTH = RECEIPT_WIDTH_MM * MM_TO_PT; // ≈ 164 pt
    private static final float MARGIN = 8f;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2;

    private static final float FONT_SIZE_TITLE = 9f;
    private static final float FONT_SIZE_TEXT = 7f;

    // колонки
    private static final float COL_ITEM_X = MARGIN;
    private static final float COL_QTY_X = MARGIN + CONTENT_WIDTH * 0.65f;
    // правая граница контента, от неё выравниваем total по правому краю
    private static final float COL_TOTAL_RIGHT_X = MARGIN + CONTENT_WIDTH;

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final String SHOP_NAME = "Orderly Bar";
    private static final String SHOP_ADDRESS = "C/ Valencia 123, Valencia";
    private static final String SHOP_PHONE = "+34 600 000 000";

    /**
     * Создаёт чек 58мм в PDF.
     */
    public void createReceipt58mm(Path outputPath,
                                  OrderResponse order,
                                  List<OrderDetailResponse> details) throws IOException {

        try (PDDocument document = new PDDocument()) {

            // Моноширинные шрифты, чтобы всё ровно стояло в колонках
            PDFont fontTitle = PDType1Font.COURIER_BOLD;
            PDFont fontText = PDType1Font.COURIER;

            float estimatedHeight = estimateHeight(details, fontText);
            PDPage page = new PDPage(new PDRectangle(PAGE_WIDTH, estimatedHeight));
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = estimatedHeight - MARGIN;

                // ====== Шапка магазина ======
                y = drawCenteredText(cs, fontTitle, FONT_SIZE_TITLE, SHOP_NAME, y);

                y -= 4;
                y = drawCenteredText(cs, fontText, FONT_SIZE_TEXT, SHOP_ADDRESS, y);
                y = drawCenteredText(cs, fontText, FONT_SIZE_TEXT, "Tel: " + SHOP_PHONE, y);

                y -= 4;
                y = drawSeparator(cs, y);

                // ====== Информация по заказу ======
                String tableInfo = order.getRestTable() != null
                        ? "Table: " + order.getRestTable().getNumber()
                        : "";
                String orderLine = "Order: " + order.getId()
                        + (tableInfo.isEmpty() ? "" : "  " + tableInfo);

                y = drawLeftText(cs, fontText, FONT_SIZE_TEXT, orderLine, y);

                if (order.getDatetime() != null) {
                    y = drawLeftText(cs, fontText, FONT_SIZE_TEXT,
                            "Date: " + order.getDatetime().format(DATE_TIME_FMT), y);
                }

                if (order.getPaymentMethod() != null) {
                    y = drawLeftText(cs, fontText, FONT_SIZE_TEXT,
                            "Payment: " + order.getPaymentMethod(), y);
                } else {
                    y = drawLeftText(cs, fontText, FONT_SIZE_TEXT,
                            "Payment: N/A", y);
                }

                y -= 4;
                y = drawSeparator(cs, y);

                // ====== Заголовок таблицы ======
                y = drawTableHeader(cs, fontText, FONT_SIZE_TEXT, y);
                y = drawSeparator(cs, y);

                // ====== Позиции заказа ======
                for (OrderDetailResponse d : details) {
                    y = drawItemLine(cs, fontText, FONT_SIZE_TEXT, d, y);
                }

                y = drawSeparator(cs, y);

                // ====== Итого ======
                BigDecimal total = BigDecimal.valueOf(order.getTotal())
                        .setScale(2, RoundingMode.HALF_UP);

                y = drawRightText(cs, fontText, FONT_SIZE_TEXT,
                        String.format("TOTAL: %.2f EUR", total), y);

                y -= 8;
                y = drawCenteredText(cs, fontText, FONT_SIZE_TEXT,
                        "Thank you!", y);
            }

            document.save(outputPath.toFile());
        }
    }

    /**
     * Примерная оценка высоты страницы, чтобы всё влезло на одну страницу.
     */
    private float estimateHeight(List<OrderDetailResponse> details,
                                 PDFont font) throws IOException {
        float lineHeight = FONT_SIZE_TEXT + 3;
        float titleHeight = FONT_SIZE_TITLE + 4;

        float h = MARGIN;

        // Шапка магазина
        h += titleHeight;  // shop name
        h += lineHeight;   // address
        h += lineHeight;   // phone
        h += 4;            // separator

        // Order info
        h += lineHeight;   // order + table
        h += lineHeight;   // date
        h += lineHeight;   // payment
        h += 4;            // separator

        // table header + separator
        h += lineHeight;   // "Item / Qty / Total"
        h += 4;            // separator

        // Items (каждый может занимать несколько строк)
        float nameWidth = COL_QTY_X - MARGIN - 2;
        for (OrderDetailResponse d : details) {
            String name = d.getName() != null ? d.getName() : ("Product " + d.getProductId());
            List<String> nameLines = wrapText(font, name, FONT_SIZE_TEXT, nameWidth);
            h += nameLines.size() * lineHeight;
        }

        h += 4;            // separator
        h += lineHeight;   // TOTAL
        h += lineHeight;   // "Thank you"
        h += MARGIN;

        return Math.max(h, 150f);
    }

    /**
     * Перенос текста по словам по заданной ширине.
     */
    private List<String> wrapText(PDFont font, String text,
                                  float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");

        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            String candidate = currentLine.length() == 0
                    ? word
                    : currentLine + " " + word;

            float width = font.getStringWidth(candidate) / 1000 * fontSize;
            if (width <= maxWidth) {
                currentLine.setLength(0);
                currentLine.append(candidate);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine.setLength(0);
                currentLine.append(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    // ====== Низкоуровневые методы рисования ======

    private float drawLeftText(PDPageContentStream cs, PDFont font,
                               float fontSize, String text, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(text);
        cs.endText();
        return y - (fontSize + 3);
    }

    private float drawCenteredText(PDPageContentStream cs, PDFont font,
                                   float fontSize, String text, float y) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (PAGE_WIDTH - textWidth) / 2f;
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - (fontSize + 3);
    }

    private float drawRightText(PDPageContentStream cs, PDFont font,
                                float fontSize, String text, float y) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = COL_TOTAL_RIGHT_X - textWidth;
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - (fontSize + 3);
    }

    /**
     * Горизонтальный разделитель на всю ширину чека.
     */
    private float drawSeparator(PDPageContentStream cs, float y) throws IOException {
        cs.moveTo(MARGIN, y);
        cs.lineTo(PAGE_WIDTH - MARGIN, y);
        cs.stroke();
        return y - 4;
    }

    /**
     * Заголовок для таблицы: Item / Qty / Total.
     */
    private float drawTableHeader(PDPageContentStream cs, PDFont font,
                                  float fontSize, float y) throws IOException {
        // Item
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(COL_ITEM_X, y);
        cs.showText("Item");
        cs.endText();

        // Qty
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(COL_QTY_X, y);
        cs.showText("Qty");
        cs.endText();

        // Total (выравниваем по правому краю колонки)
        String totalHeader = "Total";
        float tw = font.getStringWidth(totalHeader) / 1000 * fontSize;
        float totalX = COL_TOTAL_RIGHT_X - tw;

        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(totalX, y);
        cs.showText(totalHeader);
        cs.endText();

        return y - (fontSize + 3);
    }

    /**
     * Одна позиция заказа:
     * - имя (с переносом)
     * - на последней строке имени — qty и total в колонках.
     */
    private float drawItemLine(PDPageContentStream cs, PDFont font,
                               float fontSize, OrderDetailResponse d, float y) throws IOException {

        float nameWidth = COL_QTY_X - MARGIN - 2;
        String name = d.getName() != null ? d.getName() : ("Product " + d.getProductId());

        List<String> nameLines = wrapText(font, name, fontSize, nameWidth);

        int qty = d.getAmount();
        BigDecimal unitPrice = d.getUnitPrice() != null
                ? d.getUnitPrice()
                : BigDecimal.ZERO;

        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty))
                .setScale(2, RoundingMode.HALF_UP);

        String qtyStr = String.valueOf(qty);
        String totalStr = lineTotal.toString();

        for (int i = 0; i < nameLines.size(); i++) {
            String line = nameLines.get(i);

            // название
            cs.beginText();
            cs.setFont(font, fontSize);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(line);
            cs.endText();

            // на последней строке имени рисуем qty и total
            if (i == nameLines.size() - 1) {
                // Qty
                cs.beginText();
                cs.setFont(font, fontSize);
                cs.newLineAtOffset(COL_QTY_X, y);
                cs.showText(qtyStr);
                cs.endText();

                // Total по правому краю
                float totalWidth = font.getStringWidth(totalStr) / 1000 * fontSize;
                float totalX = COL_TOTAL_RIGHT_X - totalWidth;

                cs.beginText();
                cs.setFont(font, fontSize);
                cs.newLineAtOffset(totalX, y);
                cs.showText(totalStr);
                cs.endText();
            }

            y -= (fontSize + 3);
        }

        return y;
    }
}
