package com.hjc.allitemindex.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ItemInfo {

    @SerializedName("en")
    public String englishName; // 英文名称(非官方?)
    @SerializedName("cn")
    public String ChineseName; // 中文名称(非官方?)
    @SerializedName("alias")
    public List<String> ChineseAlias; // 中文别称(非官方)
    public List<String> pinYinFull; // 拼音全称
    public List<String> PinYinFAbbr; // 拼音缩写
    @SerializedName("")
    public String carpetColor; // 地面地毯颜色
    @SerializedName("")
    public String direction; // 方向
    @SerializedName("")
    public String directionColor; // 对应方向的地毯颜色
    @SerializedName("")
    public String floorLight; // 所在楼层地板的灯光
}
