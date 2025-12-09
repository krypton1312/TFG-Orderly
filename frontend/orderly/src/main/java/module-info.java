module com.yebur {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.swing;              // для SwingFXUtils (snapshot -> BufferedImage)

    // Java / HTTP / AWT
    requires java.net.http;
    requires java.desktop;              // BufferedImage, Graphics2D и т.п.

    // Jackson
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    // PDFBox
    requires org.apache.pdfbox;

    // Lombok
    requires static lombok;

    // Открываем пакеты для FXML и Jackson
    opens com.yebur.app to javafx.fxml;
    opens com.yebur.controller to javafx.fxml;
    opens com.yebur.model.response to javafx.base, com.fasterxml.jackson.databind;
    opens com.yebur.model.request to com.fasterxml.jackson.databind;

    // Экспортируем API
    exports com.yebur.app;
    exports com.yebur.controller;
    exports com.yebur.model.response;
    exports com.yebur.model.request;
}
