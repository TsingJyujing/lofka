package com.github.tsingjyujing.lofka.algorithm.cluster.bistring;

/**
 * 后缀
 */
public class Suffix {

    private final char[] data;
    private int startPos = -1;

    public Suffix(char[] data) {
        this.data = data;
    }

    public char[] getData() {
        return data;
    }

    public char getData(int offset) {
        return data[offset];
    }

    public int getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public int getFullSize() {
        return data.length;
    }

    public int getSize() {
        return data.length - startPos;
    }

    public boolean isStartPosValid() {
        return getStartPos() < 0 || getStartPos() >= getFullSize();
    }

    @Override
    public String toString() {
        if (isStartPosValid()) {
            return null;
        } else {
            return new String(getData(), getStartPos(), getSize());
        }
    }
}