package com.hjc.allitemindex.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;



public class PFindArguments {


    public static class LanguageArgument implements ArgumentType<String> {
        private static final Set<String> SUPPORTED_LANGUAGES = new HashSet<>(List.of("en", "cn", "pinyin"));
        private static final SimpleCommandExceptionType UNSUPPORTED_LANGUAGE_EXCEPTION = new SimpleCommandExceptionType(Text.literal("language not supported"));


        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
//            int start = reader.getCursor();
            String str = reader.readString();
            if(SUPPORTED_LANGUAGES.contains(str)) {
                return str;
            }
//            reader.setCursor(start);
            throw UNSUPPORTED_LANGUAGE_EXCEPTION.createWithContext(reader);
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            // 获取当前输如内容
            StringReader reader = new StringReader(builder.getInput());
            reader.setCursor(builder.getStart());
            try {
                // 还是读取一下语言字符串
                reader.readString();
                // 如果这个字符串位于最后，代表用户正在输入，那么就应该显示提示，反之不需要显示提示
                if(!reader.canRead()) {
                    for(var lang : SUPPORTED_LANGUAGES) {
                        builder.suggest(lang);
                    }
                }
            } catch (CommandSyntaxException e) {
            }
            return builder.buildFuture();
        }

        @Override
        public Collection<String> getExamples() {
            return SUPPORTED_LANGUAGES;
        }
    }
}
