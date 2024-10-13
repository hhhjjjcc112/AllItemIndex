package com.hjc.allitemindex.command;

import com.hjc.allitemindex.repository.IndexJsonLoader;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class PLoadCommand {

    /**
     * 注册/pload和/pl
     * @param dispatcher 用于注册、解析和执行命令
     */
    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher
    ) {
        var command = dispatcher.register(CommandManager.literal("pload").executes(PLoadCommand::pLoadAction));
        dispatcher.register(CommandManager.literal("pl").redirect(command));
    }

    private static int pLoadAction(
            CommandContext<ServerCommandSource> context
    ) {
        IndexJsonLoader.reload(context);
        return Command.SINGLE_SUCCESS;
    }
}
