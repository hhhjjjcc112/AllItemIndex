package com.hjc.allitemindex.command;

import com.hjc.allitemindex.exception.MyExceptionHandler;
import com.hjc.allitemindex.model.ItemIndexes;
import com.hjc.allitemindex.model.ItemInfo;
import com.hjc.allitemindex.repository.IndexJsonManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
                                        .suggests(new ChineseNameOfAliasSuggestionProvider())
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
                ).then(CommandManager.literal("id").then(CommandManager.argument("id", LongArgumentType.longArg(0))
                        .executes(ctx -> removeItemAtId(
                                ctx,
                                LongArgumentType.getLong(ctx, "id")
                        ))
                ));

        pRemove.then(pRemoveAlias).then(pRemoveItem);

        var command = dispatcher.register(pRemove);

        dispatcher.register(CommandManager.literal("pr").requires(ctx -> ctx.hasPermissionLevel(2)).redirect(command));

    }

    private static int removeAlias(
            CommandContext<ServerCommandSource> context,
            String alias,
            String chineseName
    ) {
        Set<ItemInfo> infos = IndexJsonManager.getInfosInstance(context);
        Set<ItemInfo> items = infos.stream().filter(info -> chineseName.equals(info.chineseName)).collect(Collectors.toSet());
        if (items.isEmpty()) {
            MyExceptionHandler.error(context, new Throwable("没有对应中文的物品"), "未找到中文名称为" + chineseName + "的物品");
            return -1;
        }
        // 取第一个物品的别名
        Set<String> aliases = items.iterator().next().chineseAlias;
        if (!aliases.contains(alias)) {
            MyExceptionHandler.error(context, new Throwable("该物品没有对应别名"), "未找到别名" + alias);
            return -2;
        }
        try {
            if (IndexJsonManager.removeAlias(context, alias, chineseName)) {
                ServerCommandSource source = context.getSource();
                source.sendMessage(Text.of(String.format("成功删除%s的别名%s", chineseName, alias)));
            } else {
                MyExceptionHandler.error(context, new Throwable("删除别名失败"), "删除别名失败");
                return -3;
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "删除别名时出现错误");
            return -4;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int removeItem(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            boolean removeAll
    ) {
        Set<ItemInfo> items = getItemsWithChineseName(context, chineseName);
        if (items.isEmpty()) {
            MyExceptionHandler.error(context, new Throwable("没有对应中文的物品"), "未找到中文名称为" + chineseName + "的物品");
            return -1;
        }
        // 删除全部
        if (removeAll) {
            try {
                if (IndexJsonManager.removeItems(context, items)) {
                    ServerCommandSource source = context.getSource();
                    source.sendMessage(Text.of(String.format("成功删除%s的全部单片", chineseName)));
                } else {
                    MyExceptionHandler.error(context, new Throwable("删除物品失败"), "删除物品失败");
                    return -2;
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                MyExceptionHandler.error(context, e, "删除物品时出现错误");
                return -3;
            }
        }
        // 需要指定ID或索引
        else if (items.size() > 1) {
            ServerCommandSource source = context.getSource();
            source.sendMessage(Text.of(String.format("存在多条与%s对应的物品, 请指定ID或索引", chineseName)));
            var it = items.iterator();
            for (int i = 0; i < items.size(); i++) {
                ItemInfo info = it.next();
                int index = i + 1;

                // 编号 + 中文名称
                MutableText text = Text.literal(String.format("%s 索引: %d id: %d 位置: ", info.chineseName, index, info.id.id));
                // 层灯光
                text.append(Text.translatable(info.floorLight.item.getTranslationKey()).setStyle(info.floorLight.colorStyle));
                text.append(" ");
                // 表示方向的地毯
                text.append(Text.translatable(info.directionColor.item.getTranslationKey()).setStyle(info.directionColor.colorStyle));
                text.append(" ");
                // 方向
                text.append(Text.of(info.direction.cn));
                text.append(" ");
                // 具体位置的地毯
                text.append(Text.translatable(info.carpetColor.item.getTranslationKey()).setStyle(info.carpetColor.colorStyle));

                // 分别提示每条记录
                source.sendMessage(text);
            }
        } else {
            // 删除第一个物品
            ItemInfo item = items.iterator().next();
            try {
                if (IndexJsonManager.removeItem(context, item)) {
                    ServerCommandSource source = context.getSource();
                    source.sendMessage(Text.of(String.format("成功删除%s的第一个单片", chineseName)));
                } else {
                    MyExceptionHandler.error(context, new Throwable("删除物品失败"), "删除物品失败");
                    return -4;
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                MyExceptionHandler.error(context, e, "删除物品时出现错误");
                return -5;
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int removeItemAtIndex(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            int index
    ) {
        Set<ItemInfo> items = getItemsWithChineseName(context, chineseName);
        if (items.isEmpty()) {
            MyExceptionHandler.error(context, new Throwable("没有对应中文的物品"), "未找到中文名称为" + chineseName + "的物品");
            return -1;
        }
        if (index < 1 || index > items.size()) {
            MyExceptionHandler.error(context, new Throwable("索引超出范围"), "索引超出范围");
            return -2;
        }
        ItemInfo item = items.stream().skip(index - 1).findFirst().orElseThrow();
        try {
            if (IndexJsonManager.removeItem(context, item)) {
                ServerCommandSource source = context.getSource();
                source.sendMessage(Text.of(String.format("成功删除%s的第%d个单片", chineseName, index)));
            } else {
                MyExceptionHandler.error(context, new Throwable("删除物品失败"), "删除物品失败");
                return -3;
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "删除物品时出现错误");
            return -4;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int removeItemAtId(
            CommandContext<ServerCommandSource> context,
            long id
    ) {
        Set<ItemInfo> items = IndexJsonManager.getInfosInstance(context);
        try {
            ItemInfo item = items.stream().filter(info -> info.id.id == id).findFirst().orElseThrow();
            if (IndexJsonManager.removeItem(context, item)) {
                ServerCommandSource source = context.getSource();
                source.sendMessage(Text.of(String.format("成功删除%s的id为%d的单片", item.chineseName, id)));
            } else {
                MyExceptionHandler.error(context, new Throwable("删除物品失败"), "删除物品失败");
                return -2;
            }
        } catch (NoSuchElementException e) {
            MyExceptionHandler.error(context, e, "未找到id为" + id + "的物品");
            return -3;
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "删除物品时出现错误");
            return -4;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Set<ItemInfo> getItemsWithChineseName(CommandContext<ServerCommandSource> context, String chineseName) {
        return IndexJsonManager.getInfosInstance(context).stream().filter(info -> chineseName.equals(info.chineseName)).collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    private static final class ChineseNameOfAliasSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

        private static final DynamicCommandExceptionType NO_SUCH_ALIAS = new DynamicCommandExceptionType((alias) -> Text.of("未找到别名" + alias));

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            String str = builder.getInput();
            StringReader reader;
            if (str.startsWith("/premove alias ")) {
                reader = new StringReader(str.substring("/premove alias ".length()));
            } else {
                reader = new StringReader(str.substring("/pr alias ".length()));
            }
            String alias = reader.readString();
            System.out.println(builder.getInput());
            System.out.println(alias);
            String input = builder.getRemaining().replace("\"", "");
            ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
            if (!indexes.cnIndex.containsKey(alias)) {
                throw NO_SUCH_ALIAS.create(alias);
            }
            indexes.cnIndex.get(alias).stream().map(info -> info.chineseName).distinct().filter(name -> name.startsWith(input)).forEach(s -> builder.suggest(String.format("\"%s\"", s)));
            return builder.buildFuture();
        }
    }

}
