package com.github.tsingjyujing.lofka.model;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * Author: yuanyifan
 * Created by: ModelGenerator on 18-6-29
 */
public class LoggerJsonModel {
    private String logger;
    private double timestamp;
    private String message;
    private String app_name;
    private LocationInfo location;
    private HostInfo host;
    private String level;
    private List<String> routers = Lists.newArrayList();
    private Map<String, Object> mdc = Maps.newHashMap();
    private ThrowableInfo throwable;
    private String thread;
    private static final Gson GSON = new Gson();

    public static Document getDocument(LoggerJsonModel model) {
        return Document.parse(GSON.toJson(model));
    }

    public String getLogger() {
        return logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public LocationInfo getLocation() {
        return location;
    }

    public void setLocation(LocationInfo location) {
        this.location = location;
    }

    public HostInfo getHost() {
        return host;
    }

    public void setHost(HostInfo host) {
        this.host = host;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<String> getRouters() {
        return routers;
    }

    public void setRouters(List<String> routers) {
        this.routers = routers;
    }

    public Map<String, Object> getMdc() {
        return mdc;
    }

    public void setMdc(Map<String, Object> mdc) {
        this.mdc = mdc;
    }

    public ThrowableInfo getThrowable() {
        return throwable;
    }

    public void setThrowable(ThrowableInfo throwable) {
        this.throwable = throwable;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }
}