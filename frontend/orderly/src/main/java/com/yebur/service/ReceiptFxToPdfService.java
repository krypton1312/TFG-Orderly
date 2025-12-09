package com.yebur.service;

import com.yebur.controller.PartialPaymentController;
import com.yebur.controller.ReceiptController;
import com.yebur.model.response.OrderDetailResponse;
import com.yebur.model.response.OrderResponse;
import com.yebur.model.response.RestTableResponse;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ReceiptFxToPdfService {

    /**
     * Генерирует PDF-чек из FXML-шаблона.
     * ВАЖНО: вызывать из JavaFX Application Thread (у тебя это уже так, т.к. из контроллера).
     */
    public void createReceiptPdf(Path outputPath,
                                 OrderResponse order,
                                 List<OrderDetailResponse> details,
                                 RestTableResponse table,
                                 PartialPaymentController.PaymentInfo paymentInfo) throws IOException {

        // 1. Загружаем FXML и заполняем контроллер данными
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/yebur/receipt/receipt-view.fxml"));
        Parent root = loader.load();
        ReceiptController controller = loader.getController();
        controller.setData(order, details, table, paymentInfo);

        // 2. Делаем layout (нужна сцена, чтобы CSS применились)
        Scene scene = new Scene(root);
        root.applyCss();
        root.layout();

        // 3. Делаем snapshot с повышенным масштабом (для качества) —
        //    НЕ задаём WritableImage вручную, чтобы не получить (0,0)
        double scale = 2.0;
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(javafx.scene.transform.Transform.scale(scale, scale));

        WritableImage fxImage = root.snapshot(params, null);

        if (fxImage.getWidth() <= 0 || fxImage.getHeight() <= 0) {
            throw new IllegalStateException(
                    "Receipt snapshot is empty (width=" + fxImage.getWidth()
                            + ", height=" + fxImage.getHeight() + ")");
        }

        BufferedImage awtImage = SwingFXUtils.fromFXImage(fxImage, null);

        // 4. Размер страницы под чек 58 мм
        final float MM_TO_PT = 2.834f;      // 1 мм ~ 2.834 pt
        float receiptWidthMm = 58f;
        float receiptWidthPt = receiptWidthMm * MM_TO_PT;

        // масштабируем по ширине в поинты
        float k = receiptWidthPt / (float) awtImage.getWidth();
        float receiptHeightPt = (float) (awtImage.getHeight() * k);

        PDRectangle pageSize = new PDRectangle(receiptWidthPt, receiptHeightPt);

        // 5. Создаём PDF и вставляем туда картинку
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(pageSize);
            doc.addPage(page);

            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, awtImage);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // рисуем картинку во весь чек
                cs.drawImage(pdImage, 0, 0, receiptWidthPt, receiptHeightPt);
            }

            doc.save(outputPath.toFile());
        }
    }

}
