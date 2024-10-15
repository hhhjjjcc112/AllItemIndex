package com.hjc.allitemindex.model;

import com.hjc.allitemindex.util.PinYin;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.*;

public class ItemIndexes {
    // map的value一定不能为null
    public final Map<String, Set<ItemInfo>> enIndex, cnIndex, pinyinIndex, pinyinAbbrIndex;
    // 仅仅是为了作为输入补全而加上前后引号
    public final Set<String> cnKeys;
    // 全部index
    public final Map<String, Set<ItemInfo>> allIndex;

    public ItemIndexes() {
        this.enIndex = new HashMap<>();
        this.cnIndex = new HashMap<>();
        this.pinyinIndex = new HashMap<>();
        this.pinyinAbbrIndex = new HashMap<>();
        this.cnKeys = new LinkedHashSet<>();
        this.allIndex = new HashMap<>();
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
        insertOrCreate(allIndex, enKey, info);
        // 获取并插入中文名称对应的中文, 拼音全称和拼音缩写
        String cnKey = info.chineseName;
        insertOrCreate(cnIndex, cnKey, info);
        insertOrCreate(allIndex, cnKey, info);
        // 拼音
        insertAllOrCreate(pinyinIndex, PinYin.toPinYinSet(cnKey), info);
        insertAllOrCreate(allIndex, PinYin.toPinYinSet(cnKey), info);
        // 拼音缩写
        insertAllOrCreate(pinyinAbbrIndex, PinYin.toPinYinAbbrSet(cnKey), info);
        insertAllOrCreate(allIndex, PinYin.toPinYinAbbrSet(cnKey), info);
        // 获取并插入中文别名对应的中文, 拼音全称和拼音缩写
        Set<String> cnAliases = info.chineseAlias;
        for(String cnAlias : cnAliases) {
            insertOrCreate(cnIndex, cnAlias, info);

            insertAllOrCreate(pinyinIndex, PinYin.toPinYinSet(cnAlias), info);
            insertAllOrCreate(allIndex, PinYin.toPinYinSet(cnAlias), info);

            insertAllOrCreate(pinyinAbbrIndex, PinYin.toPinYinAbbrSet(cnAlias), info);
            insertAllOrCreate(allIndex, PinYin.toPinYinAbbrSet(cnAlias), info);
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
       for(K key: keys) {
           insertOrCreate(map, key, value);
       }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemIndexes that)) return false;
        return Objects.equals(enIndex, that.enIndex) && Objects.equals(cnIndex, that.cnIndex) && Objects.equals(pinyinIndex, that.pinyinIndex) && Objects.equals(pinyinAbbrIndex, that.pinyinAbbrIndex) && Objects.equals(cnKeys, that.cnKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enIndex, cnIndex, pinyinIndex, pinyinAbbrIndex, cnKeys);
    }

    @Override
    public String toString() {
        return "ItemIndexes{" +
                "enIndex=" + enIndex +
                ", cnIndex=" + cnIndex +
                ", pinyinIndex=" + pinyinIndex +
                ", pinyinAbbrIndex=" + pinyinAbbrIndex +
                ", cnKeys=" + cnKeys +
                '}';
    }
}
