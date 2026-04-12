package com.yebur.app;

import java.io.IOException;

import com.yebur.model.response.ApiException;
import com.yebur.service.AuthService;
import com.yebur.service.SessionStore;
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
        String startView = resolveStartView();

        scene = new Scene(loadFXML(startView));
        String css = App.class.getResource("/com/yebur/portal/portal.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Orderly POS");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/yebur/icons/icon.png")));
        stage.show();
        scene.getRoot().requestFocus();
    }

    // Attempt silent refresh; fall back to login screen if no token or refresh fails.
    private String resolveStartView() {
        String refreshToken = SessionStore.getRefreshToken();
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                AuthService.refresh(refreshToken);
                return "portal";
            } catch (IOException | ApiException ignored) {
                SessionStore.clear();
            }
        }
        return "login";
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
