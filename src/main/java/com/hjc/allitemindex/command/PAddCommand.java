package com.hjc.allitemindex.command;

import com.hjc.allitemindex.exception.MyExceptionHandler;
import com.hjc.allitemindex.model.*;
import com.hjc.allitemindex.repository.IndexJsonManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PAddCommand {


    /**
     * 注册/padd /pa指令
     * @param dispatcher 用于注册、解析和执行命令
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var pAdd = CommandManager.literal("padd").requires(ctx -> ctx.hasPermissionLevel(2));
        var pAddAlias = CommandManager.literal("alias");
        pAddAlias.then(
            CommandManager.argument("alias", StringArgumentType.string())
                .then(CommandManager.literal("for")
                    .then(CommandManager.argument("chineseName", StringArgumentType.string())
                    .suggests(new ChineseNameSuggestionProvider())
                    .executes(context -> addAlias(
                        context,
                        StringArgumentType.getString(context, "alias"),
                        StringArgumentType.getString(context, "chineseName")
                    ))
                ))

        );
        var pAddItem = CommandManager.literal("item");
        var pAddItemNew = CommandManager.literal("new");
        var pAddItemExist = CommandManager.literal("exist");
        var pAddItemNewEn = CommandManager.argument("englishName", StringArgumentType.string());
        var pAddItemExistCn = CommandManager.argument("chineseName", StringArgumentType.string()).suggests(new ChineseNameSuggestionProvider());
        for(var floorLight: FloorLight.values()) {
            var existFloorLight = CommandManager.literal(floorLight.cn);
            var newFloorLight = CommandManager.literal(floorLight.cn);
            for(var direction: Direction.values()) {
                var existDirection = CommandManager.literal(direction.cn);
                var newDirection = CommandManager.literal(direction.cn);
                for(var carpetColor: CarpetColor.values()) {
                    var existCarpetColor = CommandManager.literal(carpetColor.cn)
                            .executes(context -> addItemExist(
                                context,
                                StringArgumentType.getString(context, "chineseName"),
                                floorLight,
                                direction,
                                carpetColor
                            ));
                    var newCarpetColor = CommandManager.literal(carpetColor.cn)
                            .executes(context -> addItemNew(
                                context,
                                StringArgumentType.getString(context, "chineseName"),
                                StringArgumentType.getString(context, "englishName"),
                                floorLight,
                                direction,
                                carpetColor
                            ));
                    newDirection.then(newCarpetColor);
                    existDirection.then(existCarpetColor);
                }
                newFloorLight.then(newDirection);
                existFloorLight.then(existDirection);
            }
            pAddItemNewEn.then(newFloorLight);
            pAddItemExistCn.then(existFloorLight);
        }
        pAddItemExist.then(pAddItemExistCn);
        pAddItemNew.then(CommandManager.argument("chineseName", StringArgumentType.string()).then(pAddItemNewEn));
        pAddItem.then(pAddItemNew).then(pAddItemExist);
        pAdd.then(pAddItem).then(pAddAlias);

        var command = dispatcher.register(pAdd);
        dispatcher.register(CommandManager.literal("pa").requires(ctx -> ctx.hasPermissionLevel(2)).redirect(command));
    }

    private static int addItemNew(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            String englishName,
            FloorLight floorLight,
            Direction direction,
            CarpetColor carpetColor
    ) {
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if (indexes.cnIndex.containsKey(chineseName)) {
            MyExceptionHandler.error(context, new IllegalArgumentException("中文名称已存在"), "中文名称已存在");
        }
        ItemInfo info = new ItemInfo();
        info.chineseName = chineseName;
        info.englishName = englishName;
        info.floorLight = floorLight;
        info.direction = direction;
        info.carpetColor = carpetColor;
        info.chineseAlias = new LinkedHashSet<>();
        info.directionColor = Direction.correspondingColors.get(direction);
        try {
            if(IndexJsonManager.addItem(context, info)) {
                ServerCommandSource source = context.getSource();
                // 唉, 懒得再搞翻译键了
                source.sendMessage(Text.of(String.format("成功添加新物品单片: %s", chineseName)));
            }
            else {
                MyExceptionHandler.error(context, new RuntimeException("添加新物品单片失败"), "添加新物品单片失败");
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "拼音转换失败");
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int addItemExist(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            FloorLight floorLight,
            Direction direction,
            CarpetColor carpetColor
    ) {
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if (!indexes.cnIndex.containsKey(chineseName)) {
            MyExceptionHandler.error(context, new IllegalArgumentException("中文名称不存在"), "中文名称不存在");
        }
        Set<ItemInfo> infos = indexes.cnIndex.get(chineseName);
        Iterator<ItemInfo> iterator = infos.iterator();
        // 可以保证iterator里一定有一个对象
        ItemInfo info = iterator.next();
        if(info.floorLight == floorLight && info.direction == direction && info.carpetColor == carpetColor) {
            MyExceptionHandler.error(context, new IllegalArgumentException("已存在相同的地板灯光、方向和地毯颜色"), "已存在相同的地板灯光、方向和地毯颜色");
        }
        ItemInfo newInfo = new ItemInfo();
        newInfo.chineseName = chineseName;
        newInfo.englishName = info.englishName;
        newInfo.floorLight = floorLight;
        newInfo.direction = direction;
        newInfo.carpetColor = carpetColor;
        newInfo.chineseAlias = info.chineseAlias;
        newInfo.directionColor = Direction.correspondingColors.get(direction);
        try {
            if(IndexJsonManager.addItem(context, newInfo)) {
                ServerCommandSource source = context.getSource();
                source.sendMessage(Text.of(String.format("添加物品单片成功: %s", chineseName)));
            }
            else {
                MyExceptionHandler.error(context, new RuntimeException("添加物品单片失败"), "添加物品单片失败");
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "拼音转换失败");
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int addAlias(
            CommandContext<ServerCommandSource> context,
            String alias,
            String chineseName
    ) {
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if (!indexes.cnIndex.containsKey(chineseName)) {
            MyExceptionHandler.error(context, new IllegalArgumentException("中文名称不存在"), "中文名称不存在");
        }
        Set<ItemInfo> infos = indexes.cnIndex.get(chineseName);
        Iterator<ItemInfo> iterator = infos.iterator();
        ItemInfo info = iterator.next();
        if(info.chineseAlias.contains(alias)) {
            MyExceptionHandler.error(context, new IllegalArgumentException("中文别称已存在"), "中文别称已存在");
        }
        try {
            if(IndexJsonManager.addAlias(context, alias, chineseName)) {
                ServerCommandSource source = context.getSource();
                source.sendMessage(Text.of(String.format("添加物品\"%s\"的别名\"%s\"成功", chineseName, alias)));
            }
            else {
                MyExceptionHandler.error(context, new RuntimeException("添加别名失败"), "添加别名失败");
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "拼音转换失败");
        }

        return Command.SINGLE_SUCCESS;
    }

    private static class ChineseNameSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            ItemIndexes itemIndexes = IndexJsonManager.getIndexesInstance(context);
            Set<String> keys = itemIndexes.cnKeys;
            String input = builder.getRemaining().replace("\"", "");
            for(String key: keys) {
                if(key.startsWith(input)) {
                    builder.suggest(key);
                }
            }
            return builder.buildFuture();
        }
    }
}
