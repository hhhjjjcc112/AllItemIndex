package com.hjc.allitemindex.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hjc.allitemindex.exception.*;
import com.hjc.allitemindex.model.*;
import com.hjc.allitemindex.util.ID;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class IndexJsonManager {

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
    private static final Set<ItemInfo> EMPTY_INFOS = Set.of();
    // 标识是否加载
    private static boolean loaded = false;

    // gson序列化与反序列化器
    private static final Gson gson = new GsonBuilder().setVersion(1.01).setPrettyPrinting().create();


    /**
     * 获取ItemIndexes对象
     * @param context 调用时的指令上下文
     * @return ItemIndexes对象
     */
    public static ItemIndexes getIndexesInstance(CommandContext<ServerCommandSource> context) {
        synchronized (IndexJsonManager.class) {
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
     * 获取ItemInfo对象
     * @param context 调用时的指令上下文
     * @return ItemInfo对象
     */
    public static Set<ItemInfo> getInfosInstance(CommandContext<ServerCommandSource> context) {
        synchronized (IndexJsonManager.class) {
            if(!loaded) {
                loaded = loadFromLocal(context);
            }
            if(loaded) {
                return infos;
            }
            return EMPTY_INFOS;
        }
    }

    /**
     * 重新加载index.json的内容
     * @param context 调用时的指令上下文
     * @return 重新加载是否成功
     */
    public static boolean reload(CommandContext<ServerCommandSource> context) {
        synchronized (IndexJsonManager.class) {
            loaded = loadFromLocal(context);
            return loaded;
        }
    }

    public static boolean addItem(
            CommandContext<ServerCommandSource> context,
            ItemInfo info
    ) throws BadHanyuPinyinOutputFormatCombination {
        synchronized (IndexJsonManager.class) {
            if(!loaded) {
                loaded = loadFromLocal(context);
            }
            if(loaded && infos.add(info)) {
                indexes = ItemIndexes.from(infos);
                return saveToLocal(context);
            }
            return false;
        }
    }

    public static boolean removeItem(
            CommandContext<ServerCommandSource> context,
            ItemInfo info
    ) throws BadHanyuPinyinOutputFormatCombination {
        synchronized (IndexJsonManager.class) {
            if(!loaded) {
                loaded = loadFromLocal(context);
            }
            if(loaded && infos.remove(info)) {
                indexes = ItemIndexes.from(infos);
                return saveToLocal(context);
            }
            return false;
        }
    }

    public static boolean removeItems(
            CommandContext<ServerCommandSource> context,
            Collection<ItemInfo> infoSet
    ) throws BadHanyuPinyinOutputFormatCombination {
        synchronized (IndexJsonManager.class) {
            if(!loaded) {
                loaded = loadFromLocal(context);
            }
            // 需要检查被删除的元素是否都存在
            if(loaded && infos.containsAll(infoSet) && infos.removeAll(infoSet)) {
                indexes = ItemIndexes.from(infos);
                return saveToLocal(context);
            }
            return false;
        }
    }

    public static boolean addAlias(
            CommandContext<ServerCommandSource> context,
            String alias,
            String chineseName
    ) throws BadHanyuPinyinOutputFormatCombination {
        synchronized (IndexJsonManager.class) {
            if(!loaded) {
                loaded = loadFromLocal(context);
            }
            if(loaded) {
                for(var info: infos) {
                    if(info.chineseName.equals(chineseName)) {
                        info.chineseAlias.add(alias);
                    }
                }
                indexes = ItemIndexes.from(infos);
                return saveToLocal(context);
            }
            return false;
        }
    }

    public static boolean removeAlias(
            CommandContext<ServerCommandSource> context,
            String alias,
            String chineseName
    ) throws BadHanyuPinyinOutputFormatCombination {
        synchronized (IndexJsonManager.class) {
            if(!loaded) {
                loaded = loadFromLocal(context);
            }
            if(loaded) {
                for(var info: infos) {
                    if(info.chineseName.equals(chineseName)) {
                        info.chineseAlias.remove(alias);
                    }
                }
                indexes = ItemIndexes.from(infos);
                return saveToLocal(context);
            }
            return false;
        }
    }

    public static boolean moveItem(
            CommandContext<ServerCommandSource> context,
            ItemInfo item,
            FloorLight toFloor,
            Direction toDirection,
            CarpetColor toCarpetColor
    ) throws BadHanyuPinyinOutputFormatCombination {
        synchronized (IndexJsonManager.class) {
            if(!loaded) {
                loaded = loadFromLocal(context);
            }
            if(loaded) {
                item.floorLight = toFloor;
                item.direction = toDirection;
                item.directionColor = Direction.correspondingColors.get(toDirection);
                item.carpetColor = toCarpetColor;
                indexes = ItemIndexes.from(infos);
                return saveToLocal(context);
            }
            return false;
        }
    }

    public static boolean modifyEnglishName(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            String newEnglishName
    ) throws BadHanyuPinyinOutputFormatCombination {
        synchronized (IndexJsonManager.class) {
            if(!loaded) {
                loaded = loadFromLocal(context);
            }
            if(loaded) {
                for(var info: infos) {
                    if(info.chineseName.equals(chineseName)) {
                        info.englishName = newEnglishName;
                    }
                }
                indexes = ItemIndexes.from(infos);
                return saveToLocal(context);
            }
            return false;
        }
    }

    public static boolean modifyAlias(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            String oldAlias,
            String newAlias
    ) throws BadHanyuPinyinOutputFormatCombination {
        synchronized (IndexJsonManager.class) {
            if(!loaded) {
                loaded = loadFromLocal(context);
            }
            if(loaded) {
                for(var info: infos) {
                    if(info.chineseName.equals(chineseName)) {
                        info.chineseAlias.remove(oldAlias);
                        info.chineseAlias.add(newAlias);
                    }
                }
                indexes = ItemIndexes.from(infos);
                return saveToLocal(context);
            }
            return false;
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
            ID.reset(); // 重置全局ID
            infos = gson.fromJson(content, new TypeToken<Set<ItemInfo>>() {}.getType());
            // 我真是服了为啥gson不会抛出异常, 而是返回null啊
            if(infos == null) {
                MyExceptionHandler.error(context, new NullPointerException("index.json加载失败"), "index.json文件加载失败");
                return false;
            }
            checkInfos(infos);
            indexes = ItemIndexes.from(infos);
            return true;
        } catch (EmptyValueException e) {
            MyExceptionHandler.error(context, e, "index.json包含空值");
        } catch (ConflictIdException e) {
            MyExceptionHandler.error(context, e, "ID冲突");
        } catch(CarpetAndDirectionNotMatchException e) {
            MyExceptionHandler.error(context, e, "方向和地毯颜色不对应");
        } catch (AmbiguousValuesException e) {
            MyExceptionHandler.error(context, e, "别名不一致");
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "拼音计算失败");
        } catch (JsonSyntaxException e) {
            MyExceptionHandler.error(context, e, "Json解析失败");
        } catch (IOException e) {
            MyExceptionHandler.error(context, e, "index.json文件加载失败");
        }
        return false;
    }

    /**
     * 保存infos到本地
     * @param context 调用时的指令上下文
     * @return 保存是否成功
     */
    private static boolean saveToLocal(CommandContext<ServerCommandSource> context) {
        try {
            String content = gson.toJson(infos);
            Files.writeString(indexFile, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            MyExceptionHandler.error(context, e, "index.json文件保存失败");
        }
        return false;
    }

    /**
     * 检查加载的infos是否合法, 抛出错误
     */
    private static void checkInfos(Set<ItemInfo> infos) throws EmptyValueException, CarpetAndDirectionNotMatchException, AmbiguousValuesException {
        for(var info : infos) {
            // 存在可能的空值
            if(info.anyEmpty()) {
                throw new EmptyValueException(info);
            }
            // 方向和羊毛颜色是否对应(不是, 既然一定对应的话为啥俩都要啊)
            if(Direction.correspondingColors.get(info.direction) != info.directionColor) {
                throw new CarpetAndDirectionNotMatchException("单片" + info.chineseName + "的方向和对应地毯颜色不对应, 地毯颜色应为" + Direction.correspondingColors.get(info.direction) + ", 实际为" + info.directionColor);
            }
        }
        var set = new HashSet<>(infos);
        while (!set.isEmpty()) {
            var first = set.iterator().next();
            var items = set.stream().filter(info -> first.chineseName.equals(info.chineseName))
                    .collect(Collectors.toSet());
            for(var item: items) {
                if(!first.chineseAlias.equals(item.chineseAlias)) {
                    throw new AmbiguousValuesException(first.chineseName + "的别名不一致, 分别有" + first.chineseAlias + "和" + item.chineseAlias);
                }
                else if(!first.englishName.equals(item.englishName)) {
                    throw new AmbiguousValuesException(first.chineseName + "的英文名不一致, 分别有" + first.englishName + "和" + item.englishName);
                }
                set.remove(item);
            }
        }
    }

    /**
     * 加载该类，执行static语句块创建文件夹
     */
    public static void initialize() {}
}
