package com.github.tsingjyujing.lofka.algorithm.cluster.bistring;

/**
 * 最长子序列计算器
 * 原作者：http://www.hankcs.com/program/algorithm/implementation-and-application-of-nlp-longest-common-subsequence-longest-common-subsequence-of-java.html
 */
public class LongestStringSubSequenceCalculator {

    public static int compute(char[] str1, char[] str2) {
        int substringLength1 = str1.length;
        int substringLength2 = str2.length;
        int[][] opt = new int[substringLength1 + 1][substringLength2 + 1];
        for (int i = substringLength1 - 1; i >= 0; i--) {
            for (int j = substringLength2 - 1; j >= 0; j--) {
                if (str1[i] == str2[j])
                    opt[i][j] = opt[i + 1][j + 1] + 1;// 状态转移方程
                else
                    opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);// 状态转移方程
            }
        }
        return opt[0][0];
    }

    /**
     * 计算最长子序列长度
     *
     * @param str1
     * @param str2
     * @return
     */
    public static int compute(String str1, String str2) {
        return compute(str1.toCharArray(), str2.toCharArray());
    }
}
