package com.pairzhu.mcdrcmdsuggest.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class CommandParser {
    private CommandParser() {
    }

    public static ParsedCommandData parse(String jsonData) throws CommandValidationException {
        CommandData commandData = JSON.parseObject(jsonData, CommandData.class);
        if (commandData == null || commandData.commandNodes == null) {
            throw new CommandValidationException("Invalid command data format: missing or empty data");
        }

        SuggestProvider suggestProvider;
        switch (commandData.mode) {
            case STDIO:
                suggestProvider = new StdioSuggestProvider();
                break;
            case HTTP:
                if (commandData.port < 1 || commandData.port > 65535) {
                    throw new CommandValidationException("Invalid port number: must be between 1 and 65535");
                }
                suggestProvider = new HttpSuggestProvider(commandData.host, commandData.port);
                break;
            default:
                throw new CommandValidationException("Unsupported mode: " + commandData.mode);
        }

        List<CommandTreeNode> nodes = new ArrayList<>();
        for (JSONObject nodeJson : commandData.commandNodes) {
            nodes.add(new CommandTreeNode(nodeJson));
        }
        return new ParsedCommandData(commandData.mode, commandData.port, nodes, suggestProvider);
    }
}
