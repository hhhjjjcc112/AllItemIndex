package com.hjc.allitemindex.command;

import com.hjc.allitemindex.exception.MyExceptionHandler;
import com.hjc.allitemindex.model.ItemIndexes;
import com.hjc.allitemindex.model.ItemInfo;
import com.hjc.allitemindex.repository.IndexJsonManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Set;

public class PRemoveCommand {
    /**
     * 注册/premove /pr指令
     *
     * @param dispatcher 用于注册、解析和执行命令
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var pRemove = CommandManager.literal("premove").requires(ctx -> ctx.hasPermissionLevel(2));

        var pRemoveAlias = CommandManager.literal("alias").then(
                CommandManager.argument("alias", StringArgumentType.string())
                        .suggests(new SuggestionProviders.AliasSuggestionProvider())
                        .then(CommandManager.literal("from")
                                .then(CommandManager.argument("chineseName", StringArgumentType.string())
                                        .suggests(new SuggestionProviders.ChineseNameOfAliasSuggestionProvider(2))
                                        .executes(ctx -> removeAlias(
                                                ctx,
                                                StringArgumentType.getString(ctx, "alias"),
                                                StringArgumentType.getString(ctx, "chineseName"))
                                        )
                                ))

        );

        var pRemoveItem = CommandManager.literal("item")
                .then(CommandManager.literal("name").then(
                        CommandManager.argument("chineseName", StringArgumentType.string())
                                .suggests(new SuggestionProviders.ChineseNameSuggestionProvider())
                                .executes(ctx -> removeItem(
                                        ctx,
                                        StringArgumentType.getString(ctx, "chineseName"),
                                        false
                                ))
                                .then(CommandManager.literal("all")
                                        .executes(ctx -> removeItem(
                                                ctx,
                                                StringArgumentType.getString(ctx, "chineseName"),
                                                true
                                        ))
                                )
                                .then(CommandManager.literal("index")
                                        .then(CommandManager.argument("index", IntegerArgumentType.integer(0))
                                                .executes(ctx -> removeItemAtIndex(
                                                        ctx,
                                                        StringArgumentType.getString(ctx, "chineseName"),
                                                        IntegerArgumentType.getInteger(ctx, "index")
                                                )))
                                ))
                ).then(CommandManager.literal("id").then(CommandManager.argument("id", LongArgumentType.longArg(1))
                        .suggests(new SuggestionProviders.IdSuggestionProvider())
                        .executes(ctx -> removeItemAtId(
                                ctx,
                                LongArgumentType.getLong(ctx, "id")
                        ))
                ));

        pRemove.then(pRemoveAlias).then(pRemoveItem);

        var command = dispatcher.register(pRemove);

        dispatcher.register(CommandManager.literal("pr").requires(ctx -> ctx.hasPermissionLevel(2)).redirect(command));

    }

    /**
     * 删除别名
     * @param context     调用时的指令上下文
     * @param alias       别名
     * @param chineseName 中文名
     * @return 指令返回值
     */
    private static int removeAlias(
            CommandContext<ServerCommandSource> context,
            String alias,
            String chineseName
    ) {
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if (!indexes.chineseIndex.containsKey(chineseName)) {
            MyExceptionHandler.error(context, new Throwable("未找到中文名称为" + chineseName + "的物品"), "没有对应中文的物品");
            return 0;
        }
        Set<ItemInfo> items = indexes.chineseIndex.get(chineseName);
        // 取第一个物品的别名
        Set<String> aliases = items.iterator().next().chineseAlias;
        if (!aliases.contains(alias)) {
            MyExceptionHandler.error(context, new Throwable(String.format("%s没有别名%s", chineseName, alias)), "该物品没有对应别名");
            return 0;
        }
        try {
            if (IndexJsonManager.removeAlias(context, alias, chineseName)) {
                ServerCommandSource source = context.getSource();
                source.sendMessage(Text.of(String.format("成功删除%s的别名%s", chineseName, alias)));
                return Command.SINGLE_SUCCESS;
            } else {
                MyExceptionHandler.error(context, new Throwable("删除别名失败"), "删除别名失败");
                return 0;
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "删除别名时出现错误");
            return 0;
        }
    }

