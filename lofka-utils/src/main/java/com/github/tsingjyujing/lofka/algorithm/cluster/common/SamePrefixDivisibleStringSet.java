package com.github.tsingjyujing.lofka.algorithm.cluster.common;

import com.google.common.collect.Lists;

import java.util.List;

public class SamePrefixDivisibleStringSet<T extends Comparable<T>> extends IndivisibleStringSet<T> {
    private final int commonPrefixSize;

    public SamePrefixDivisibleStringSet(Iterable<TypedSequence<T>> data) {
        super(data);
        int commonOffset = 0;
        int minSize = Integer.MAX_VALUE;
        for (int i = 0; i < size(); i++) {
            minSize = Math.min(minSize, get(i).size());
        }
        for (; commonOffset < minSize; commonOffset++) {
            int k = 1;
            for (; k < size(); k++) {
                if (!get(0).get(commonOffset).equals(get(k).get(commonOffset))) {
                    break;
                }
            }
            if (k != size()) {
                break;
            }
        }
        commonPrefixSize = commonOffset;
    }

    @Override
    public boolean isDivisible() {
        return true;
    }

    @Override
    public List<IDivisible<T>> divide() {
        final List<IDivisible<T>> result = Lists.<IDivisible<T>>newArrayList(
                new SingleString<T>(get(0).subSequence(0, commonPrefixSize))
        );
        final List<TypedSequence<T>> suffixs = Lists.newArrayList();
        for (int i = 0; i < size(); i++) {
            suffixs.add(get(i).subSequence(commonPrefixSize));
        }
        result.add(new IndivisibleStringSet<T>(suffixs));
        return result;
    }
}
