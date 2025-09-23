package com.yebur.controller;

import java.io.IOException;

import com.yebur.app.App;

import javafx.fxml.FXML;

public class SecondaryController {
 
    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}