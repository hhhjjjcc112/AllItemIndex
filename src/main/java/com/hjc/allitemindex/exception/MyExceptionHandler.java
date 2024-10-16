package com.hjc.allitemindex.exception;


import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.logging.Logger;

public class MyExceptionHandler {

    public static final int VISITORS = 0;
    public static final int OPS = 2;

    private static final Logger LOGGER = Logger.getLogger("allitemindex");

    public static void error(
            CommandContext<ServerCommandSource> ctx,
            Throwable throwable,
            String message
    ) {
        ServerCommandSource source = ctx.getSource();
        MinecraftServer server = source.getServer();
        // 如果不是管理员, 那么返回如下消息
        if(!source.hasPermissionLevel(OPS)) {
            source.sendMessage(Text.literal("模组出现错误, 请联系服务器管理员").formatted(Formatting.RED));
        }
        PlayerManager manager = server.getPlayerManager();
        manager.broadcast(Text.literal("错误: " + message).formatted(Formatting.RED), false);
        manager.broadcast(Text.literal(throwable.getMessage()).formatted(Formatting.RED), false);

        LOGGER.warning(message);
        LOGGER.warning(throwable.getMessage());

    }
}
