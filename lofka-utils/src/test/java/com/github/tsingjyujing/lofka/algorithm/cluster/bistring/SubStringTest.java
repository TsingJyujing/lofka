package com.github.tsingjyujing.lofka.algorithm.cluster.bistring;

import org.junit.Assert;
import org.junit.Test;

public class SubStringTest {

    @Test
    public void simpleSplitTest() {
        String rawStr = "123456789";
        Suffix suffix = new Suffix(rawStr.toCharArray());
        suffix.setStartPos(3);
        SubString ss = new SubString(suffix, 3);
        Assert.assertEquals("Failed",ss.getPrefixString() + ss.toString() + ss.getSuffixString(), rawStr);
    }
}