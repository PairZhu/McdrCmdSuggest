package com.pairzhu.mcdrcmdsuggest.core;

import com.alibaba.fastjson2.annotation.JSONField;

public enum SuggestMode {
    @JSONField(name = "stdio")
    STDIO,

    @JSONField(name = "http")
    HTTP
}
