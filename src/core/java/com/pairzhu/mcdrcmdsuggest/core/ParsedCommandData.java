package com.pairzhu.mcdrcmdsuggest.core;

import java.util.List;

public class ParsedCommandData {
    public final SuggestMode mode;
    public final int port;
    public final List<CommandTreeNode> nodes;
    public final SuggestProvider suggestProvider;

    public ParsedCommandData(SuggestMode mode, int port, List<CommandTreeNode> nodes,
            SuggestProvider suggestProvider) {
        this.mode = mode;
        this.port = port;
        this.nodes = nodes;
        this.suggestProvider = suggestProvider;
    }
}
