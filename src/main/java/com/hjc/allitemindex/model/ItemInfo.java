package com.hjc.allitemindex.model;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

public class ItemInfo {

    @SerializedName("en")
    public String englishName; // 英文名称(非官方?)
    @SerializedName("cn")
    public PinYin chineseName; // 中文名称(非官方?)
    @SerializedName("alias")
    public Set<PinYin> ChineseAlias; // 中文别称(非官方)
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
}
