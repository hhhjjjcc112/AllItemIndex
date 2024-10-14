package com.hjc.allitemindex.model;

import com.google.gson.annotations.SerializedName;

public enum Direction {
    @SerializedName("南")
    SOUTH("direction.south", "南方"),
    @SerializedName("北")
    NORTH("direction.north", "北方"),
    @SerializedName("东")
    EAST("direction.east", "东方"),
    @SerializedName("西")
    WEST("direction.west", "西方");

    Direction(String key, String cn) {
        translationKey = key;
        this.cn = cn;
    }
    public final String translationKey;
    public final String cn;


}
