package com.hjc.allitemindex.command;

import com.hjc.allitemindex.algorithm.Similarity;
import com.hjc.allitemindex.model.ItemIndexes;
import com.hjc.allitemindex.model.ItemInfo;
import com.hjc.allitemindex.repository.IndexJsonLoader;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
        en, cn, pinyin, pinyin_abbr
    }

    /**
     * 注册pf pfind指令
     * @param dispatcher 用于注册、解析和执行命令
     */
    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher
    ) {
        // 对于每种语言lang, 都生成一个/pfind <lang>的指令
        for(var lang :Language.values()) {
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
            var command = dispatcher.register(CommandManager.literal("pfind")
                    .then(CommandManager.literal(lang.name())
                            .then(queryArg)));

            // 设置别名
            dispatcher.register(CommandManager.literal("pf").redirect(command));
        }
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
        ItemIndexes itemIndexes = IndexJsonLoader.getIndexesInstance(context);
        // 比较函数
        Comparator<String> comparator = Similarity.getComparator(query.trim());
        Comparator<String> lowercaseComparator = Similarity.getComparator(query.trim().toLowerCase());
        List<ItemInfo> results;
        switch (lang) {
            // 英文查询
            case en -> results = minKQueryResults(itemIndexes.enIndex, lowercaseComparator, limit);
            // 中文查询
            case cn -> results = minKQueryResults(itemIndexes.cnIndex, comparator, limit);
            // 拼音查询
            case pinyin -> results = minKQueryResults(itemIndexes.pinyinIndex, lowercaseComparator, limit);
            // 拼音全称查询
            case pinyin_abbr -> results = minKQueryResults(itemIndexes.pinyinAbbrIndex, lowercaseComparator, limit);
            default -> throw new IllegalStateException("unreachable code");
        }
        // 输出结果
        sender.sendMessage(Text.translatable("pfind.result", limit, query));
        for(int i = 0;i < results.size();i++) {
            sender.sendMessage(genText(i + 1, results.get(i)));
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
        return keys.stream().sorted(comparator).peek(System.out::println).flatMap(k -> map.get(k).stream()).peek(System.out::println).distinct().limit(limit).toList();
    }

    private static MutableText genText(int index, ItemInfo info) {
        // 编号 + 中文名称
        MutableText text = Text.literal(String.format("%d. %s: ", index, info.chineseName.chineseName));
        // 层灯光
        text.append(Text.translatable(info.floorLight.item.getTranslationKey()).setStyle(info.floorLight.colorStyle));
        text.append(" ");
        // 表示方向的地毯
        text.append(Text.translatable(info.directionColor.item.getTranslationKey()).setStyle(info.directionColor.colorStyle));
        text.append(" ");
        // 方向
        text.append(Text.translatable(info.direction.translationKey));
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
            ) throws CommandSyntaxException {
                ItemIndexes itemIndexes = IndexJsonLoader.getIndexesInstance(context);
                Set<String> keys;
                String input = builder.getRemainingLowerCase();
                switch(lang) {
                    case en -> keys = itemIndexes.enIndex.keySet();
                    case cn -> keys = itemIndexes.cnKeys;
                    case pinyin -> keys = itemIndexes.pinyinIndex.keySet();
                    case pinyin_abbr -> keys = itemIndexes.pinyinAbbrIndex.keySet();
                    default -> throw new IllegalStateException("unreachable code");
                }
                for(var k : keys) {
                    if(k.contains(input)) {
                        builder.suggest(k);
                    }
                }
                return builder.buildFuture();
            }
        }

}
