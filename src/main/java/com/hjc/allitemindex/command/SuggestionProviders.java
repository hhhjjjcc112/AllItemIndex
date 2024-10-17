package com.hjc.allitemindex.command;

import com.hjc.allitemindex.model.ItemInfo;
import com.hjc.allitemindex.repository.IndexJsonManager;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SuggestionProviders {

    public static final class AliasSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
            String input = builder.getRemaining().replace("\"", "");

            Set<ItemInfo> infos = IndexJsonManager.getInfosInstance(context);
            infos.stream().map(info -> info.chineseAlias).flatMap(Set::stream).distinct().filter(alias -> alias.startsWith(input)).forEach(str -> builder.suggest(String.format("\"%s\"", str)));
            return builder.buildFuture();
        }
    }

    public static final class ChineseNameSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
            String input = builder.getRemaining().replace("\"", "");
            Set<ItemInfo> infos = IndexJsonManager.getInfosInstance(context);
            infos.stream().map(info -> info.chineseName).distinct().filter(name -> name.startsWith(input)).forEach(str -> builder.suggest(String.format("\"%s\"", str)));
            return builder.buildFuture();
        }
    }
}
