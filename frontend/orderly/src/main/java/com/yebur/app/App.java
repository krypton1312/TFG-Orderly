package com.yebur.app;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        scene = new Scene(loadFXML("portal"));
        String css = App.class.getResource("/com/yebur/portal/portal.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Orderly POS");
        stage.initStyle(StageStyle.DECORATED);
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/yebur/icons/icon.png")));
        stage.show();
        scene.getRoot().requestFocus();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
            App.class.getResource("/com/yebur/portal/" + fxml + ".fxml")
        );
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
    
}
