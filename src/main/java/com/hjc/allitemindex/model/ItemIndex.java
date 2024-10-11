package com.hjc.allitemindex.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemIndex {
    public final Map<String, List<ItemInfo>> enIndex, cnIndex, pinyinIndex, pinyinabbrIndex;

    public ItemIndex() {
        this.enIndex = new HashMap<>();
        this.cnIndex = new HashMap<>();
        this.pinyinIndex = new HashMap<>();
        this.pinyinabbrIndex = new HashMap<>();
    }
}
