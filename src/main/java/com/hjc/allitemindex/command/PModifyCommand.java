package com.hjc.allitemindex.command;

import com.hjc.allitemindex.model.CarpetColor;
import com.hjc.allitemindex.model.Direction;
import com.hjc.allitemindex.model.FloorLight;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PModifyCommand {

    /**
     * 注册/pmodify /pm指令
     * @param dispatcher 用于注册、解析和执行命令
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        var pModify = CommandManager.literal("pmodify").executes(context -> {
            context.getSource().sendMessage(Text.literal("PURE MEMORY").formatted(Formatting.AQUA));
            return Command.SINGLE_SUCCESS;
        });
//        var command = dispatcher.register(pModify);
//        dispatcher.register(CommandManager.literal("pm").redirect(command));

    }

    /**
     * 移动物品到指定位置, 如果有多个同名单片则提示用户选择
     * @param context 调用时的指令上下文
     * @param chineseName 物品的中文名
     * @param toFloor 移动到的楼层
     * @param toDirection 移动到的方向
     * @param toCarpetColor 移动到的地毯颜色
     * @return 指令返回值
     */
    private static int moveItem(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            FloorLight toFloor,
            Direction toDirection,
            CarpetColor toCarpetColor
    ) {
        return Command.SINGLE_SUCCESS;
    }

    /**
     * 移动指定索引(取决于排序)的物品到指定位置
     * @param context 调用时的指令上下文
     * @param chineseName 物品的中文名
     * @param index 物品的索引
     * @param toFloor 移动到的楼层
     * @param toDirection 移动到的方向
     * @param toCarpetColor 移动到的地毯颜色
     * @return 指令返回值
     */
    private static int moveItemAtIndex(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            int index,
            FloorLight toFloor,
            Direction toDirection,
            CarpetColor toCarpetColor
    ) {
        return Command.SINGLE_SUCCESS;
    }

    /**
     * 移动指定id的物品到指定位置
     * @param context 调用时的指令上下文
     * @param id 物品的id
     * @param toFloor 移动到的楼层
     * @param toDirection 移动到的方向
     * @param toCarpetColor 移动到的地毯颜色
     * @return 指令返回值
     */
    private static int moveItemAtId(
            CommandContext<ServerCommandSource> context,
            long id,
            FloorLight toFloor,
            Direction toDirection,
            CarpetColor toCarpetColor
    ) {
        return Command.SINGLE_SUCCESS;
    }

    /**
     * 修改物品的英文名
     * @param context 调用时的指令上下文
     * @param chineseName 物品的中文名
     * @param newEnglishName 物品的新英文名
     * @return 指令返回值
     */
    private static int modifyEnglishName(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            String newEnglishName
    ) {
        return Command.SINGLE_SUCCESS;
    }

    /**
     * 修改物品的指定别名
     * @param context 调用时的指令上下文
     * @param chineseName 物品的中文名
     * @param oldAlias 物品的旧别名
     * @param newAlias 物品的新别名
     * @return 指令返回值
     */
    private static int modifyAlias(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            String oldAlias,
            String newAlias
    ) {
        return Command.SINGLE_SUCCESS;
    }
}
