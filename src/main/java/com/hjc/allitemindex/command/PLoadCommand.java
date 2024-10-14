package com.hjc.allitemindex.command;

import com.hjc.allitemindex.repository.IndexJsonLoader;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.hjc.allitemindex.AllItemIndex.USE_CHINESE;

public class PLoadCommand {

    /**
     * 最低权限要求，至少为管理员
     */
    private static final int LEAST_PERMISSION_LEVEL = 2;

    /**
     * 注册/pload和/pl
     * @param dispatcher 用于注册、解析和执行命令
     */
    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher
    ) {
        dispatcher.register(CommandManager.literal("pload").requires(ctx -> ctx.hasPermissionLevel(LEAST_PERMISSION_LEVEL)).executes(PLoadCommand::pLoadAction));
        dispatcher.register(CommandManager.literal("pl").requires(ctx -> ctx.hasPermissionLevel(LEAST_PERMISSION_LEVEL)).executes(PLoadCommand::pLoadAction));
    }

    private static int pLoadAction(
            CommandContext<ServerCommandSource> context
    ) {
        var source = context.getSource();
        if(USE_CHINESE) {
            source.sendMessage(Text.of("正在尝试重新加载index.json"));
        } else {
            source.sendMessage(Text.translatable("pload.reloadAttempt"));
        }
        boolean success = IndexJsonLoader.reload(context);
        if (success) {
            if(USE_CHINESE) {
                source.sendMessage(Text.of("重新加载index.json成功!"));
            } else {
                source.sendMessage(Text.translatable("pload.success"));
            }
        }
        else {
            if(USE_CHINESE) {
                source.sendMessage(Text.of("重新加载index.json失败"));
            } else {
                source.sendMessage(Text.translatable("pload.failure"));
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
