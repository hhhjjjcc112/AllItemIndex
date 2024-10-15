package com.hjc.allitemindex.model;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

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

    public static ItemIndexes from(Set<ItemInfo> infos) throws BadHanyuPinyinOutputFormatCombination {
        ItemIndexes indexes = new ItemIndexes();
        for (ItemInfo info : infos) {
            indexes.add(info);
        }
        for(var key: indexes.cnIndex.keySet()) {
            indexes.cnKeys.add(String.format("\"%s\"", key));
        }
        return indexes;
    }

    private void add(ItemInfo info) throws BadHanyuPinyinOutputFormatCombination {
        String enKey = info.englishName;
        insertOrCreate(enIndex, enKey, info);
        // 获取并插入中文名称对应的中文, 拼音全称和拼音缩写
        String cnKey = info.chineseName;
        insertOrCreate(cnIndex, cnKey, info);
        for(var py: PinYin.toPinYinSet(cnKey)) {
            insertOrCreate(pinyinIndex, py, info);
        }
        for(var pyAbbr: PinYin.toPinYinAbbrSet(cnKey)) {
            insertOrCreate(pinyinAbbrIndex, pyAbbr, info);
        }
        // 获取并插入中文别名对应的中文, 拼音全称和拼音缩写
        Set<String> cnAliases = info.chineseAlias;
        for(String cnAlias : cnAliases) {
            insertOrCreate(cnIndex, cnAlias, info);
            for(var py: PinYin.toPinYinSet(cnAlias)) {
                insertOrCreate(pinyinIndex, py, info);
            }
            for(var pyAbbr: PinYin.toPinYinAbbrSet(cnAlias)) {
                insertOrCreate(pinyinAbbrIndex, pyAbbr, info);
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
