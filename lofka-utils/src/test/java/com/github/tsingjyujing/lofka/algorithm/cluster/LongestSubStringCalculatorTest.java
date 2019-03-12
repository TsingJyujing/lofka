package com.github.tsingjyujing.lofka.algorithm.cluster;

import com.github.tsingjyujing.lofka.algorithm.cluster.bistring.LongestSubStringCalculator;
import org.junit.Assert;
import org.junit.Test;

public class LongestSubStringCalculatorTest {

    @Test
    public void calculateSubString() {
        LongestSubStringCalculator cp = new LongestSubStringCalculator("ABC-AADAKJ-HJDGHAJKHDGJSYWHGDJ", "ABC-52465464-HJDGHAJKHDGJ56465464");
        System.out.printf("%s~%s", cp.getSubString1(), cp.getSubString2());
        Assert.assertEquals("Substrings should same", cp.getSubString1().toString(), cp.getSubString2().toString());
    }

}