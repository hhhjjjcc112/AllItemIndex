package com.hjc.allitemindex.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class PFindCommand {

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment
    ) {

        dispatcher.register(CommandManager.literal("pfind")
                .then(CommandManager.argument("language", new PFindArguments.LanguageArgument())
                        .then(CommandManager.argument("value", StringArgumentType.string())
                                .executes(PFindCommand::pFindAction)
                        )
                )
        );
    }

    private static int pFindAction(CommandContext<ServerCommandSource> context) {
        String lan = context.getArgument("language", String.class);
        String value = StringArgumentType.getString(context, "value");
        context.getSource().sendFeedback(() -> Text.literal(String.format("call pfind with %s %s", lan, value)), false);

        return Command.SINGLE_SUCCESS;
    }

    private static class LanguageSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            builder.suggest("en");
            builder.suggest("cn");
            return builder.buildFuture();
        }
    }
}
