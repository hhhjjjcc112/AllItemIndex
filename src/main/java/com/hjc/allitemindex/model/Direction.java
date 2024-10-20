package com.hjc.allitemindex.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public enum Direction {
    @SerializedName("南")
    SOUTH("direction.south", "南方"),
    @SerializedName("北")
    NORTH("direction.north", "北方"),
    @SerializedName("东")
    EAST("direction.east", "东方"),
    @SerializedName("西")
    WEST("direction.west", "西方");

    public static final Map<Direction, CarpetColor> correspondingColors = Map.of(
            NORTH, CarpetColor.WHITE,
            SOUTH, CarpetColor.GREEN,
            WEST, CarpetColor.BLUE,
            EAST, CarpetColor.RED
    );

    Direction(String key, String cn) {
        translationKey = key;
        this.cn = cn;
    }
    public final String translationKey;
    public final String cn;

    @Override
    public String toString() {
        return cn;
    }
}
