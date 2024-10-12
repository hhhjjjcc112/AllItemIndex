package com.hjc.allitemindex.algorithm;

public class Similarity {
    // 设定数组第二维的大小
    private static final int MAX_LEN = 1000;
    private static final int[][] dp = new int[2][MAX_LEN];

    /**
     * 认为插入，删除或替换一个字符为一次编辑，计算两个字符串的编辑距离
     * @param s1 第一个字符串
     * @param s2 第二个字符串
     * @return 两个字符串的编辑距离
     */
    public static int editDistance(String s1, String s2) {
        int len1 = s1.length(), len2 = s2.length();
        // 这里交替使用二维数组的第一行和第二行，互相更新
        int line1 = 0, line2 = 1, temp;
        // 初始化dp的第一行
        for(int j = 0; j < len2 + 1; j++) {
            dp[line1][j] = j;
        }
        for(int i = 1; i < len1 + 1; i++) {
            // 初始化dp第一列
            dp[line2][0] = i;
            for(int j = 1; j < len2 + 1; j++) {
                // 如果i-1与j-1字符相同，则dp[i][j] = dp[i - 1][j - 1]
                if(s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[line2][j] = dp[line1][j - 1];
                }
                else {
                    // dp[i][j] = min(dp[i - 1][j - 1] + 1, dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    dp[line2][j] = min(
                            dp[line1][j - 1] + 1,
                            dp[line2][j - 1] + 1,
                            dp[line1][j] + 1
                    );
                }
            }
            // 交换line1和line2
            temp = line1;
            line1 = line2;
            line2 = temp;

        }
        return dp[len1 % 2][len2];
    }

    private static <T extends Comparable<T>> T min(T a, T b, T c) {
        if(a.compareTo(b) > 0) {
            if(b.compareTo(c) > 0) {
                return c;
            }
            return b;
        }
        if(a.compareTo(c) > 0) {
            return c;
        }
        return a;
    }

    public static void main(String[] args) {
        System.out.println(editDistance("horse", "ros") + " differences");
    }
}
