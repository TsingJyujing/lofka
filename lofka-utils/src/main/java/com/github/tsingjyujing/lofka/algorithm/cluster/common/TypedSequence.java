package com.github.tsingjyujing.lofka.algorithm.cluster.common;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Typed Sequence which can be compared
 *
 * @param <T>
 */
public class TypedSequence<T extends Comparable<T>> implements Comparable<TypedSequence<T>>, Iterable<T> {

    private final ArrayList<T> data;

    public TypedSequence(Iterable<T> data) {
        this.data = Lists.newArrayList(data);
    }

    public TypedSequence(T... data) {
        this.data = Lists.newArrayList(data);
    }

    /**
     * Get element by index
     *
     * @param i index
     * @return
     */
    public T get(int i) {
        return data.get(i);
    }

    /**
     * Sequence size
     *
     * @return
     */
    public int size() {
        return data.size();
    }

    public TypedSequence<T> subSequence(int startIndex) {
        return subSequence(startIndex, size());
    }

    public TypedSequence<T> subSequence(int startIndex, int endIndex) {
        return new TypedSequence<T>(data.subList(startIndex, endIndex));
    }

    public int indexOf(TypedSequence<T> seq) {
        int lastIndex = size() - seq.size();
        for (int i = 0; i <= lastIndex; i++) {
            for (int j = 0; j < seq.size(); j++) {
                if (!get(i + j).equals(seq.get(j))) {
                    break;
                } else {
                    if (j == (seq.size() - 1)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public boolean contains(TypedSequence<T> seq) {
        if (seq.size() == 0) {
            return true;
        }
        return indexOf(seq) >= 0;
    }

    @Override
    public String toString() {
        return Joiner.on("").join(data);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (T datum : data) {
            hash ^= datum.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypedSequence) {
            final TypedSequence seq = (TypedSequence) obj;
            if (data.size() != seq.size()) {
                return false;
            }
            for (int i = 0; i < data.size(); i++) {
                if (!data.get(i).equals(seq.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    @Override
    public int compareTo(TypedSequence<T> o) {
        int compareSize = Math.min(size(), o.size());
        for (int i = 0; i < compareSize; i++) {
            int result = get(i).compareTo(o.get(i));
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(size(), o.size());
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }
}
