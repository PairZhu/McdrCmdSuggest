package com.pairzhu.mcdrcmdsuggest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

public interface SuggestImplement {
    String[] getSuggestions(ServerPlayerEntity player, String command);
}


// HTTP实现
class HttpSuggestImplement implements SuggestImplement {
    private final String host;
    private final int port;
    private final HttpClient httpClient;

    public HttpSuggestImplement(String host, int port) {
        this.host = host;
        this.port = port;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String[] getSuggestions(ServerPlayerEntity player, String command) {
        try {
            // 构建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http", null, host, port, "/suggest",
                            "player=%s&command=%s".formatted(player.getName().getString(), command),
                            null))
                    .GET().build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return JSON.parseArray(response.body(), String.class).toArray(new String[0]);
            } else {
                System.err.println("Failed to get suggestions: " + response.statusCode());
                return new String[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }
}


// STDIO实现
class StdioSuggestImplement implements SuggestImplement {
    private static final int TIMEOUT_MS = 5000;
    private static final String COMMAND_PREFIX = "$$__McdrCmdSuggest$$";
    private static final String COMMAND_SUFFIX = "$$/__McdrCmdSuggest$$";

    @Override
    public String[] getSuggestions(ServerPlayerEntity player, String command) {
        // 生成UUID作为请求标识符
        String requestId = UUID.randomUUID().toString();
        // 构建请求JSON
        JSONObject requestJson = new JSONObject();
        requestJson.put("requestId", requestId);
        requestJson.put("player", player.getName().getString());
        requestJson.put("command", command);
        String requestString = COMMAND_PREFIX + requestJson.toString() + COMMAND_SUFFIX;
        // 发送请求到标准输出
        System.out.println(requestString);
        throw new UnsupportedOperationException("STDIO suggest implement is not supported yet");

        // return new String[0];
    }
}