    /**
     * 删除物品, 如果有多个同名单片且不删除全部则提示用户选择
     * @param context     调用时的指令上下文
     * @param chineseName 中文名
     * @param removeAll   是否删除全部
     * @return 指令返回值
     */
    private static int removeItem(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            boolean removeAll
    ) {
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if (!indexes.chineseIndex.containsKey(chineseName)) {
            MyExceptionHandler.error(context, new Throwable("没有对应中文的物品"), "未找到中文名称为" + chineseName + "的物品");
            return 0;
        }
        Set<ItemInfo> items = indexes.chineseIndex.get(chineseName);
        // 删除全部
        if (removeAll) {
            try {
                if (IndexJsonManager.removeItems(context, items)) {
                    ServerCommandSource source = context.getSource();
                    source.sendMessage(Text.of(String.format("成功删除%s的全部单片", chineseName)));
                    // 把所有删除成功的展示给用户
                    var it = items.iterator();
                    for (int i = 1; it.hasNext(); i++) {
                        ItemInfo item = it.next();
                        source.sendMessage(genDescription(i, item));
                    }
                    return Command.SINGLE_SUCCESS;
                } else {
                    MyExceptionHandler.error(context, new Throwable("删除物品失败"), "删除物品失败");
                    return 0;
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                MyExceptionHandler.error(context, e, "删除物品时出现错误");
                return 0;
            }
        }
        // 需要指定ID或索引
        else if (items.size() > 1) {
            ServerCommandSource source = context.getSource();
            source.sendMessage(Text.of(String.format("存在多条与%s对应的物品, 请指定ID或索引", chineseName)));
            // 把所有记录展示给用户
            var it = items.iterator();
            for (int i = 1; it.hasNext(); i++) {
                ItemInfo item = it.next();
                source.sendMessage(genDescription(i, item));
            }
            return Command.SINGLE_SUCCESS;

        } else {
            // 删除第一个物品
            ItemInfo item = items.iterator().next();
            try {
                if (IndexJsonManager.removeItem(context, item)) {
                    ServerCommandSource source = context.getSource();
                    source.sendMessage(Text.of(String.format("成功删除%s的唯一一个单片", chineseName)));
                    // 把删除成功的展示给用户
                    source.sendMessage(genDescription(1, item));
                    return Command.SINGLE_SUCCESS;
                } else {
                    MyExceptionHandler.error(context, new Throwable("删除物品失败"), "删除物品失败");
                    return 0;
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                MyExceptionHandler.error(context, e, "删除物品时出现错误");
                return 0;
            }
        }
    }

    /**
     * 删除指定索引(取决于排序)的物品
     * @param context     调用时的指令上下文
     * @param chineseName 中文名
     * @param index       索引
     * @return 指令返回值
     */
    private static int removeItemAtIndex(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            int index
    ) {
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if (!indexes.chineseIndex.containsKey(chineseName)) {
            MyExceptionHandler.error(context, new Throwable("没有对应中文的物品"), "未找到中文名称为" + chineseName + "的物品");
            return 0;
        }
        Set<ItemInfo> items = indexes.chineseIndex.get(chineseName);
        if (index < 1 || index > items.size()) {
            MyExceptionHandler.error(context, new Throwable("索引超出范围"), "索引超出范围");
            ServerCommandSource source = context.getSource();
            // 把所有记录展示给用户, 这样用户好知道是什么单片
            var it = items.iterator();
            for (int i = 1; it.hasNext(); i++) {
                ItemInfo item = it.next();
                source.sendMessage(genDescription(i, item));
            }
            return 0;
        }
        ItemInfo item = items.stream().skip(index - 1).findFirst().orElseThrow();
        try {
            if (IndexJsonManager.removeItem(context, item)) {
                ServerCommandSource source = context.getSource();
                source.sendMessage(Text.of(String.format("成功删除%s的第%d个单片", chineseName, index)));
                // 把删除成功的展示给用户
                source.sendMessage(genDescription(index, item));
                return Command.SINGLE_SUCCESS;
            } else {
                MyExceptionHandler.error(context, new Throwable(String.format("删除物品%s失败", item.chineseName)), "删除物品失败");
                return 0;
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "删除物品时出现错误");
            return 0;
        }
    }

    /**
     * 删除指定ID的物品
     * @param context 调用时的指令上下文
     * @param id      ID
     * @return 指令返回值
     */
    private static int removeItemAtId(
            CommandContext<ServerCommandSource> context,
            long id
    ) {
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if(!indexes.idIndex.containsKey(id)) {
            MyExceptionHandler.error(context, new Throwable("未找到id为" + id + "的物品"), "未找到id为" + id + "的物品");
            return 0;
        }
        try {
            ItemInfo item = indexes.idIndex.get(id);
            if (IndexJsonManager.removeItem(context, item)) {
                ServerCommandSource source = context.getSource();
                source.sendMessage(Text.of(String.format("成功删除%s的id为%d的单片", item.chineseName, id)));
                // 把删除成功的展示给用户
                source.sendMessage(genDescription(1, item));
                return Command.SINGLE_SUCCESS;
            } else {
                MyExceptionHandler.error(context, new Throwable(String.format("删除物品%s失败", item.chineseName)), "删除物品失败");
                return 0;
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "删除物品时出现错误");
            return 0;
        }
    }

    private static MutableText genDescription(int index, ItemInfo item) {
            // 编号 + 中文名称
            MutableText text = Text.literal(String.format("%s 索引: %d id: %d 位置: ", item.chineseName, index, item.id.value));
            // 层灯光
            text.append(Text.translatable(item.floorLight.item.getTranslationKey()).setStyle(item.floorLight.colorStyle));
            text.append(" ");
            // 表示方向的地毯
            text.append(Text.translatable(item.directionColor.item.getTranslationKey()).setStyle(item.directionColor.colorStyle));
            text.append(" ");
            // 方向
            text.append(Text.of(item.direction.cn));
            text.append(" ");
            // 具体位置的地毯
            text.append(Text.translatable(item.carpetColor.item.getTranslationKey()).setStyle(item.carpetColor.colorStyle));
            return text;
    }

}
