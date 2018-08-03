package com.github.tsingjyujing.lofka.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 压缩工具
 */
public class CompressUtil {

    public static String decompressUTF8String(InputStream inputStream) throws IOException {
        final GZIPInputStream in = new GZIPInputStream(inputStream);
        final byte[] b = new byte[1024];
        int temp = 0;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ((temp = in.read(b, 0, b.length)) != -1) {
            out.write(b, 0, temp);
        }
        out.flush();
        in.close();
        out.close();
        return out.toString("UTF-8");
    }

    public static String decompressUTF8String(byte[] data) throws IOException {
        return decompressUTF8String(new ByteArrayInputStream(data));
    }

    public static byte[] compressUTF8String(String data) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = null;
        try {
            gzos = new GZIPOutputStream(baos);
            gzos.write(data.getBytes("UTF-8"));
            gzos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzos != null) {
                try {
                    gzos.close();
                } catch (IOException ignore) {
                }
            }
        }
        return baos.toByteArray();
    }
}
