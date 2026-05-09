package com.pairzhu.mcdrcmdsuggest.core;

import com.alibaba.fastjson2.JSONObject;
import java.util.UUID;

public class StdioSuggestProvider implements SuggestProvider {
    private static final String COMMAND_PREFIX = "$$__McdrCmdSuggest$$";
    private static final String COMMAND_SUFFIX = "$$/__McdrCmdSuggest$$";

    @Override
    public String[] getSuggestions(String playerName, String command) {
        String requestId = UUID.randomUUID().toString();
        JSONObject requestJson = new JSONObject();
        requestJson.put("requestId", requestId);
        requestJson.put("player", playerName);
        requestJson.put("command", command);
        String requestString = COMMAND_PREFIX + requestJson + COMMAND_SUFFIX;
        System.out.println(requestString);
        throw new UnsupportedOperationException("STDIO suggest implement is not supported yet");
    }
}
