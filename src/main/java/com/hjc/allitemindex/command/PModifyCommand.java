package com.hjc.allitemindex.command;

import com.hjc.allitemindex.exception.MyExceptionHandler;
import com.hjc.allitemindex.model.*;
import com.hjc.allitemindex.repository.IndexJsonManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Iterator;
import java.util.Set;

public class PModifyCommand {

    /**
     * 注册/pmodify /pm指令
     * @param dispatcher 用于注册、解析和执行命令
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        var pModify = CommandManager.literal("pmodify").requires(ctx -> ctx.hasPermissionLevel(2));

        // 组装moveitem子指令
        var pMoveItem = CommandManager.literal("moveitem");
        var pMoveCn = CommandManager.literal("cn");
        var pMoveId = CommandManager.literal("id");
        var n11 = CommandManager.argument("chineseName", StringArgumentType.string()).suggests(new SuggestionProviders.ChineseNameSuggestionProvider());
        var n12 = CommandManager.argument("id", LongArgumentType.longArg(1))
                .suggests(new SuggestionProviders.IdSuggestionProvider());
        for(FloorLight floor : FloorLight.values()) {
            var n21 = CommandManager.literal(floor.cn);
            var n22 = CommandManager.literal(floor.cn);
            for(Direction direction : Direction.values()) {
                var n31 = CommandManager.literal(direction.cn);
                var n32 = CommandManager.literal(direction.cn);
                for(CarpetColor carpetColor : CarpetColor.values()) {
                    var n41 = CommandManager.literal(carpetColor.cn).executes(ctx -> moveItem(ctx, StringArgumentType.getString(ctx, "chineseName"), floor, direction, carpetColor))
                            .then(CommandManager.literal("index")
                                    .then(CommandManager.argument("index", IntegerArgumentType.integer(1)).executes(ctx -> moveItemAtIndex(ctx, StringArgumentType.getString(ctx, "chineseName"), IntegerArgumentType.getInteger(ctx, "index"), floor, direction, carpetColor))));
                    var n42 = CommandManager.literal(carpetColor.cn).executes(ctx -> moveItemAtId(ctx, LongArgumentType.getLong(ctx, "id"), floor, direction, carpetColor));

                    n31.then(n41);
                    n32.then(n42);
                }
                n21.then(n31);
                n22.then(n32);
            }
            n11.then(n21);
            n12.then(n22);
        }
        pMoveItem.then(pMoveCn.then(n11)).then(pMoveId.then(n12));
        // 组装modify子指令
        var pModifyEnglishName = CommandManager.literal("en")
                .then(CommandManager.argument("englishName", StringArgumentType.string())
                        .then(CommandManager.literal("for")
                                .then(CommandManager.argument("chineseName", StringArgumentType.string())
                                        .suggests(new SuggestionProviders.ChineseNameSuggestionProvider())
                                        .executes(ctx -> modifyEnglishName(ctx, StringArgumentType.getString(ctx, "chineseName"), StringArgumentType.getString(ctx, "englishName"))))));
        var pModifyAlias = CommandManager.literal("alias")
                .then(CommandManager.argument("oldAlias", StringArgumentType.string())
                        .suggests(new SuggestionProviders.AliasSuggestionProvider())
                        .then(CommandManager.literal("to").then(CommandManager.argument("newAlias", StringArgumentType.string())
                                .then(CommandManager.literal("for").then(CommandManager.argument("chineseName", StringArgumentType.string())
                                        .suggests(new SuggestionProviders.ChineseNameOfAliasSuggestionProvider(2))
                                        .executes(ctx -> modifyAlias(ctx, StringArgumentType.getString(ctx, "chineseName"), StringArgumentType.getString(ctx, "oldAlias"), StringArgumentType.getString(ctx, "newAlias"))))))));

        pModify.then(pMoveItem).then(pModifyEnglishName).then(pModifyAlias);

        var command = dispatcher.register(pModify);
        dispatcher.register(CommandManager.literal("pm").requires(ctx -> ctx.hasPermissionLevel(2)).executes(PModifyCommand::easterEgg).redirect(command));

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
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if(!indexes.chineseIndex.containsKey(chineseName)) {
            MyExceptionHandler.error(context, new RuntimeException(String.format("%s不存在于索引中", chineseName)), "物品不存在");
            return 0;
        }
        Set<ItemInfo> items = indexes.chineseIndex.get(chineseName);
        if (items.size() > 1) {
            ServerCommandSource source = context.getSource();
            source.sendMessage(Text.of("存在多个同名物品, 请指定索引或ID"));
            Iterator<ItemInfo> iterator = items.iterator();
            for(int i = 1; iterator.hasNext(); i++) {
                source.sendMessage(genDescription(i, iterator.next()));
            }
            return 0;
        }
        else {
            ItemInfo item = items.iterator().next();
            if(item.floorLight == toFloor && item.direction == toDirection && item.carpetColor == toCarpetColor) {
                MyExceptionHandler.error(context, new RuntimeException(String.format("%s已经在%s, %s, %s", chineseName, toFloor, toDirection, toCarpetColor)), "物品已存在");
                return 0;
            }
            try {
                if(IndexJsonManager.moveItem(context, item, toFloor, toDirection, toCarpetColor)) {
                    context.getSource().sendMessage(Text.of(String.format("移动%s到%s, %s, %s成功", chineseName, toFloor, toDirection, toCarpetColor)));
                    return Command.SINGLE_SUCCESS;
                }
                else {
                    MyExceptionHandler.error(context, new RuntimeException(String.format("移动%s失败", chineseName)), "移动失败");
                    return 0;
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                MyExceptionHandler.error(context, e, "拼音转换失败");
                return 0;
            }
        }
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
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if(!indexes.chineseIndex.containsKey(chineseName)) {
            MyExceptionHandler.error(context, new RuntimeException(String.format("%s不存在于索引中", chineseName)), "物品不存在");
            return 0;
        }
        Set<ItemInfo> items = indexes.chineseIndex.get(chineseName);
        if(index < 1 || index > items.size()) {
            MyExceptionHandler.error(context, new RuntimeException(String.format("%s不存在索引为%d的物品", chineseName, index)), "索引不存在");
            return 0;
        }
        ItemInfo item = items.stream().skip(index - 1).findFirst().orElseThrow();
        if(item.floorLight == toFloor && item.direction == toDirection && item.carpetColor == toCarpetColor) {
            MyExceptionHandler.error(context, new RuntimeException(String.format("%s index=%s已经在%s, %s, %s", chineseName, index, toFloor, toDirection, toCarpetColor)), "物品已存在");
            return 0;
        }
        try {
            if(IndexJsonManager.moveItem(context, item, toFloor, toDirection, toCarpetColor)) {
                context.getSource().sendMessage(Text.of(String.format("移动%s index=%s到%s, %s, %s成功", chineseName, index, toFloor, toDirection, toCarpetColor)));
                return Command.SINGLE_SUCCESS;
            }
            else {
                MyExceptionHandler.error(context, new RuntimeException(String.format("移动%s index=%s失败", chineseName, index)), "移动失败");
                return 0;
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "拼音转换失败");
            return 0;
        }
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
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if(!indexes.idIndex.containsKey(id)) {
            MyExceptionHandler.error(context, new RuntimeException(String.format("id=%d不存在于索引中", id)), "物品不存在");
            return 0;
        }
        ItemInfo item = indexes.idIndex.get(id);
        if(item.floorLight == toFloor && item.direction == toDirection && item.carpetColor == toCarpetColor) {
            MyExceptionHandler.error(context, new RuntimeException(String.format("%s id=%s已经在%s, %s, %s", item.chineseName, item.id.value, toFloor, toDirection, toCarpetColor)), "物品已存在");
            return 0;
        }
        try {
            if(IndexJsonManager.moveItem(context, item, toFloor, toDirection, toCarpetColor)) {
                context.getSource().sendMessage(Text.of(String.format("移动%s id=%s到%s, %s, %s成功", item.chineseName, item.id.value, toFloor, toDirection, toCarpetColor)));
                return Command.SINGLE_SUCCESS;
            }
            else {
                MyExceptionHandler.error(context, new RuntimeException(String.format("移动%s id=%s失败", item.chineseName, item.id)), "移动失败");
                return 0;
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "拼音转换失败");
            return 0;
        }
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
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if(!indexes.chineseIndex.containsKey(chineseName)) {
            MyExceptionHandler.error(context, new RuntimeException(String.format("%s不存在于索引中", chineseName)), "物品不存在");
            return 0;
        }
        String oldEnglishName = indexes.chineseIndex.get(chineseName).iterator().next().englishName;
        if (oldEnglishName.equals(newEnglishName)) {
            MyExceptionHandler.error(context, new RuntimeException(String.format("%s的英文名已经是%s", chineseName, newEnglishName)), "英文名已存在");
            return 0;
        }
        try {
            if(IndexJsonManager.modifyEnglishName(context, chineseName, newEnglishName)) {
                context.getSource().sendMessage(Text.of(String.format("修改%s的英文名为%s成功", chineseName, newEnglishName)));
                return Command.SINGLE_SUCCESS;
            }
            else {
                MyExceptionHandler.error(context, new RuntimeException(String.format("修改%s的英文名为%s失败", chineseName, newEnglishName)), "修改失败");
                return 0;
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "拼音转换失败");
            return 0;
        }
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
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if(!indexes.chineseIndex.containsKey(chineseName)) {
            MyExceptionHandler.error(context, new RuntimeException(String.format("%s不存在于索引中", chineseName)), "物品不存在");
            return 0;
        }
        ItemInfo item = indexes.chineseIndex.get(chineseName).iterator().next();
        if(!item.chineseAlias.contains(oldAlias)) {
            MyExceptionHandler.error(context, new RuntimeException(String.format("%s不存在别名%s", chineseName, oldAlias)), "别名不存在");
            return 0;
        }
        if (item.chineseAlias.contains(newAlias)) {
            MyExceptionHandler.error(context, new RuntimeException(String.format("%s已经存在别名%s", chineseName, newAlias)), "别名已存在");
            return 0;
        }
        try {
            if(IndexJsonManager.modifyAlias(context, chineseName, oldAlias, newAlias)) {
                context.getSource().sendMessage(Text.of(String.format("修改%s的别名%s为%s成功", chineseName, oldAlias, newAlias)));
                return Command.SINGLE_SUCCESS;
            }
            else {
                MyExceptionHandler.error(context, new RuntimeException(String.format("修改%s的别名%s为%s失败", chineseName, oldAlias, newAlias)), "修改失败");
                return 0;
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "拼音转换失败");
            return 0;
        }
    }

    private static MutableText genDescription(int index, ItemInfo item) {
        // 编号 + 中文名称
        MutableText text = Text.literal(String.format("%s 索引: %d id: %d 位置: ", item.chineseName, index, item.id.value));
        // 层灯光
        text.append(Text.translatable(item.floorLight.item.getTranslationKey()).setStyle(item.floorLight.colorStyle));
        text.append(" ");
        // 表示方向的地毯
        text.append(Text.translatable(item.directionColor.item.getTranslationKey()).setStyle(item.directionColor.colorStyle));
        text.append(" ");
        // 方向
        text.append(Text.of(item.direction.cn));
        text.append(" ");
        // 具体位置的地毯
        text.append(Text.translatable(item.carpetColor.item.getTranslationKey()).setStyle(item.carpetColor.colorStyle));
        return text;
    }

    /**
     * 一个彩蛋
     * @param context 调用时的指令上下文
     * @return 指令返回值
     */
    private static int easterEgg(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendMessage(Text.literal("PURE MEMORY").formatted(Formatting.AQUA));
        // 相信您应该知道这是哪首了(笑)
        source.sendMessage(Text.literal("10002221").formatted(Formatting.AQUA));
        return Command.SINGLE_SUCCESS;
    }
}
