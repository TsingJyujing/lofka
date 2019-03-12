package com.github.tsingjyujing.lofka.algorithm.cluster.common;

import java.util.List;

public class SingleString<T extends Comparable<T>> implements IDivisible<T> {
    public TypedSequence<T> getData() {
        return data;
    }

    public SingleString(Iterable<T> data) {
        this.data = new TypedSequence<>(data);
    }

    private final TypedSequence<T> data;

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
        return getData().toString();
    }
}
