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
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            var queryArg = CommandManager.argument("query", StringArgumentType.string()).executes(ctx -> {
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
        // 给指令的发送者返回信息
        sender.sendMessage(Text.literal(String.format("call pfind with %s %s %d", lang, query, limit)));
        // 获取当前的index表
        ItemIndexes itemIndexes = IndexJsonLoader.getIndexesInstance(context);
        // 比较函数
        Comparator<String> comparator = Comparator.comparingInt(s -> Similarity.editDistance(s, query));
        List<ItemInfo> results;
        switch (lang) {
            // 英文查询
            case en -> results = minKQueryResults(itemIndexes.enIndex, comparator, limit);
            // 中文查询
            case cn -> results = minKQueryResults(itemIndexes.cnIndex, comparator, limit);
            // 拼音查询
            case pinyin -> results = minKQueryResults(itemIndexes.pinyinIndex, comparator, limit);
            // 拼音全称查询
            case pinyin_abbr -> results = minKQueryResults(itemIndexes.pinyinAbbrIndex, comparator, limit);
            default -> throw new IllegalStateException("unreachable code");
        }
        // 输出结果
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
            Map<K, List<V>> map,
            Comparator<K> comparator,
            int limit
    ) {
        Set<K> keys = map.keySet();
        return keys.stream().sorted(comparator).flatMap(k -> map.get(k).stream()).distinct().limit(limit).toList();
    }

    private static Text genText(int index, ItemInfo info) {
        MutableText text = Text.literal(String.format("%d. %s: ", index, info.ChineseName));
//        text.append(Text.literal(info.floorLight.cnName).formatted(TextColor.fromRgb()));
        return text;
    }

}
