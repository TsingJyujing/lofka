package com.github.tsingjyujing.lofka.algorithm.cluster.bistring;

/**
 * 子字符串
 */
public class SubString extends Suffix {

    private int endPos;

    public SubString(Suffix suffix, int length) {
        super(suffix.getData());
        setStartPos(suffix.getStartPos());
        endPos = suffix.getStartPos() + length - 1;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    private boolean isStartEndValid() {
        return getStartPos() <= getEndPos();
    }

    public boolean isEndPosValid() {
        return (getEndPos() < 0 || getEndPos() >= getFullSize()) && isStartEndValid();
    }

    @Override
    public int getSize() {
        return getEndPos() - getStartPos();
    }

    @Override
    public boolean isStartPosValid() {
        return super.isStartPosValid() && isStartEndValid();
    }

    @Override
    public String toString() {
        if (isStartPosValid() && isEndPosValid()) {
            return null;
        } else {
            return new String(getData(), getStartPos(), getSize());
        }
    }
}