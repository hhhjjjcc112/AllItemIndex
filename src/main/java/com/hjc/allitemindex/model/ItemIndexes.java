package com.hjc.allitemindex.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemIndexes {
    public final Map<String, List<ItemInfo>> enIndex, cnIndex, pinyinIndex, pinyinabbrIndex;

    public ItemIndexes() {
        this.enIndex = new HashMap<>();
        this.cnIndex = new HashMap<>();
        this.pinyinIndex = new HashMap<>();
        this.pinyinabbrIndex = new HashMap<>();
    }
}
