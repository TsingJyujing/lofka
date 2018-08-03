package com.github.tsingjyujing.lofka.util;

import org.junit.Assert;
import org.junit.Test;

public class CompressUtilTest {

    @Test
    public void testCompress() throws Exception {
        final String baseData = "QWERTYHJDSJBSDNMCBABSCJASHCC";
        StringBuilder dataBuilder = new StringBuilder(baseData);
        for (int i = 0; i < 100; i++) {
            dataBuilder.append(baseData);
        }
        String data = dataBuilder.toString();
        byte[] compressedData = CompressUtil.compressUTF8String(data);
        String dataRestore = CompressUtil.decompressUTF8String(compressedData);
        Assert.assertTrue("String not equals", data.equals(dataRestore));
        Assert.assertTrue("Data too large", compressedData.length < data.getBytes().length);
    }
}