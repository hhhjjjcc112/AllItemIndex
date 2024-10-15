package com.hjc.allitemindex.model;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

public class ItemInfo {

    @SerializedName("en")
    public String englishName; // 英文名称(非官方?)
    @SerializedName("cn")
    public String chineseName; // 中文名称(非官方?)
    @SerializedName("alias")
    public Set<String> chineseAlias; // 中文别称(非官方)
//    @SerializedName("pinyin")
//    public List<String> pinYinFull; // 拼音全称
//    @SerializedName("pinyin_abbr")
//    public List<String> pinYinFAbbr; // 拼音缩写
    @SerializedName("carpet")
    public CarpetColor carpetColor; // 地面地毯颜色
    @SerializedName("direction")
    public Direction direction; // 方向
    @SerializedName("direction_color")
    public CarpetColor directionColor; // 对应方向的地毯颜色
    @SerializedName("light")
    public FloorLight floorLight; // 所在楼层地板的灯光

    @Override
    public String toString() {
        return "ItemInfo{" +
                "englishName='" + englishName + '\'' +
                ", chineseName=" + chineseName +
                ", ChineseAlias=" + chineseAlias +
                ", carpetColor=" + carpetColor +
                ", direction=" + direction +
                ", directionColor=" + directionColor +
                ", floorLight=" + floorLight +
                '}';
    }

    /**
     * 返回是否存在空值, 包含PinYin类型中的空值
     * @return 是否存在空值
     */
    public boolean anyEmpty() {
        return englishName == null || chineseName == null || chineseAlias == null || carpetColor == null || direction == null || directionColor == null || floorLight == null || anyNullInSet() || englishName.isBlank() || chineseName.isBlank();
    }

    private boolean anyNullInSet() {
        for(var alias: chineseAlias) {
            if(alias == null || alias.isBlank()) {
                return true;
            }
        }
        return false;
    }
}
