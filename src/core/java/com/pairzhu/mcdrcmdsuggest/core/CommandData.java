package com.pairzhu.mcdrcmdsuggest.core;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import java.util.List;

public class CommandData {
    @JSONField(name = "mode", required = true)
    public final SuggestMode mode;

    @JSONField(name = "host")
    public final String host;

    @JSONField(name = "port")
    public final int port;

    @JSONField(name = "nodes")
    public final List<JSONObject> commandNodes;

    public CommandData(SuggestMode mode, String host, int port, List<JSONObject> commandNodes) {
        this.mode = mode;
        this.host = host == null ? "localhost" : host;
        this.port = port;
        this.commandNodes = commandNodes;
    }
}
