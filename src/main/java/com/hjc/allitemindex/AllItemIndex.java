package com.hjc.allitemindex;

import com.hjc.allitemindex.command.PFindArguments;
import com.hjc.allitemindex.command.PFindCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.util.Identifier;

public class AllItemIndex implements ModInitializer {

    @Override
    public void onInitialize() {
        // 注册自定义参数类型，话说有没有可能换到别的地方?
        ArgumentTypeRegistry.registerArgumentType(
                Identifier.of("allitemindex", "language"),
                PFindArguments.LanguageArgument.class,
                ConstantArgumentSerializer.of(PFindArguments.LanguageArgument::new)
        );

        // 注册命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PFindCommand.register(dispatcher, registryAccess, environment);
        });



    }
}
