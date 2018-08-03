package com.github.tsingjyujing.lofka.model;

/**
 * Author: yuanyifan
 * Created by: ModelGenerator on 18-6-29
 */
public class LocationInfo {
    private int line;
    private String filename;
    private String method;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}