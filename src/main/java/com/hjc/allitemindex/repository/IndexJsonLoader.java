package com.hjc.allitemindex.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hjc.allitemindex.exception.MyExceptionHandler;
import com.hjc.allitemindex.model.CarpetColor;
import com.hjc.allitemindex.model.Direction;
import com.hjc.allitemindex.model.ItemIndexes;
import com.hjc.allitemindex.model.ItemInfo;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IndexJsonLoader {

    private static final Path root = FabricLoader.getInstance().getConfigDir().resolve("allitemindex");
    static {
        if (!Files.exists(root)) {
            try {
                Files.createDirectories(root);
            } catch (IOException e) {
                System.err.println("创建文件夹失败 " + root);
            }
        }
    }
    private static final Path indexFile = root.resolve("index.json");
    private static Set<ItemInfo> infos = null;
    private static ItemIndexes indexes = null;
    private static final ItemIndexes EMPTY_INDEXES = new ItemIndexes();
    // 标识是否加载
    private static boolean loaded = false;

    // gson序列化与反序列化器
    private static final Gson gson = new GsonBuilder().create();


    /**
     * 获取ItemIndexes对象
     * @param context 调用时的指令上下文
     * @return ItemIndexes对象
     */
    public static ItemIndexes getIndexesInstance(CommandContext<ServerCommandSource> context) {
        synchronized (IndexJsonLoader.class) {
            if(!loaded) {
                loaded = loadFromLocal(context);
            }
            if(loaded) {
                return indexes;
            }
            return EMPTY_INDEXES;
        }
    }

    /**
     * 重新加载index.json的内容
     * @param context 调用时的指令上下文
     * @return 重新加载是否成功
     */
    public static boolean reload(CommandContext<ServerCommandSource> context) {
        synchronized (IndexJsonLoader.class) {
            loaded = loadFromLocal(context);
            return loaded;
        }
    }

    /**
     * 从本地加载index.json中的内容
     * @param context 调用时的指令上下文
     * @return 加载是否成功
     */
    private static boolean loadFromLocal(CommandContext<ServerCommandSource> context) {
        try {
            String content = Files.readString(indexFile, StandardCharsets.UTF_8);
            infos = gson.fromJson(content, new TypeToken<Set<ItemInfo>>() {
            }.getType());
            checkInfos(infos);
            indexes = ItemIndexes.from(infos);
            return true;
        } catch (NullPointerException e) {
            MyExceptionHandler.error(context, e, "index.json包含空值");
        } catch(IllegalArgumentException e) {
            MyExceptionHandler.error(context, e, "方向和地毯颜色不对应");
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "拼音计算失败");
        } catch (JsonSyntaxException e) {
            MyExceptionHandler.error(context, e, "Json解析失败");
        } catch (IOException e) {
            MyExceptionHandler.error(context, e, "index.json文件加载失败");
        }
        return false;
    }

    private static Map<Direction, CarpetColor> correspondingColors = new HashMap<>();
    static {
        correspondingColors.put(Direction.NORTH, CarpetColor.WHITE);
        correspondingColors.put(Direction.SOUTH, CarpetColor.GREEN);
        correspondingColors.put(Direction.WEST, CarpetColor.BLUE);
        correspondingColors.put(Direction.EAST, CarpetColor.RED);
        // 设置不可变
        correspondingColors = Collections.unmodifiableMap(correspondingColors);
    }

    /**
     * 检查加载的infos是否合法, 抛出错误
     */
    private static void checkInfos(Set<ItemInfo> infos) throws NullPointerException, IllegalArgumentException {
        for(var info : infos) {
            // 存在可能的空值
            if(info.anyEmpty()) {
                throw new NullPointerException("itemInfo " + info + " 包含空值");
            }
            // 方向和羊毛颜色是否对应(不是, 既然一定对应的话为啥俩都要啊)
            if(correspondingColors.get(info.direction) != info.directionColor) {
                throw new IllegalArgumentException("单片" + info.chineseName + "的方向和对应地毯颜色不对应, 地毯颜色应为" + correspondingColors.get(info.direction) + ", 实际为" + info.directionColor);
            }
        }
    }

    /**
     * 加载该类，执行static语句块创建文件夹
     */
    public static void initialize() {}
}
