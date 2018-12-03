package com.github.tsingjyujing.lofka.persistence.util;

import com.github.tsingjyujing.lofka.persistence.basic.ILogReceiver;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ConfigLoaderTest {

    public static void main(String[] args) throws Exception{
        ArrayList<ILogReceiver> runnable = ConfigLoader.loadSource ();
        System.out.println(runnable.getClass().toString());
    }
}