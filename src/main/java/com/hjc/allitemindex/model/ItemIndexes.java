package com.hjc.allitemindex.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemIndexes {
    // map的value一定不能为null
    public final Map<String, List<ItemInfo>> enIndex, cnIndex, pinyinIndex, pinyinAbbrIndex;

    public ItemIndexes() {
        this.enIndex = new HashMap<>();
        this.cnIndex = new HashMap<>();
        this.pinyinIndex = new HashMap<>();
        this.pinyinAbbrIndex = new HashMap<>();
    }

    public void clearAll() {
        this.enIndex.clear();
        this.cnIndex.clear();
        this.pinyinIndex.clear();
        this.pinyinAbbrIndex.clear();
    }

    public void add(ItemInfo info) {
        String enKey = info.englishName;
        insertOrCreate(enIndex, enKey, info);
        String cnKey = info.ChineseName;
        insertOrCreate(cnIndex, cnKey, info);
        List<String> cnKeys = info.ChineseAlias;
        for (String key : cnKeys) {
            insertOrCreate(cnIndex, key, info);
        }
        List<String> pinyinKeys = info.pinYinFull;
        for (String key : pinyinKeys) {
            insertOrCreate(pinyinIndex, key, info);
        }
        List<String> pinyinAbbrKeys = info.pinYinFAbbr;
        for (String key : pinyinAbbrKeys) {
            insertOrCreate(pinyinAbbrIndex, key, info);
        }
    }

    private <K, V> void insertOrCreate(Map<K, List<V>> map, K key, V value) {
        List<V> list = map.get(key);
        if(list == null) {
            List<V> list1 = new ArrayList<>();
            list1.add(value);
            map.put(key, list1);
        }
        else {
            list.add(value);
        }
    }
}
