package com.pairzhu.mcdrcmdsuggest.core;

public interface SuggestProvider {
    String[] getSuggestions(String playerName, String command);
}
