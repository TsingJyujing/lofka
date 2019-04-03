package com.github.tsingjyujing.lofka.algorithm.cluster;

import org.junit.Test;

public class IncrementalTextGeneralizerTest {

    @Test
    public void append() throws Exception {
        String initData = "a=1 b=1 c=1 d=1";
        String[] appendDataSet = new String[]{
                "a=2 b=1 c=1 d=1",
                "a=3 b=4 c=1 d=1",
                "a=3 b=5 c=6 d=1"
        };
        IncrementalTextGeneralizer itg = new IncrementalTextGeneralizer(1, 1, initData);
        System.out.printf("Generalized=%s\n", itg);
        for (String s : appendDataSet) {
            itg.append(s);
            System.out.printf("\nData append: %s\nGeneralized=%s\n", s, itg);
        }

    }
}