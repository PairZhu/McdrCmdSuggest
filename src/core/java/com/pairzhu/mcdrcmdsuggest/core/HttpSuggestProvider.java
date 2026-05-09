package com.pairzhu.mcdrcmdsuggest.core;

import com.alibaba.fastjson2.JSON;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpSuggestProvider implements SuggestProvider {
    private final String host;
    private final int port;
    private final HttpClient httpClient;

    public HttpSuggestProvider(String host, int port) {
        this.host = host;
        this.port = port;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String[] getSuggestions(String playerName, String command) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http", null, host, port, "/suggest",
                            "player=%s&command=%s".formatted(playerName, command), null))
                    .GET().build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return JSON.parseArray(response.body(), String.class).toArray(new String[0]);
            }
            System.err.println("Failed to get suggestions: " + response.statusCode());
            return new String[0];
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }
}
