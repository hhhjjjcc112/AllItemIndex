package com.hjc.allitemindex;

import com.hjc.allitemindex.command.*;
import com.hjc.allitemindex.repository.IndexJsonManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class AllItemIndex implements ModInitializer {

    @Override
    public void onInitialize() {

        // 注册所有命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PFindCommand.register(dispatcher);
            PLoadCommand.register(dispatcher);
            PAddCommand.register(dispatcher);
            PRemoveCommand.register(dispatcher);
            PModifyCommand.register(dispatcher);
        });

        // 创建文件夹
        IndexJsonManager.initialize();
    }
}
