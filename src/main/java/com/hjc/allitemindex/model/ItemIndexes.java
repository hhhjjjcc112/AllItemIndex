package com.hjc.allitemindex.model;

import com.hjc.allitemindex.exception.PinYinNotMatchException;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ItemIndexes {
    // map的value一定不能为null
    public final Map<String, Set<ItemInfo>> enIndex, cnIndex, pinyinIndex, pinyinAbbrIndex;
    // 仅仅是为了作为输入补全而加上前后引号
    public final Set<String> cnKeys;

    public ItemIndexes() {
        this.enIndex = new HashMap<>();
        this.cnIndex = new HashMap<>();
        this.pinyinIndex = new HashMap<>();
        this.pinyinAbbrIndex = new HashMap<>();
        this.cnKeys = new LinkedHashSet<>();
    }

    public static ItemIndexes from(Set<ItemInfo> infos) throws PinYinNotMatchException {
        ItemIndexes indexes = new ItemIndexes();
        for (ItemInfo info : infos) {
            indexes.add(info);
        }
        for(var key: indexes.cnIndex.keySet()) {
            indexes.cnKeys.add(String.format("\"%s\"", key));
        }
        return indexes;
    }

    private void add(ItemInfo info) throws PinYinNotMatchException {
        String enKey = info.englishName;
        insertOrCreate(enIndex, enKey.toLowerCase(), info);
        // 获取并插入中文名称对应的中文, 拼音全称和拼音缩写
        PinYin cnKey = info.chineseName;
        insertOrCreate(cnIndex, cnKey.chineseName.toLowerCase(), info);
        for(var py: cnKey.pinYin()) {
            insertOrCreate(pinyinIndex, py.toLowerCase(), info);
        }
        for(var pyAbbr: cnKey.pinYinAbbr()) {
            insertOrCreate(pinyinAbbrIndex, pyAbbr.toLowerCase(), info);
        }
        // 获取并插入中文别名对应的中文, 拼音全称和拼音缩写
        Set<PinYin> cnAliases = info.chineseAlias;
        for(PinYin cnAlias : cnAliases) {
            insertOrCreate(cnIndex, cnAlias.chineseName, info);
            for(var py: cnAlias.pinYin()) {
                insertOrCreate(pinyinIndex, py.toLowerCase(), info);
            }
            for(var pyAbbr: cnAlias.pinYinAbbr()) {
                insertOrCreate(pinyinAbbrIndex, pyAbbr.toLowerCase(), info);
            }
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
}
