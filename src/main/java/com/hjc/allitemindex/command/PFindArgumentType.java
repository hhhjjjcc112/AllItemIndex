package com.hjc.allitemindex.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PFindArgumentType implements ArgumentType<PFindArguments> {
    private static final Collection<String> EXAMPLES = List.of("en stick 20", "cn \"木棍\" 10", "pinyin mugun 5", "pinyinabbr mg 3");
    // 将枚举类成员映射到字符串集合
    private static final Set<String> LANGUAGES_STRING_SET = Arrays.stream(PFindArguments.Language.values()).map(Enum::name).collect(Collectors.toSet());

    private static final SimpleCommandExceptionType UNSUPPORTED_LANGUAGE_EXCEPTION = new SimpleCommandExceptionType(Text.literal("language not supported"));
    private static final SimpleCommandExceptionType EMPTY_QUERY_EXCEPTION = new SimpleCommandExceptionType(Text.literal("query should not be empty"));

    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 20;
    private static final int DEFAULT_LIMIT = 5;


    @Override
    public PFindArguments parse(StringReader reader) throws CommandSyntaxException {
        PFindArguments.Language lang = parseLanguage(reader);
        parseSpace(reader);
        String query = parseQuery(reader);
        int limit;
        if(reader.canRead()) {
            parseSpace(reader);
            if(reader.canRead()) {
                limit = parseLimit(reader);
            }
            else {
                limit = DEFAULT_LIMIT;
            }
        }
        else {
            limit = DEFAULT_LIMIT;
        }
        return new PFindArguments(lang, query, limit);
    }

    private PFindArguments.Language parseLanguage(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String langString = reader.readUnquotedString();
        if(!LANGUAGES_STRING_SET.contains(langString)) {
            reader.setCursor(start);
            throw UNSUPPORTED_LANGUAGE_EXCEPTION.createWithContext(reader);
        }
        return PFindArguments.Language.valueOf(langString);
    }

    private String parseQuery(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String queryString = reader.readString();
        if(queryString.isEmpty()) {
            reader.setCursor(start);
            throw EMPTY_QUERY_EXCEPTION.createWithContext(reader);
        }
        return queryString;
    }

    private int parseLimit(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        int limit = reader.readInt();
        if(limit < MIN_LIMIT) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(reader, limit, MIN_LIMIT);
        }
        else if(limit > MAX_LIMIT) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(reader, limit, MAX_LIMIT);
        }
        return limit;
    }

    private void parseSpace(StringReader reader) throws CommandSyntaxException {
        // 至少有一个空格
        if (reader.canRead()) {
            if (reader.peek() != CommandDispatcher.ARGUMENT_SEPARATOR_CHAR) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().createWithContext(reader);
            }
            reader.skip();
        }
        while(reader.canRead() && reader.peek() != CommandDispatcher.ARGUMENT_SEPARATOR_CHAR) {
            reader.skip();
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        // 获取当前输入内容
        StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
