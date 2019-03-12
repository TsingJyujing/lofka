package com.github.tsingjyujing.lofka.algorithm.cluster.common;


import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class DivisibleStringSet<T extends Comparable<T>> extends IndivisibleStringSet<T> {
    private final TypedSequence<T> commonData;
    private final int minCommonSize;

    public DivisibleStringSet(int minCommonSize, Iterable<TypedSequence<T>> strings) {
        super(strings);
        this.minCommonSize = minCommonSize;
        commonData = commonSubString(minCommonSize);
    }

    @Override
    public boolean isDivisible() {
        return true;
    }

    @Override
    public List<IDivisible<T>> divide() {
        if (commonData.size() <= 0) {
            return Lists.<IDivisible<T>>newArrayList(new IndivisibleStringSet<>(data));
        } else {
            final List<TypedSequence<T>> prefixSet = Lists.newArrayList();
            final List<TypedSequence<T>> suffixSet = Lists.newArrayList();

            for (int i = 0; i < size(); i++) {
                TypedSequence<T> currentString = get(i);
                final int indexOfStart = currentString.indexOf(commonData);
                prefixSet.add(currentString.subSequence(0, indexOfStart));
                suffixSet.add(currentString.subSequence(indexOfStart + commonData.size()));
            }

            final List<IDivisible<T>> result = Lists.newArrayList();
            result.addAll(
                    getDivisibleByStrings(prefixSet)
            );
            result.add(new SingleString<>(commonData));
            result.addAll(
                    getDivisibleByStrings(suffixSet)
            );
            return result;
        }
    }

    /**
     * 是否可分（可以找到大于minSubString的公共子串）
     * <p>
     * 非常暴力
     *
     * @param minCommonStringSize 最小子串长度
     * @return
     */
    private TypedSequence<T> commonSubString(int minCommonStringSize) {
        int n = data.size();
        TypedSequence<T> s = data.get(0);
        int len = s.size();
        TypedSequence<T> res = new TypedSequence<>(Lists.<T>newArrayList());


        for (int i = 0; i < len; i++) {
            for (int j = i + 1 + (minCommonStringSize - 1); j <= len; j++) {
                TypedSequence<T> stem = s.subSequence(i, j);
                int k = 1;
                for (; k < n; k++) {
                    if (!data.get(k).contains(stem)) {
                        break;
                    }
                }
                if (k == n && res.size() < stem.size()) {
                    res = stem;
                }
            }
        }
        return res;
    }

    /**
     * 根据数据类型判断何种类型的元素
     *
     * @param data
     * @return
     */
    public List<IDivisible<T>> getDivisibleByStrings(final List<TypedSequence<T>> data) {
        final ArrayList<IDivisible<T>> returnData = Lists.newArrayList();
        if (data.size() <= 0) {
            return returnData;
        }
        TypedSequence<T> firstData = data.get(0);
        int k = 1;
        for (; k < data.size(); k++) {
            if (!data.get(k).contains(firstData)) {
                break;
            }
        }
        if (k == data.size()) {
            if (firstData.size() > 0) {
                returnData.add(new SingleString<>(firstData));
            }
            return returnData;
        } else {
            // 预筛选机制
            for (TypedSequence<T> s1 : data) {
                if (s1.size() < this.minCommonSize) {
                    returnData.add(new IndivisibleStringSet<T>(data));
                    return returnData;
                }
            }
            returnData.add(new DivisibleStringSet<>(minCommonSize, data));
            return returnData;
        }
    }
}
