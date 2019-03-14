package com.github.tsingjyujing.lofka.algorithm.cluster;

import com.github.tsingjyujing.lofka.algorithm.cluster.bistring.LongestSubStringCalculator;
import com.github.tsingjyujing.lofka.algorithm.cluster.bistring.SubString;
import com.github.tsingjyujing.lofka.algorithm.cluster.common.*;
import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 增量式文本泛化器
 *
 * @author yuanyifan
 */
@Beta
public class IncrementalTextGeneralizer implements Serializable {

    /**
     * 最小公共长度
     */
    private final int minCommonSize;

    /**
     * 当前的结果集合
     */
    private final List<IDivisible<String>> pattern;

    /**
     * 根据泛化结果生成
     *
     * @param generalizeResult 泛化结果
     */
    public IncrementalTextGeneralizer(int minCommonSize, Iterable<IDivisible<String>> generalizeResult) {
        this.minCommonSize = minCommonSize;
        this.pattern = Lists.newArrayList(generalizeResult);
    }

    /**
     * 根据字符串生成
     *
     * @param minCommonSize 最小连续串大小（推荐值：1）
     * @param textArray     文本序列
     */
    public IncrementalTextGeneralizer(int minCommonSize, String... textArray) {
        this(
                minCommonSize,
                DivisibleGenerator.analysisCommonStrings(
                        minCommonSize,
                        DivisibleGenerator.generateSequenceByComma(
                                textArray
                        )
                )
        );
    }

    public List<IDivisible<String>> getPattern() {
        return pattern;
    }

    /**
     * 向当前的模型追加训练数据
     *
     * @param appendData 追加的数据
     * @throws Exception 追加失败
     */
    public void append(String appendData) throws Exception {
        synchronized (pattern) {
            if (!fetch(getPattern(), appendData)) {
                final List<IDivisible<String>> newPattern = Lists.newArrayList();
                for (IDivisible<String> subPattern : pattern) {
                    if (subPattern instanceof SingleString) {
                        final String patternInfo = subPattern.toString();
                        if (!appendData.contains(patternInfo)) {
                            final SubString ss = new LongestSubStringCalculator(patternInfo, appendData).getSubString1();
                            if (ss.getPrefixString().length() > 0) {
                                newPattern.add(new IndivisibleStringSet<String>(Lists.<TypedSequence<String>>newArrayList(new TypedSequence<String>(
                                        ss.getPrefixString()
                                ))));
                            }
                            newPattern.add(new SingleString<String>(Lists.<String>newArrayList(ss.toString())));
                            if (ss.getSuffixString().length() > 0) {
                                newPattern.add(new IndivisibleStringSet<String>(Lists.<TypedSequence<String>>newArrayList(new TypedSequence<String>(
                                        ss.getSuffixString()
                                ))));
                            }
                        } else {
                            newPattern.add(subPattern);
                        }
                    } else {
                        newPattern.add(subPattern);
                    }
                }
                boolean fetchResult = fetch(newPattern, appendData);
                if (fetchResult) {
                    pattern.clear();
                    pattern.addAll(newPattern);
                } else {
                    throw new RuntimeException("Can't append this log into current pattern.");
                }
            }
        }

    }


    /**
     * 匹配数据
     *
     * @param pattern 需要匹配的模式
     * @param data    待匹配的数据
     * @return
     */
    public static boolean fetch(List<IDivisible<String>> pattern, String data) {
        final StringBuilder regexp = new StringBuilder();
        for (IDivisible<String> subPattern : pattern) {
            if (subPattern instanceof SingleString) {
                regexp.append(escapeRegExp(subPattern.toString()));
            } else {
                regexp.append(".*?");
            }
        }
        return Pattern.compile(
                regexp.toString()
        ).matcher(
                data
        ).find();
    }

    /**
     * 正则表达式规避关键符号
     *
     * @param rawString 原始数据
     * @return
     */
    private static String escapeRegExp(CharSequence rawString) {
        final char[] keyCharsOfRegExp = "*.?+$^[](){}|\\/".toCharArray();
        Arrays.sort(keyCharsOfRegExp);
        int size = rawString.length();
        final StringBuilder sb = new StringBuilder(size * 2);
        for (int i = 0; i < size; i++) {
            final char c = rawString.charAt(i);
            int res = Arrays.binarySearch(keyCharsOfRegExp, c);
            if (res < 0) {
                sb.append(c);
            } else {
                sb.append('\\');
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder showString = new StringBuilder();
        for (IDivisible<String> subPattern : pattern) {
            if (subPattern instanceof SingleString) {
                showString.append(subPattern.toString());
            } else {
                showString.append('*');
            }
        }
        return showString.toString();
    }
}
