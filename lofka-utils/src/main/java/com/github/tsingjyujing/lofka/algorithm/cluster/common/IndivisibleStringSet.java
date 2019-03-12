package com.github.tsingjyujing.lofka.algorithm.cluster.common;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;

/**
 * 不可分的集合
 */
public class IndivisibleStringSet<T extends Comparable<T>> implements IDivisible<T> {

    protected final List<TypedSequence<T>> data;

    public IndivisibleStringSet(Iterable<TypedSequence<T>> data) {
        this.data = Lists.newArrayList(
                Sets.newHashSet(
                        data
                )
        );
    }


    public TypedSequence<T> get(int i) {
        return data.get(i);
    }

    public int size() {
        return this.data.size();
    }

    @Override
    public boolean isDivisible() {
        return false;
    }

    @Override
    public List<IDivisible<T>> divide() {
        throw new RuntimeException("Not divisible.");
    }

    @Override
    public String toString() {
        return String.format(
                "{%s}", Joiner.on(", ").join(data)
        );
    }
}
