package com.github.tsingjyujing.lofka.algorithm.cluster.common;

import java.util.List;

/**
 * 可分割的
 */
public interface IDivisible<T extends Comparable<T>> {
    /**
     * 是否还可分
     *
     * @return
     */
    boolean isDivisible();

    /**
     * 分得的结果
     *
     * @return
     */
    List<IDivisible<T>> divide();
}
