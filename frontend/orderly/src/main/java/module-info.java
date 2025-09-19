module com.yebur {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.yebur to javafx.fxml;
    exports com.yebur;
}
