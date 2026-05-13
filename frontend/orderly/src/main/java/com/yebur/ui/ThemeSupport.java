package com.yebur.ui;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Window;

import java.net.URL;

public final class ThemeSupport {

    private static final String DARK_STYLESHEET = "/com/yebur/portal/portal-dark.css";
    private static final String BOUND_KEY = ThemeSupport.class.getName() + ".bound";
    private static final String ATTACHED_SCENE_KEY = ThemeSupport.class.getName() + ".attachedScene";
    private static final String LISTENER_KEY = ThemeSupport.class.getName() + ".listener";
    private static final String DARK_URL = resolveDarkUrl();

    private ThemeSupport() {
    }

    public static String darkStylesheetUrl() {
        return DARK_URL;
    }

    public static boolean isDark(Scene scene) {
        return scene != null && scene.getStylesheets().stream().anyMatch(ThemeSupport::isDarkStylesheet);
    }

    public static Scene findActiveScene() {
        Scene focusedScene = null;
        Scene fallbackScene = null;

        for (Window window : Window.getWindows()) {
            if (!window.isShowing() || window.getScene() == null) {
                continue;
            }
            if (window.isFocused()) {
                focusedScene = window.getScene();
                break;
            }
            if (fallbackScene == null) {
                fallbackScene = window.getScene();
            }
        }

        return focusedScene != null ? focusedScene : fallbackScene;
    }

    public static void setDark(ObservableList<String> stylesheets, boolean dark) {
        if (dark) {
            if (!stylesheets.contains(DARK_URL)) {
                stylesheets.add(DARK_URL);
            }
            return;
        }

        stylesheets.remove(DARK_URL);
        stylesheets.removeIf(ThemeSupport::isDarkStylesheet);
    }

    public static void bindRootStylesheet(Parent root) {
        if (Boolean.TRUE.equals(root.getProperties().get(BOUND_KEY))) {
            return;
        }

        root.getProperties().put(BOUND_KEY, Boolean.TRUE);
        root.sceneProperty().addListener((obs, oldScene, newScene) -> rebindSceneListener(root, oldScene, newScene));

        if (root.getScene() != null) {
            rebindSceneListener(root, null, root.getScene());
        }
    }

    public static void copyTheme(Parent root, Scene targetScene, Scene ownerScene) {
        boolean dark = isDark(ownerScene);
        setDark(targetScene.getStylesheets(), dark);
        if (root != null) {
            setDark(root.getStylesheets(), dark);
        }
    }

    public static void copyTheme(Scene targetScene, Scene ownerScene) {
        setDark(targetScene.getStylesheets(), isDark(ownerScene));
    }

    private static void rebindSceneListener(Parent root, Scene oldScene, Scene newScene) {
        @SuppressWarnings("unchecked")
        ListChangeListener<String> oldListener = (ListChangeListener<String>) root.getProperties().remove(LISTENER_KEY);
        Scene attachedScene = (Scene) root.getProperties().remove(ATTACHED_SCENE_KEY);

        if (attachedScene != null && oldListener != null) {
            attachedScene.getStylesheets().removeListener(oldListener);
        }

        if (newScene == null) {
            setDark(root.getStylesheets(), false);
            return;
        }

        ListChangeListener<String> listener = change -> syncRootStylesheet(root, newScene);
        newScene.getStylesheets().addListener(listener);
        root.getProperties().put(ATTACHED_SCENE_KEY, newScene);
        root.getProperties().put(LISTENER_KEY, listener);
        syncRootStylesheet(root, newScene);
    }

    private static void syncRootStylesheet(Parent root, Scene scene) {
        setDark(root.getStylesheets(), isDark(scene));
    }

    private static boolean isDarkStylesheet(String stylesheet) {
        return DARK_URL.equals(stylesheet) || stylesheet.contains("portal-dark.css");
    }

    private static String resolveDarkUrl() {
        URL url = ThemeSupport.class.getResource(DARK_STYLESHEET);
        if (url == null) {
            throw new IllegalStateException("Missing stylesheet: " + DARK_STYLESHEET);
        }
        return url.toExternalForm();
    }
}