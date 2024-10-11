package com.hjc.allitemindex.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class PFindCommand {

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment
    ) {
        // 指令形式为 /pfind <language> <value>
        dispatcher.register(CommandManager.literal("pfind")
                .then(CommandManager.argument("language", new PFindArguments.LanguageArgument())
                        .then(CommandManager.argument("value", StringArgumentType.string())
                                .executes(PFindCommand::pFindAction)
                        )
                )
        );
    }

    // 输入指令后执行的内容，目前是打印参数
    private static int pFindAction(CommandContext<ServerCommandSource> context) {
        String lan = context.getArgument("language", String.class);
        String value = StringArgumentType.getString(context, "value");
        context.getSource().sendFeedback(() -> Text.literal(String.format("call pfind with %s %s", lan, value)), false);

        return Command.SINGLE_SUCCESS;
    }
}
