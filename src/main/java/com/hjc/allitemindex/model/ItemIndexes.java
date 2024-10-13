package com.hjc.allitemindex.model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ItemIndexes {
    // map的value一定不能为null
    public final Map<String, Set<ItemInfo>> enIndex, cnIndex, pinyinIndex, pinyinAbbrIndex;

    public ItemIndexes() {
        this.enIndex = new HashMap<>();
        this.cnIndex = new HashMap<>();
        this.pinyinIndex = new HashMap<>();
        this.pinyinAbbrIndex = new HashMap<>();
    }

    public static ItemIndexes from(Set<ItemInfo> infos) {
        ItemIndexes indexes = new ItemIndexes();
        for (ItemInfo info : infos) {
            indexes.add(info);
        }
        return indexes;
    }

    public void add(ItemInfo info) {
        String enKey = info.englishName;
        insertOrCreate(enIndex, enKey, info);
        // 获取并插入中文名称对应的中文, 拼音全称和拼音缩写
        PinYin cnKey = info.chineseName;
        insertOrCreate(cnIndex, cnKey.chineseName, info);
        insertAllOrCreate(pinyinIndex, cnKey.pinYin(), info);
        insertAllOrCreate(pinyinAbbrIndex, cnKey.pinYinAbbr(), info);
        // 获取并插入中文别名对应的中文, 拼音全称和拼音缩写
        Set<PinYin> cnAliases = info.ChineseAlias;
        for(PinYin cnAlias : cnAliases) {
            insertOrCreate(cnIndex, cnAlias.chineseName, info);
            insertAllOrCreate(pinyinIndex, cnAlias.pinYin(), info);
            insertAllOrCreate(pinyinAbbrIndex, cnAlias.pinYinAbbr(), info);
        }
    }


    private <K, V> void insertOrCreate(Map<K, Set<V>> map, K key, V value) {
        Set<V> set = map.get(key);
        if(set == null) {
            Set<V> set1 = new LinkedHashSet<>();
            set1.add(value);
            map.put(key, set1);
        }
        else {
            set.add(value);
        }
    }

    private <K, V> void insertAllOrCreate(Map<K, Set<V>> map, Set<K> keys, V value) {
        for (K key : keys) {
            insertOrCreate(map, key, value);
        }
    }
}
