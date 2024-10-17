package com.hjc.allitemindex.command;

import com.hjc.allitemindex.algorithm.Similarity;
import com.hjc.allitemindex.model.ItemIndexes;
import com.hjc.allitemindex.model.ItemInfo;
import com.hjc.allitemindex.repository.IndexJsonManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PFindCommand {

    /**
     * 默认的输出结果数
     */
    private static final int DEFAULT_LIMIT = 5;
    /**
     * 最少输出结果数
     */
    private static final int MIN_LIMIT = 1;
    /**
     * 最多输出结果数
     */
    private static final int MAX_LIMIT = 20;

    /**
     * 目前支持的语言的枚举
     */
    private enum Language {
        py, pinyin, cn, en, none
    }

    /**
     * 注册pf pfind指令
     * @param dispatcher 用于注册、解析和执行命令
     */
    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher
    ) {
//        registerLangVer(dispatcher);
        registerNoLangVer(dispatcher);
    }

    private static void registerLangVer(CommandDispatcher<ServerCommandSource> dispatcher) {
        var pFind = CommandManager.literal("pfind");
        // 对于每种语言lang, 都生成一个/pfind <lang>的指令
        for(var lang :Language.values()) {
            if(lang == Language.none) {
                continue;
            }
            // limit参数在最后
            var limitArg = CommandManager.argument("limit", IntegerArgumentType.integer(MIN_LIMIT, MAX_LIMIT)).executes(ctx -> {
                String query = StringArgumentType.getString(ctx, "query");
                int limit = IntegerArgumentType.getInteger(ctx, "limit");
                return pFindAction(ctx, lang, query, limit);
            });
            // query参数 <limit>
            var queryArg = CommandManager.argument("query", StringArgumentType.string())
                    // 自动补全提示
                    .suggests(new QuerySuggestionProvider(lang))
                    .executes(ctx -> {
                        String query = StringArgumentType.getString(ctx, "query");
                        return pFindAction(ctx, lang, query, DEFAULT_LIMIT);
                    })
                    .then(limitArg);
            // 指令形式为 /pfind <language> <query> <limit>
            pFind.then(CommandManager.literal(lang.name()).then(queryArg));
        }

        // 注册pfind指令
        var command = dispatcher.register(pFind);

        // 设置别名
        dispatcher.register(CommandManager.literal("pf").redirect(command));
    }

    private static void registerNoLangVer(CommandDispatcher<ServerCommandSource> dispatcher) {
        var limitArg = CommandManager.argument("limit", IntegerArgumentType.integer(MIN_LIMIT, MAX_LIMIT)).executes(ctx -> {
            String query = StringArgumentType.getString(ctx, "query");
            int limit = IntegerArgumentType.getInteger(ctx, "limit");
            return pFindAction(ctx, Language.none, query, limit);
        });
        // query参数 <limit>
        var queryArg = CommandManager.argument("query", StringArgumentType.string())
                // 自动补全提示
                .suggests(new QuerySuggestionProvider(Language.none))
                .executes(ctx -> {
                    String query = StringArgumentType.getString(ctx, "query");
                    return pFindAction(ctx, Language.none, query, DEFAULT_LIMIT);
                })
                .then(limitArg);
        var pFind = CommandManager.literal("pfind").then(queryArg);
        // 注册pfind指令
        var command = dispatcher.register(pFind);
        dispatcher.register(CommandManager.literal("pf").redirect(command));
    }

    // 输入指令后执行的内容，目前是打印参数
    private static int pFindAction(
            CommandContext<ServerCommandSource> context,
            Language lang,
            String query,
            int limit
    ) {
        // 获取指令的发送者
        ServerCommandSource sender = context.getSource();
//        // 给指令的发送者返回信息
//        sender.sendMessage(Text.literal(String.format("call pfind with %s %s %d", lang, query, limit)));
        // 获取当前的index表
        ItemIndexes itemIndexes = IndexJsonManager.getIndexesInstance(context);
        // 比较函数
        Comparator<String> lowercaseComparator = Similarity.getComparator(query);
        List<ItemInfo> results;
        switch (lang) {
            // 英文查询
            case en -> results = minKQueryResults(itemIndexes.enIndex, lowercaseComparator, limit);
            // 中文查询
            case cn -> results = minKQueryResults(itemIndexes.cnIndex, lowercaseComparator, limit);
            // 拼音查询
            case pinyin -> results = minKQueryResults(itemIndexes.pinyinIndex, lowercaseComparator, limit);
            // 拼音全称查询
            case py -> results = minKQueryResults(itemIndexes.pinyinAbbrIndex, lowercaseComparator, limit);
            case none -> results = minKQueryResults(itemIndexes.allIndex, lowercaseComparator, limit);
            default -> throw new IllegalStateException("unreachable code");
        }
        // 输出结果
        sender.sendMessage(Text.of(String.format("前 %s 个搜索 %s 的结果为:", limit, query)));
        for(int i = 0;i < results.size();i++) {
            sender.sendMessage(genText(i + 1, results.get(i), sender.hasPermissionLevel(2)));
        }

        return Command.SINGLE_SUCCESS;
    }

    /**
     * 返回前limit个与query最相似的key对应的value
     * @param map 包含K key与List<V> value映射关系的map
     * @param comparator 比较两个key与query相似度的比较器
     * @param limit 输出的结果数量
     * @return 前limit个结果
     * @param <K> 键的类型
     * @param <V> 值的类型
     */
    private static <K ,V> List<V> minKQueryResults(
            Map<K, Set<V>> map,
            Comparator<K> comparator,
            int limit
    ) {
        Set<K> keys = map.keySet();
        return keys.stream().sorted(comparator).flatMap(k -> map.get(k).stream()).distinct().limit(limit).toList();
    }

    private static MutableText genText(int index, ItemInfo info, boolean showId) {
        // 编号 + 中文名称
        MutableText text = Text.literal(String.format("%d. %s: ", index, info.chineseName));
        if(showId) {
            text.append(Text.of(String.format("id=%d ", info.id.id)));
        }
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

        return text;
    }


    private record QuerySuggestionProvider(Language lang) implements SuggestionProvider<ServerCommandSource> {

        @Override
            public CompletableFuture<Suggestions> getSuggestions(
                    CommandContext<ServerCommandSource> context,
                    SuggestionsBuilder builder
            ) {
                ItemIndexes itemIndexes = IndexJsonManager.getIndexesInstance(context);
                Set<String> keys;
                // 转换为小写
                String input = builder.getRemainingLowerCase().trim().replace("\"", "");
                switch(lang) {
                    case en -> keys = itemIndexes.enIndex.keySet();
                    case cn -> keys = itemIndexes.cnIndex.keySet();
                    case pinyin -> keys = itemIndexes.pinyinIndex.keySet();
                    case py -> keys = itemIndexes.pinyinAbbrIndex.keySet();
                    case none -> keys = itemIndexes.allIndex.keySet();
                    default -> throw new IllegalStateException("unreachable code");
                }
                for(var k : keys) {
                    if(k.toLowerCase().startsWith(input)) {
                        if(lang == Language.cn || lang == Language.none) {
                            builder.suggest(String.format("\"%s\"", k));
                        }
                        else {
                            builder.suggest(k);
                        }
                    }
                }
                return builder.buildFuture();
            }
        }

}
