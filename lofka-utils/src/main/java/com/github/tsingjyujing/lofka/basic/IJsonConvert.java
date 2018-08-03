package com.github.tsingjyujing.lofka.basic;

import org.bson.Document;

/**
 * @param <RawType> 原始类型
 * @author yuanyifan
 */
public interface IJsonConvert<RawType> {
    /**
     * 将来源类型的转换为
     *
     * @param rawData
     * @return
     */
    Document toDocument(RawType rawData);
}
