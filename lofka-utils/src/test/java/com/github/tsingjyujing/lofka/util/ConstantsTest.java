package com.github.tsingjyujing.lofka.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConstantsTest {

    @Test
    public void urlProcessing() {
        assertEquals("Failed",
                "http://127.0.0.1:9301/api/route",
                Constants.urlProcessing("http://127.0.0.1:9301/api/route","api/route")
        );
        assertEquals("Failed",
                "http://127.0.0.1:9301/api/route",
                Constants.urlProcessing("http://127.0.0.1:9301","api/route")
        );
        assertEquals("Failed",
                "http://127.0.0.1:9301/api/route",
                Constants.urlProcessing("127.0.0.1:9301/api/route","api/route")
        );
        assertEquals("Failed",
                "http://127.0.0.1:9301/api/route",
                Constants.urlProcessing("127.0.0.1:9301","api/route")
        );

    }
}