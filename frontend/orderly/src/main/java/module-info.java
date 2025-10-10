module com.yebur {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    opens com.yebur.app to javafx.fxml;
    opens com.yebur.controller to javafx.fxml;
    opens com.yebur.model.response to javafx.base;

    exports com.yebur.app;
    exports com.yebur.controller;
    exports com.yebur.model.response;
}
