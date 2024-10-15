package com.hjc.allitemindex.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PinYin {


    private static final int CHINESE_MIN = 0x4e00;
    private static final int CHINESE_MAX = 0x9fa5;
    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();
    static {
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    }

    /**
     * 获取中文词语的拼音全称表示
     * @param chineseName 中文词语
     * @return 中文词语的拼音全称表示
     */
    public static Set<String> toPinYinSet(String chineseName) throws BadHanyuPinyinOutputFormatCombination {
        final Set<StringBuilder> builders = new HashSet<>();
        // 先添加一个
        builders.add(new StringBuilder());
        char[] chars = chineseName.toCharArray();
        for(var ch: chars) {
            if(CHINESE_MIN <= ch && ch <= CHINESE_MAX) {
                String[] possible = PinyinHelper.toHanyuPinyinStringArray(ch, FORMAT);
                if(possible.length > 1) {
                    Set<StringBuilder> newBuilders = new HashSet<>();
                    for(var builder: builders) {
                        for(int i = 1;i < possible.length;i++) {
                            // 第二个结果及之后的结果, 都需要创建新的StringBuilder
                            newBuilders.add(new StringBuilder(builder).append(possible[i]));
                        }
                        // 对于第一个结果, 直接加入到原StringBuilder上, 不再创建新的StringBuilder
                        builder.append(possible[0]);
                    }
                    builders.addAll(newBuilders);
                }
                else {
                    for(var builder: builders) {
                        builder.append(possible[0]);
                    }
                }
            }
            else {
                for(var builder: builders) {
                    builder.append(ch);
                }
            }
        }
        return builders.stream().map(StringBuilder::toString).collect(Collectors.toSet());
    }

    private static final Set<String> SPECIAL_CASES = new HashSet<>(List.of("ch", "zh", "sh"));

    /**
     * 获取中文词语的拼音缩写表示
     * @param chineseName 中文词语
     * @return 中文词语的拼音缩写表示
     */
    public static Set<String> toPinYinAbbrSet(String chineseName) throws BadHanyuPinyinOutputFormatCombination {
        Set<StringBuilder> builders = new HashSet<>();
        // 先添加一个
        builders.add(new StringBuilder());
        char[] chars = chineseName.toCharArray();
        for(var ch: chars) {
            if(CHINESE_MIN <= ch && ch <= CHINESE_MAX) {
                String[] possible = PinyinHelper.toHanyuPinyinStringArray(ch, FORMAT);
                Set<StringBuilder> newBuilders = new HashSet<>();
                for(var pinyin: possible) {
                    if(pinyin.length() > 2) {
                        String sub2 = pinyin.substring(0, 2);
                        if(SPECIAL_CASES.contains(sub2)) {
                            for(var builder: builders) {
                                newBuilders.add(new StringBuilder(builder).append(sub2));
                            }
                        }
                    }
                    for (var builder: builders) {
                        newBuilders.add(new StringBuilder(builder).append(pinyin.charAt(0)));
                    }
                }
                builders = newBuilders;
            }
            else {
                for(var builder: builders) {
                    builder.append(ch);
                }
            }
        }
        return builders.stream().map(StringBuilder::toString).collect(Collectors.toSet());
    }
}
