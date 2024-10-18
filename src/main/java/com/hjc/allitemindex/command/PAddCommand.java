package com.hjc.allitemindex.command;

import com.hjc.allitemindex.exception.MyExceptionHandler;
import com.hjc.allitemindex.model.*;
import com.hjc.allitemindex.repository.IndexJsonManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class PAddCommand {


    /**
     * 注册/padd /pa指令
     * @param dispatcher 用于注册、解析和执行命令
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var pAdd = CommandManager.literal("padd").requires(ctx -> ctx.hasPermissionLevel(2));
        var pAddAlias = CommandManager.literal("alias");
        pAddAlias.then(
            CommandManager.argument("alias", StringArgumentType.string())
                .then(CommandManager.literal("for")
                    .then(CommandManager.argument("chineseName", StringArgumentType.string())
                    .suggests(new SuggestionProviders.ChineseNameSuggestionProvider())
                    .executes(context -> addAlias(
                        context,
                        StringArgumentType.getString(context, "alias"),
                        StringArgumentType.getString(context, "chineseName")
                    ))
                ))

        );
        var pAddItem = CommandManager.literal("item");
        var pAddItemNew = CommandManager.literal("new");
        var pAddItemExist = CommandManager.literal("exist");
        var pAddItemNewEn = CommandManager.argument("englishName", StringArgumentType.string());
        var pAddItemExistCn = CommandManager.argument("chineseName", StringArgumentType.string()).suggests(new SuggestionProviders.ChineseNameSuggestionProvider());
        for(var floorLight: FloorLight.values()) {
            // 相比于让用户输入字符串然后我去判断是否合法, 或者自定义一种参数类型, 这种方法或许更好
            // 只要是可预知数量参数的枚举, 理论上都能用这种方式实现
            var existFloorLight = CommandManager.literal(floorLight.cn);
            var newFloorLight = CommandManager.literal(floorLight.cn);
            for(var direction: Direction.values()) {
                var existDirection = CommandManager.literal(direction.cn);
                var newDirection = CommandManager.literal(direction.cn);
                for(var carpetColor: CarpetColor.values()) {
                    var existCarpetColor = CommandManager.literal(carpetColor.cn)
                            .executes(context -> addItemExist(
                                context,
                                StringArgumentType.getString(context, "chineseName"),
                                floorLight,
                                direction,
                                carpetColor
                            ));
                    var newCarpetColor = CommandManager.literal(carpetColor.cn)
                            .executes(context -> addItemNew(
                                context,
                                StringArgumentType.getString(context, "chineseName"),
                                StringArgumentType.getString(context, "englishName"),
                                floorLight,
                                direction,
                                carpetColor
                            ));
                    newDirection.then(newCarpetColor);
                    existDirection.then(existCarpetColor);
                }
                newFloorLight.then(newDirection);
                existFloorLight.then(existDirection);
            }
            // 为什么then要放在最后呢? 因为then会调用其参数builder的build方法(会导致builder被复制一份), 此时再对其参数builder进行修改将不会被反映到最后的结果里
            // 顺带一提, 可以看到.then方法会返回自身, 所以不需要再次给自己赋值
            pAddItemNewEn.then(newFloorLight);
            pAddItemExistCn.then(existFloorLight);
        }
        pAddItemExist.then(pAddItemExistCn);
        pAddItemNew.then(CommandManager.argument("chineseName", StringArgumentType.string()).then(pAddItemNewEn));
        pAddItem.then(pAddItemNew).then(pAddItemExist);
        pAdd.then(pAddItem).then(pAddAlias);

        var command = dispatcher.register(pAdd);
        dispatcher.register(CommandManager.literal("pa").requires(ctx -> ctx.hasPermissionLevel(2)).redirect(command));
    }

    /**
     * 添加新物品单片, 当且仅当物品不存在于索引中
     * @param context 调用时的指令上下文
     * @param chineseName 中文名称
     * @param englishName 英文名称
     * @param floorLight 层灯光
     * @param direction 方向
     * @param carpetColor 地毯颜色
     * @return 指令返回值
     */
    private static int addItemNew(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            String englishName,
            FloorLight floorLight,
            Direction direction,
            CarpetColor carpetColor
    ) {
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if (indexes.chineseIndex.containsKey(chineseName)) {
            MyExceptionHandler.error(context, new IllegalArgumentException(String.format("中文名称%s已存在", chineseName)), "中文名称已存在");
            return 0;
        }
        ItemInfo info = new ItemInfo();
        info.chineseName = chineseName;
        info.englishName = englishName;
        info.floorLight = floorLight;
        info.direction = direction;
        info.carpetColor = carpetColor;
        // 创建一个空的别名集合
        info.chineseAlias = new LinkedHashSet<>();
        // 计算方向对应的颜色
        info.directionColor = Direction.correspondingColors.get(direction);
        try {
            if(IndexJsonManager.addItem(context, info)) {
                ServerCommandSource source = context.getSource();
                // 唉, 懒得再搞翻译键了
                source.sendMessage(Text.of(String.format("成功添加新物品单片: %s", chineseName)));
                // 展示添加成功的给用户看
                source.sendMessage(genDescription(info));
                return Command.SINGLE_SUCCESS;
            }
            else {
                MyExceptionHandler.error(context, new RuntimeException(String.format("添加新物品单片%s失败", chineseName)), "添加新物品单片失败");
                return -1;
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "拼音转换失败");
            return -2;
        }
    }

    /**
     * 添加已存在的物品的新单片, 当且仅当物品存在于索引中
     * @param context 调用时的指令上下文
     * @param chineseName 中文名称
     * @param floorLight 层灯光
     * @param direction 方向
     * @param carpetColor 地毯颜色
     * @return 指令返回值
     */
    private static int addItemExist(
            CommandContext<ServerCommandSource> context,
            String chineseName,
            FloorLight floorLight,
            Direction direction,
            CarpetColor carpetColor
    ) {
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if (!indexes.chineseIndex.containsKey(chineseName)) {
            MyExceptionHandler.error(context, new IllegalArgumentException(String.format("中文名称%s不存在", chineseName)), "中文名称不存在");
            return 0;
        }
        Set<ItemInfo> items = indexes.chineseIndex.get(chineseName);
        Iterator<ItemInfo> iterator = items.iterator();
        // 可以保证iterator里一定有一个对象
        ItemInfo info = iterator.next();

        ItemInfo newInfo = new ItemInfo();
        newInfo.chineseName = chineseName;
        newInfo.englishName = info.englishName;
        newInfo.floorLight = floorLight;
        newInfo.direction = direction;
        newInfo.carpetColor = carpetColor;
        // 直接使用已有单片的别名集合, 反正是集合, 不用担心添加两次同一别名
        newInfo.chineseAlias = info.chineseAlias;
        newInfo.directionColor = Direction.correspondingColors.get(direction);
        try {
            if(IndexJsonManager.addItem(context, newInfo)) {
                ServerCommandSource source = context.getSource();
                source.sendMessage(Text.of(String.format("添加物品单片成功: %s", chineseName)));
                // 展示添加成功的给用户看
                source.sendMessage(genDescription(newInfo));
                return Command.SINGLE_SUCCESS;
            }
            else {
                MyExceptionHandler.error(context, new RuntimeException(String.format("添加物品单片%s失败", chineseName)), "添加物品单片失败");
                return -1;
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "拼音转换失败");
            return -2;
        }
    }

    /**
     * 为物品添加别名
     * @param context 调用时的指令上下文
     * @param alias 别名
     * @param chineseName 中文名称
     * @return 指令返回值
     */
    private static int addAlias(
            CommandContext<ServerCommandSource> context,
            String alias,
            String chineseName
    ) {
        ItemIndexes indexes = IndexJsonManager.getIndexesInstance(context);
        if (!indexes.chineseIndex.containsKey(chineseName)) {
            MyExceptionHandler.error(context, new IllegalArgumentException(String.format("中文名称%s不存在", chineseName)), "中文名称不存在");
            return 0;
        }
        else if(indexes.chineseIndex.containsKey(alias)) {
            MyExceptionHandler.error(context, new IllegalArgumentException(String.format("请不要使用此别称: %s, 因为存在与该名称相同的中文名称", alias)), "中文名称已存在");
            return 0;
        }
        Set<ItemInfo> items = indexes.chineseIndex.get(chineseName);
        Iterator<ItemInfo> iterator = items.iterator();
        ItemInfo info = iterator.next();
        if(info.chineseAlias.contains(alias)) {
            MyExceptionHandler.error(context, new IllegalArgumentException(String.format("%s的别名%s已存在",chineseName, alias)), "中文别称已存在");
            return 0;
        }
        try {
            if(IndexJsonManager.addAlias(context, alias, chineseName)) {
                ServerCommandSource source = context.getSource();
                source.sendMessage(Text.of(String.format("添加物品\"%s\"的别名\"%s\"成功", chineseName, alias)));
                return Command.SINGLE_SUCCESS;
            }
            else {
                MyExceptionHandler.error(context, new RuntimeException(String.format("为中文名称%s添加别名%s失败", chineseName, alias)), "添加别名失败");
                return -1;
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            MyExceptionHandler.error(context, e, "拼音转换失败");
            return -2;
        }
    }

    private static MutableText genDescription(ItemInfo item) {
        // 编号 + 中文名称
        MutableText text = Text.literal(String.format("%s id: %d 位置: ", item.chineseName, item.id.id));
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

}
