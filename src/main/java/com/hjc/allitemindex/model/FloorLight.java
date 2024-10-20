package com.hjc.allitemindex.model;

import com.google.gson.annotations.SerializedName;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Style;

public enum FloorLight {
    @SerializedName("海晶灯")
    SEA_LANTERN(Items.SEA_LANTERN, 0x6CA799, "海晶灯"),
    @SerializedName("珠光蛙明灯")
    PEARLESCENT_FROGLIGHT(Items.PEARLESCENT_FROGLIGHT, 0xC2A4C3, "珠光蛙明灯"),
    @SerializedName("青翠蛙明灯")
    VERDANT_FROGLIGHT(Items.VERDANT_FROGLIGHT, 0x93C584, "青翠蛙明灯"),
    @SerializedName("赭黄蛙明灯")
    OCHRE_FROGLIGHT(Items.OCHRE_FROGLIGHT, 0xF7DF92, "赭黄蛙明灯"),
    @SerializedName("菌光体")
    SHROOMLIGHT(Items.SHROOMLIGHT, 0xD34808, "菌光体"),;


    public final Item item;
    public final Style colorStyle;
    public final String cn;

    FloorLight(Item item, int rgb, String cnName) {
        this.item = item;
        this.colorStyle = Style.EMPTY.withColor(rgb);
        this.cn = cnName;
    }

    @Override
    public String toString() {
        return cn;
    }
}
