package com.pairzhu.mcdrcmdsuggest.core;

import com.alibaba.fastjson2.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CommandTreeNode {
    public final String name;
    public final String type;
    public final boolean suggestible;
    public final List<CommandTreeNode> children = new ArrayList<>();

    public CommandTreeNode(JSONObject json) {
        this.name = json.getString("name");
        this.type = json.getString("type");
        this.suggestible = json.getBooleanValue("suggestible");
        if (json.containsKey("children")) {
            for (JSONObject childJson : json.getJSONArray("children").toArray(JSONObject.class)) {
                this.children.add(new CommandTreeNode(childJson));
            }
        }
    }

    public boolean needsSuggestion() {
        return !"LITERAL".equals(type) && suggestible;
    }
}
