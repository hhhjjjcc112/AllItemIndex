package com.hjc.allitemindex.model;

import com.google.gson.annotations.SerializedName;
import net.minecraft.text.Text;

import java.util.*;
import java.util.stream.Collectors;

public class PinYin {
    @SerializedName("cn")
    public String chineseName;
    @SerializedName("pinyin")
    public String pinYin;

    public PinYin(String chineseName, String pinYin) {
        this.chineseName = chineseName;
        this.pinYin = pinYin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PinYin pinYin1)) return false;
        return Objects.equals(chineseName, pinYin1.chineseName) && Objects.equals(pinYin, pinYin1.pinYin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chineseName, pinYin);
    }

    /**
     * 获取中文词语的拼音全称表示
     * @return 中文词语的拼音全称表示
     */
    public Set<String> pinYin() {
        // 去除中间的_
        return Collections.singleton(pinYin.replace("-", ""));
    }

    private static final int CHINESE_MIN = 0x4e00;
    private static final int CHINESE_MAX = 0x9fa5;

    private static final Set<String> SPECIAL_CASES = new HashSet<>(List.of("ch", "zh", "sh"));

    /**
     * 获取拼音对应的所有拼音缩写
     * @return 拼音对应的所有拼音缩写
     */
    public Set<String> pinYinAbbr() {
        Set<StringBuilder> pinYinAbbr = new HashSet<>();
        pinYinAbbr.add(new StringBuilder());
        int len = chineseName.length();
        String[] subs = pinYin.split("-");
        if(subs.length != len) {
            throw new IllegalStateException(Text.translatable("pinyin.chineseAndPinyinNotMatch", chineseName, pinYin).getString());
        }
        for(int i = 0; i < len; i++) {
            int c = chineseName.charAt(i);
            // 对应位置上是中文字符
            if(CHINESE_MIN <= c && c <= CHINESE_MAX) {
                String sub = subs[i];
                String sub2 = sub.substring(0, 2);
                // 是zh, sh, ch的一种
                if(len > 2 && SPECIAL_CASES.contains(sub2)) {
                    Set<StringBuilder> pinyinAbbr1 = new HashSet<>();
                    for(var builder : pinYinAbbr) {
                        // 产生分支
                        builder.append(sub.charAt(0));
                        pinyinAbbr1.add(new StringBuilder(builder).append(sub2));
                    }
                    pinYinAbbr.addAll(pinyinAbbr1);
                }
                else {
                    for(var builder : pinYinAbbr) {
                        builder.append(sub.charAt(0));
                    }
                }
            }
            else {
                // 不是中文字符直接添加
                for(var builder : pinYinAbbr) {
                    builder.append(c);
                }
            }
        }
        return pinYinAbbr.stream().map(StringBuilder::toString).collect(Collectors.toSet());
    }
}
