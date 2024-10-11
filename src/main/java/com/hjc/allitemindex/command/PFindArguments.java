package com.hjc.allitemindex.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class PFindArguments {

    /**
     * 注册所有pf/pfind使用的自定义参数类
     */
    public static void registerAll() {
        LanguageArgument.register();
    }

    public enum Language {
        en, cn, pinyin, pinyinabbr
    }


    public static class LanguageArgument implements ArgumentType<Language> {
        // 将枚举类成员映射到字符串集合
        private static final Set<String> LANGUAGES_STRING_SET = Arrays.stream(Language.values()).map(Enum::name).collect(Collectors.toSet());
        private static final SimpleCommandExceptionType UNSUPPORTED_LANGUAGE_EXCEPTION = new SimpleCommandExceptionType(Text.literal("language not supported"));

        public static void register() {
            ArgumentTypeRegistry.registerArgumentType(
                    Identifier.of("allitemindex", "language"),
                    LanguageArgument.class,
                    ConstantArgumentSerializer.of(LanguageArgument::new)
            );
        }

        @Override
        public Language parse(StringReader reader) throws CommandSyntaxException {
//            int start = reader.getCursor();
            String str = reader.readUnquotedString();
            if(LANGUAGES_STRING_SET.contains(str)) {
                return Language.valueOf(str);
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
                    for(var lang : LANGUAGES_STRING_SET) {
                        builder.suggest(lang);
                    }
                }
            } catch (CommandSyntaxException e) {
            }
            return builder.buildFuture();
        }

        @Override
        public Collection<String> getExamples() {
            return LANGUAGES_STRING_SET;
        }
    }
}
