package com.yebur.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";

    private ApiClient() {}

    public static String get(String endpoint) throws Exception {
        URL url = URI.create(BASE_URL + endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        return readResponse(conn);
    }

    public static String post(String endpoint, String jsonInput) throws Exception {
        URL url = URI.create(BASE_URL + endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return readResponse(conn);
    }

    public static String put(String endpoint, String jsonInput) throws Exception {
        URL url = URI.create(BASE_URL + endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return readResponse(conn);
    }

    public static String delete(String endpoint) throws Exception {
        URL url = URI.create(BASE_URL + endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");

        return readResponse(conn);
    }

    private static String readResponse(HttpURLConnection conn) throws Exception {
        int status = conn.getResponseCode();
        BufferedReader br;

        if (status >= 200 && status < 300) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder(); 
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line.trim());
        }
        br.close();
        conn.disconnect();

        return response.toString();
    }
}
