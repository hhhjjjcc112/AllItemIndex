package com.hjc.allitemindex.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.Set;

public enum Direction {
    @SerializedName("南")
    SOUTH("direction.south"),
    @SerializedName("北")
    NORTH("direction.north"),
    @SerializedName("东")
    EAST("direction.east"),
    @SerializedName("西")
    WEST("direction.west");

    Direction(String key) {
        translationKey = key;
    }
    public final String translationKey;

    public static void main(String[] args) {
        Gson gson = new GsonBuilder().create();
        System.out.println(Direction.SOUTH.name());
        System.out.println(gson.toJson(Direction.SOUTH));

        var json = "[\"111\", \"111\"]";
        var set1 = gson.fromJson(json, Set.class);
        System.out.println(set1.size());
    }
}
