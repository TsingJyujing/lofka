package com.github.tsingjyujing.lofka.algorithm.cluster.bistring;

/**
 * 最长子串计算器
 */
public class LongestSubStringCalculator {

    private final Suffix suffix1;
    private final Suffix suffix2;

    private int longest = 0;

    public LongestSubStringCalculator(String str1, String str2) {
        this(str1.toCharArray(), str2.toCharArray());
    }

    public LongestSubStringCalculator(char[] str1, char[] str2) {
        this(new Suffix(str1), new Suffix(str2));
    }

    public LongestSubStringCalculator(Suffix suffix1, Suffix suffix2) {
        this.suffix1 = suffix1;
        this.suffix2 = suffix2;
        updateParameters(suffix1, suffix2);
        updateParameters(suffix2, suffix1);
    }

    private void updateParameters(Suffix s1, Suffix s2) {
        for (int i = 0; i < s1.getFullSize(); ++i) {
            int m = i;
            int n = 0;
            int length = 0;
            while (m < s1.getFullSize() && n < s2.getFullSize()) {
                // ++comparisons;
                if (s1.getData(m) != s2.getData(n)) {
                    length = 0;
                } else {
                    ++length;
                    if (longest < length) {
                        longest = length;
                        s1.setStartPos(m - longest + 1);
                        s2.setStartPos(n - longest + 1);
                    }
                }
                ++m;
                ++n;
            }
        }
    }

    public SubString getSubString1() {
        return new SubString(suffix1, longest);
    }

    public SubString getSubString2() {
        return new SubString(suffix2, longest);
    }
}