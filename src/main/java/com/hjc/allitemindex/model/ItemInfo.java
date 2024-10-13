package com.hjc.allitemindex.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class ItemInfo {

    @SerializedName("en")
    public String englishName; // 英文名称(非官方?)
    @SerializedName("cn")
    public String ChineseName; // 中文名称(非官方?)
    @SerializedName("alias")
    public List<String> ChineseAlias; // 中文别称(非官方)
    @SerializedName("pinyin")
    public List<String> pinYinFull; // 拼音全称
    @SerializedName("pinyin_abbr")
    public List<String> pinYinFAbbr; // 拼音缩写
    @SerializedName("carpet")
    public CarpetColor carpetColor; // 地面地毯颜色
    @SerializedName("direction")
    public Direction direction; // 方向
    @SerializedName("direction_color")
    public CarpetColor directionColor; // 对应方向的地毯颜色
    @SerializedName("light")
    public FloorLight floorLight; // 所在楼层地板的灯光

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemInfo itemInfo)) return false;
        return Objects.equals(englishName, itemInfo.englishName) && Objects.equals(ChineseName, itemInfo.ChineseName) && Objects.equals(ChineseAlias, itemInfo.ChineseAlias) && Objects.equals(pinYinFull, itemInfo.pinYinFull) && Objects.equals(pinYinFAbbr, itemInfo.pinYinFAbbr) && Objects.equals(carpetColor, itemInfo.carpetColor) && Objects.equals(direction, itemInfo.direction) && Objects.equals(directionColor, itemInfo.directionColor) && Objects.equals(floorLight, itemInfo.floorLight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(englishName, ChineseName, ChineseAlias, pinYinFull, pinYinFAbbr, carpetColor, direction, directionColor, floorLight);
    }
}
