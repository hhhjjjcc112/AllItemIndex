package com.hjc.allitemindex.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hjc.allitemindex.model.ItemIndexes;
import com.hjc.allitemindex.model.ItemInfo;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IndexJsonLoader {

    private static final Path root = FabricLoader.getInstance().getConfigDir().resolve("allitemindex");
    static {
        if (!Files.exists(root)) {
            try {
                Files.createDirectories(root);
            } catch (IOException e) {
                System.err.println("Failed to create directory " + root);
            }
        }
    }
    private static final Path indexFile = root.resolve("index.json");
    // 标识是否加载
    private static final ItemIndexes infos = new ItemIndexes();
    private static boolean loaded = false;

    // gson序列化与反序列化器
    private static final Gson gson = new GsonBuilder().serializeNulls().create();


    /**
     * 获取ItemIndexes对象
     * @param context 调用时的指令上下文
     * @return ItemIndexes对象
     */
    public static ItemIndexes getIndexesInstance(CommandContext<ServerCommandSource> context) {
        synchronized (IndexJsonLoader.class) {
            if(!loaded) {
                loaded = loadFromLocal();
                alertIfFailed(context);
            }
            return infos;
        }
    }

    /**
     * 重新加载index.json的内容
     * @param context 调用时的指令上下文
     * @return 重新加载是否成功
     */
    public static boolean reload(CommandContext<ServerCommandSource> context) {
        synchronized (IndexJsonLoader.class) {
            loaded = loadFromLocal();
            alertIfFailed(context);
            return loaded;
        }
    }

    private static void alertIfFailed(CommandContext<ServerCommandSource> context) {
        if(!loaded) {
            ServerCommandSource source = context.getSource();
            var server = source.getServer();
            server.sendMessage(Text.literal("failed to load index.json. Please check if the file exists."));
            System.err.println("Failed to load index.json. Please check if the file exists.");
        }
    }

    /**
     * 从本地加载index.json中的内容
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
