package com.hjc.allitemindex.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PModifyCommand {

    /**
     * 注册/pmodify /pm指令
     * @param dispatcher 用于注册、解析和执行命令
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        var pModify = CommandManager.literal("pmodify").executes(context -> {
            context.getSource().sendMessage(Text.literal("PURE MEMORY").formatted(Formatting.AQUA));
            return Command.SINGLE_SUCCESS;
        });
        var command = dispatcher.register(pModify);
        dispatcher.register(CommandManager.literal("pm").redirect(command));

    }
}
