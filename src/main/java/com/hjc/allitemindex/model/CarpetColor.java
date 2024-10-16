package com.hjc.allitemindex.model;

import com.google.gson.annotations.SerializedName;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Style;

public enum CarpetColor {
    @SerializedName("白") WHITE(Items.WHITE_CARPET, 0xffffff, "白色"),
    @SerializedName("橙") ORANGE(Items.ORANGE_CARPET, 0xff681f, "橙色"),
    @SerializedName("品红") MAGENTA(Items.MAGENTA_CARPET, 0xff00ff, "品红"),
    @SerializedName("淡蓝") LIGHT_BLUE(Items.LIGHT_BLUE_CARPET, 0x9ac0cd, "淡蓝"),
    @SerializedName("黄") YELLOW(Items.YELLOW_CARPET, 0xffff00, "黄色"),
    @SerializedName("黄绿") LIME(Items.LIME_CARPET, 0xbfff00, "黄绿"),
    @SerializedName("粉") PINK(Items.PINK_CARPET, 0xff69b4, "粉色"),
    @SerializedName("灰") GRAY(Items.GRAY_CARPET, 0x808080, "灰色"),
    @SerializedName("淡灰") LIGHT_GRAY(Items.LIGHT_GRAY_CARPET, 0xd3d3d3, "淡灰"),
    @SerializedName("青") CYAN(Items.CYAN_CARPET, 0x00ffff, "青色"),
    @SerializedName("紫") PURPLE(Items.PURPLE_CARPET, 0xa020f0, "紫色"),
    @SerializedName("蓝") BLUE(Items.BLUE_CARPET, 0x0000ff, "蓝色"),
    @SerializedName("棕") BROWN(Items.BROWN_CARPET, 0x8b4513, "棕色"),
    @SerializedName("绿") GREEN(Items.GREEN_CARPET, 0x00ff00, "绿色"),
    @SerializedName("红") RED(Items.RED_CARPET, 0xff0000, "红色"),
    @SerializedName("黑") BLACK(Items.BLACK_CARPET, 0x000000, "黑色");

    CarpetColor(Item item, int rgb, String cn) {
        this.item = item;
        this.colorStyle = Style.EMPTY.withColor(rgb);
        this.cn = cn;
    }

    public final Item item;
    public final Style colorStyle;
    public final String cn;
}
