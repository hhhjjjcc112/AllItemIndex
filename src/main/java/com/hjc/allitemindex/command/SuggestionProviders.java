package com.hjc.allitemindex.command;

import com.hjc.allitemindex.model.ItemIndexes;
import com.hjc.allitemindex.model.ItemInfo;
import com.hjc.allitemindex.repository.IndexJsonManager;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
            infos.stream().flatMap(info -> info.chineseAlias.stream()).filter(alias -> alias.startsWith(input)).distinct().forEach(str -> builder.suggest(String.format("\"%s\"", str)));
            return builder.buildFuture();
        }
    }

    public static final class ChineseNameSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
            String input = builder.getRemaining().replace("\"", "");
            ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
            for(String str : indexes.chineseIndex.keySet()) {
                if(str.startsWith(input)) {
                    builder.suggest(String.format("\"%s\"", str));
                }
            }
            return builder.buildFuture();
        }
    }

    public static final class IdSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
            String input = builder.getRemaining().replace("\"", "");
            ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
            for(long id : indexes.idIndex.keySet()) {
                if(String.valueOf(id).startsWith(input)) {
                    builder.suggest(String.format("%d", id));
                }
            }
            return builder.buildFuture();
        }
    }

    public record ChineseNameOfAliasSuggestionProvider(int skip) implements SuggestionProvider<ServerCommandSource> {

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            String str = builder.getInput();

            StringReader reader = new StringReader(str.substring(1));
            for(int i = 0; i < skip; i++) {
                reader.readString();
                reader.skipWhitespace();
            }
            String alias = reader.readString();

            String input = builder.getRemaining().replace("\"", "");
            Set<ItemInfo> infos = IndexJsonManager.getInfosInstance(context);
            infos.stream().filter(info->info.chineseAlias.contains(alias)).map(info->info.chineseName).filter(name->name.startsWith(input)).distinct().forEach(name -> builder.suggest(String.format("\"%s\"", name)));
            return builder.buildFuture();
        }
    }

    private record AliasOfChineseNameSuggestionProvider(int skip) implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(
                CommandContext<ServerCommandSource> context,
                SuggestionsBuilder builder
        ) throws CommandSyntaxException {
            String str = builder.getInput();
            StringReader reader = new StringReader(str.substring(1));
            for(int i = 0; i < skip; i++) {
                reader.readString();
                reader.skipWhitespace();
            }
            String chineseName = reader.readString();
            String input = builder.getRemaining().replace("\"", "");

            ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
            if(indexes.chineseIndex.containsKey(chineseName)) {
                Set<String> aliases = indexes.chineseIndex.get(chineseName).iterator().next().chineseAlias;
                for(var alias : aliases) {
                    if(alias.startsWith(input)) {
                        builder.suggest(String.format("\"%s\"", alias));
                    }
                }
            }
            return builder.buildFuture();
        }
    }
}
