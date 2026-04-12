package com.yebur.service;

import java.util.prefs.Preferences;

public class SessionStore {

    private static final Preferences PREFS = Preferences.userNodeForPackage(SessionStore.class);
    private static final String REFRESH_KEY = "orderly_refresh_token";

    private static String accessToken;

    private SessionStore() {}

    public static void saveAccessToken(String token) {
        accessToken = token;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void saveRefreshToken(String token) {
        PREFS.put(REFRESH_KEY, token);
    }

    public static String getRefreshToken() {
        return PREFS.get(REFRESH_KEY, null);
    }

    public static void save(String access, String refresh) {
        saveAccessToken(access);
        saveRefreshToken(refresh);
    }

    public static void clear() {
        accessToken = null;
        PREFS.remove(REFRESH_KEY);
    }
}
