package com.github.tsingjyujing.lofka.model;

import java.util.List;

/**
 * Author: yuanyifan
 * Created by: ModelGenerator on 18-6-29
 */
public class ThrowableInfo {
    private String message;
    private List<LocationInfo> stackTrace;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<LocationInfo> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(List<LocationInfo> stackTrace) {
        this.stackTrace = stackTrace;
    }
}