package com.hjc.allitemindex.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class PFindCommand {

    /**
     * 默认的输出结果数
     */
    private static final int DEFAULT_LIMIT = 5;
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 20;

    /**
     * 目前支持的语言枚举
     */
    private enum Language {
        en, cn, pinyin, pinyinabbr
    }

    /**
     * 注册pf pfind指令
     * @param dispatcher 用于注册、解析和执行命令
     * @param registryAccess 为可能传入特定命令参数的注册表提供抽象方法
     * @param environment 识别命令将要注册到的服务器的类型
     */
    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment
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
        var sender = context.getSource();
        // 给指令的发送者返回信息，不广播给管理员
        sender.sendFeedback(() -> Text.literal(String.format("call pfind with %s %s %d", lang, query, limit)), false);
        switch (lang) {
            case en -> {
                // 英文查询
            }
            case cn -> {
                // 中文查询
            }
            case pinyin -> {
                // 拼音全称查询
            }
            case pinyinabbr -> {
                // 拼音缩写查询
            }
        }

        return Command.SINGLE_SUCCESS;
    }

}
