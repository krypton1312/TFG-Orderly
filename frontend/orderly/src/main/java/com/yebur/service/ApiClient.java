package com.yebur.service;

import com.yebur.model.response.ApiException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";

    private ApiClient() {}

    public static String get(String endpoint) throws IOException, ApiException {
        URL url = URI.create(BASE_URL + endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        return readResponse(conn);
    }

    public static String post(String endpoint, String jsonInput) throws IOException, ApiException {
        URL url = URI.create(BASE_URL + endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
        }

        return readResponse(conn);
    }

    public static String post(String endpoint) throws IOException, ApiException {
        URL url = URI.create(BASE_URL + endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(false);
        return readResponse(conn);
    }

    public static String put(String endpoint, String jsonInput) throws IOException, ApiException {
        URL url = URI.create(BASE_URL + endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
        }

        return readResponse(conn);
    }

    public static String delete(String endpoint) throws IOException, ApiException {
        URL url = URI.create(BASE_URL + endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");

        return readResponse(conn);
    }

    private static String readResponse(HttpURLConnection conn) throws IOException, ApiException {
        int status = conn.getResponseCode();
        InputStream stream;

        if (status >= 200 && status < 300) {
            stream = conn.getInputStream();
        } else {
            stream = conn.getErrorStream();
            if (stream == null) {
                conn.disconnect();
                throw new ApiException(status, "");
            }
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line.trim());
            }
        } finally {
            conn.disconnect();
        }

        String body = response.toString();

        if (status >= 200 && status < 300) {
            return body.isEmpty() ? null : body;
        } else {
            // кидаем исключение с кодом и текстом
            throw new ApiException(status, body);
        }
    }
}
