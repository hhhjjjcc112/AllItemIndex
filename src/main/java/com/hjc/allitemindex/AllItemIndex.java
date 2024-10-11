package com.hjc.allitemindex;

import com.hjc.allitemindex.command.PFindArguments;
import com.hjc.allitemindex.command.PFindCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class AllItemIndex implements ModInitializer {

    @Override
    public void onInitialize() {
        // 注册自定义参数类型
        PFindArguments.registerAll();

        // 注册所有命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PFindCommand.register(dispatcher, registryAccess, environment);
        });



    }
}
