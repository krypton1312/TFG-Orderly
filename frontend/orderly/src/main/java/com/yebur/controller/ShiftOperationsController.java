package com.yebur.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class ShiftOperationsController {

    @FXML private VBox root;

    @FXML
    public void initialize() {

        root.getStylesheets().add(getClass().getResource("/com/yebur/portal/views/shiftOperations.css").toExternalForm());
    }
}
