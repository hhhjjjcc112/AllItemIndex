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

    private static final int DEFAULT_LIMIT = 5;

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment
    ) {
        // limit参数在最后
        var limitArg = CommandManager.argument("limit", IntegerArgumentType.integer())
                .executes(ctx -> {
                    String lang = ctx.getArgument("language", String.class);
                    String query = StringArgumentType.getString(ctx, "query");
                    int limit = IntegerArgumentType.getInteger(ctx, "limit");
                    return pFindAction(ctx, lang, query, limit);
                });
        // query参数 <limit>
        var queryArg = CommandManager.argument("query", StringArgumentType.string())
                .executes(ctx -> {
                    String lang = ctx.getArgument("language", String.class);
                    String query = StringArgumentType.getString(ctx, "query");
                    return pFindAction(ctx, lang, query, DEFAULT_LIMIT);
                })
                .then(limitArg);
        // language参数 <query> <limit>
        var languageArg = CommandManager.argument("language", new PFindArguments.LanguageArgument())
                .then(queryArg);
        // 指令形式为 /pfind <language> <query> <limit>
        var command = dispatcher.register(CommandManager.literal("pfind").then(languageArg));
        // 设置别名
        dispatcher.register(CommandManager.literal("pf").redirect(command));
    }

    // 输入指令后执行的内容，目前是打印参数
    private static int pFindAction(
            CommandContext<ServerCommandSource> context,
            String lang,
            String query,
            int limit
    ) {
        // 获取指令的发送者
        var sender = context.getSource();
        // 给指令的发送者返回信息，不广播给管理员
        sender.sendFeedback(() -> Text.literal(String.format("call pfind with %s %s %d", lang, query, limit)), false);

        return Command.SINGLE_SUCCESS;
    }
}
