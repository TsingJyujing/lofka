package com.github.tsingjyujing.lofka.algorithm.cluster;

import org.junit.Test;

import static org.junit.Assert.*;

public class IncrementalTextGeneralizerTest {

    @Test
    public void append() throws Exception{
        IncrementalTextGeneralizer itg = new IncrementalTextGeneralizer(1,"a=1 b=1 c=1 d=1");
        itg.append("a=2 b=1 c=1 d=1");
        itg.append("a=3 b=4 c=1 d=1");
        itg.append("a=3 b=5 c=6 d=1");
        System.out.println(itg.toString());
    }
}