package com.hjc.allitemindex.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hjc.allitemindex.model.ItemInfo;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IndexJsonLoader {

    private static final Path root = FabricLoader.getInstance().getConfigDir().resolve("allitemindex");
    private static final Path indexFile = root.resolve("index.json");
    // 标识是否加载
    private static final ItemInfo infos = new ItemInfo();
    private static boolean loaded = false;

    // gson序列化与反序列化器
    private static final Gson gson = new GsonBuilder().serializeNulls().create();


    public static ItemInfo getInfos() {
        synchronized (IndexJsonLoader.class) {
            if(!loaded) {
                loaded = loadFromLocal();
            }
            return infos;
        }
    }

    public static boolean reload() {
        synchronized (IndexJsonLoader.class) {
            loaded = loadFromLocal();
            return loaded;
        }
    }

    /**
     *
     * @return 加载是否成功
     */
    public static boolean loadFromLocal() {
        try {
            String content = Files.readString(indexFile, StandardCharsets.UTF_8);
            List<ItemInfo> list = gson.fromJson(content, new TypeToken<List<ItemInfo>>() {}.getType());
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
