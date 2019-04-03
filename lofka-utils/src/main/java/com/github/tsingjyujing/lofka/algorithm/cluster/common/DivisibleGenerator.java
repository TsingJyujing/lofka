package com.github.tsingjyujing.lofka.algorithm.cluster.common;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DivisibleGenerator<T extends Comparable<T>> {

    /**
     * @param minCommonSize
     * @param data
     * @return
     */
    public List<IDivisible<T>> analysisCommonStrings(int minCommonSize, Iterable<TypedSequence<T>> data) {
        List<IDivisible<T>> initList = Lists.newArrayList();
        initList.add(new DivisibleStringSet<>(minCommonSize, data));
        while (true) {
            List<IDivisible<T>> buffer = Lists.newArrayList();
            for (IDivisible<T> iDivisible : initList) {
                if (iDivisible.isDivisible()) {
                    buffer.addAll(iDivisible.divide());
                } else {
                    buffer.add(iDivisible);
                }
            }
            if (buffer.size() == initList.size()) {
                return buffer;
            } else {
                initList = buffer;
            }
        }
    }


    /**
     * @param minCommonSize
     * @param data
     * @return
     */
    public List<IDivisible<T>> analysisPrefixStrings(int minCommonSize, Iterable<TypedSequence<T>> data) {
        return new SamePrefixDivisibleStringSet<T>(data).divide();
    }

    /**
     * 根据标点符号为分隔生成数据集
     *
     * @param textArray 原始数据
     * @return
     */
    public static List<TypedSequence<String>> generateSequenceByComma(String... textArray) {
        Pattern pattern = Pattern.compile("[^0-9a-zA-Z]+|[0-9a-zA-Z]+");
        List<TypedSequence<String>> seqs = Lists.newArrayList();
        for (String data : textArray) {
            Matcher matcher = pattern.matcher(data);
            List<String> strs = Lists.newArrayList();
            while (matcher.find()) {
                strs.add(matcher.group());
            }
            seqs.add(new TypedSequence<String>(strs));
        }
        return seqs;
    }


    /**
     * 根据标点符号为分隔生成数据集
     *
     * @param textArray 原始数据
     * @return
     */
    public static List<TypedSequence<Character>> generateSequenceForEachChar(String...  textArray) {
        List<TypedSequence<Character>> result = Lists.newArrayList();
        for (String s : textArray) {
            final char[] a = s.toCharArray();
            final List<Character> l = Lists.newArrayList();
            for (char c : a) {
                l.add(c);
            }
            result.add(new TypedSequence<Character>(l));
        }
        return result;
    }

}
