package com.hjc.allitemindex.model;

import java.util.List;

public class ItemInfo {


    public String englishName; // 英文名称(非官方?)

    public String ChineseName; // 中文名称(非官方?)
    public List<String> ChineseAlias; // 中文别称(非官方)

    public List<String> pinYinFull; // 拼音全称
    public List<String> PinYinFAbbr; // 拼音缩写

    public String carpetColor; // 地面地毯颜色
    public String direction; // 方向
    public String directionColor; // 对应方向的地毯颜色
    public String floorLight; // 所在楼层地板的灯光
}
