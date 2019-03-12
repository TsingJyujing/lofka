package com.github.tsingjyujing.lofka.algorithm.cluster.common;

import com.google.common.collect.Lists;
import org.junit.Test;

public class DivisibleStringSetTest {

    @Test
    public void divide() {

        for (IDivisible<String> iDivisible : new DivisibleStringSet<>(1, Lists.newArrayList(
                new TypedSequence<>("flink/", "jiadin", "1", "/FUCK"),
                new TypedSequence<>("flink/", "jiadin", "2", "/FUCK"),
                new TypedSequence<>("flink/", "jiaxin", "2", "/FUCK"),
                new TypedSequence<>("flink/", "jiaxin", "1", "/FUCK")
        )).divide()) {
            System.out.print(iDivisible);
        }
    }
}