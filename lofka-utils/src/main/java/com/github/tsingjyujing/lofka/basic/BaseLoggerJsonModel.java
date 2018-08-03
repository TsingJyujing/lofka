package com.github.tsingjyujing.lofka.basic;

import com.github.tsingjyujing.lofka.model.LoggerJsonModel;
import org.bson.Document;

public class BaseLoggerJsonModel implements IJsonConvert<LoggerJsonModel> {

    @Override
    public Document toDocument(LoggerJsonModel rawData) {
        return LoggerJsonModel.getDocument(rawData);
    }
}
