package com.yebur.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PortalController {

    @FXML
    private ImageView logoImage;

    public void initialize() {
        logoImage.setImage(new Image(
                getClass().getResourceAsStream("/com/yebur/icons/logo.png"),
                32, 32, true, false // width, height, preserveRatio, smooth
        ));

    }
}
