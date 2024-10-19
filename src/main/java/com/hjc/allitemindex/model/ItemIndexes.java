package com.hjc.allitemindex.model;

import com.hjc.allitemindex.exception.ConflictIdException;
import com.hjc.allitemindex.util.PinYin;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ItemIndexes {
    // 每个map对应一种语言的索引
    public final Map<String, Set<ItemInfo>> enIndex, cnIndex, pinyinIndex, pinyinAbbrIndex, noneIndex;
    // 两个主键索引
    public final Map<String, Set<ItemInfo>> chineseIndex;
    public final Map<Long, ItemInfo> idIndex;

    public ItemIndexes() {
        this.enIndex = new HashMap<>();
        this.cnIndex = new HashMap<>();
        this.pinyinIndex = new HashMap<>();
        this.pinyinAbbrIndex = new HashMap<>();
        this.noneIndex = new HashMap<>();
        this.chineseIndex = new HashMap<>();
        this.idIndex = new HashMap<>();
    }

    public static @NotNull ItemIndexes from(@NotNull Set<ItemInfo> infos) throws BadHanyuPinyinOutputFormatCombination, ConflictIdException {
        ItemIndexes indexes = new ItemIndexes();
        for (ItemInfo info : infos) {
            indexes.add(info);
        }
        return indexes;
    }

    private void add(@NotNull ItemInfo info) throws BadHanyuPinyinOutputFormatCombination, ConflictIdException {
        // 主键1
        if(idIndex.containsKey(info.id.value)) {
            throw new ConflictIdException(info, idIndex.get(info.id.value));
        }
        idIndex.put(info.id.value, info);
        // 英文
        String enKey = info.englishName;
        insertOrCreate(enIndex, enKey, info);
        insertOrCreate(noneIndex, enKey, info);
        // 获取并插入中文名称对应的中文, 拼音全称和拼音缩写
        String cnKey = info.chineseName;
        addCnKey(info, cnKey);
        // 主键2
        insertOrCreate(chineseIndex, cnKey, info);
        // 获取并插入中文别名对应的中文, 拼音全称和拼音缩写
        Set<String> cnAliases = info.chineseAlias;
        for(String cnAlias : cnAliases) {
            addCnKey(info, cnAlias);
        }
    }

    private void addCnKey(@NotNull ItemInfo info, String cn) throws BadHanyuPinyinOutputFormatCombination {
        insertOrCreate(cnIndex, cn, info);
        insertOrCreate(noneIndex, cn, info);

        Set<String> aliasPinyinSet = PinYin.toPinYinSet(cn);
        insertAllOrCreate(pinyinIndex, aliasPinyinSet, info);
        insertAllOrCreate(noneIndex, aliasPinyinSet, info);

        Set<String> aliasPinyinAbbrSet = PinYin.toPinYinAbbrSet(cn);
        insertAllOrCreate(pinyinAbbrIndex, aliasPinyinAbbrSet, info);
        insertAllOrCreate(noneIndex, aliasPinyinAbbrSet, info);
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
        return Objects.equals(enIndex, that.enIndex) && Objects.equals(cnIndex, that.cnIndex) && Objects.equals(pinyinIndex, that.pinyinIndex) && Objects.equals(pinyinAbbrIndex, that.pinyinAbbrIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enIndex, cnIndex, pinyinIndex, pinyinAbbrIndex);
    }

    @Override
    public String toString() {
        return "ItemIndexes{" +
                "enIndex=" + enIndex +
                ", cnIndex=" + cnIndex +
                ", pinyinIndex=" + pinyinIndex +
                ", pinyinAbbrIndex=" + pinyinAbbrIndex +
                '}';
    }
}
