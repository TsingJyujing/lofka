package com.github.tsingjyujing.lofka.nightwatcher.util;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.bson.Document;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 以JSON字符串方式传递Document的序列化反序列化
 */
public class JsonDocumentSchema implements DeserializationSchema<Document>, SerializationSchema<Document> {

    @Override
    public Document deserialize(byte[] message) throws IOException {
        try {
            return Document.parse(new String(message, Charset.defaultCharset()));
        } catch (Throwable ex) {
            // return null to skip unparsable element
            return null;
        }
    }

    @Override
    public boolean isEndOfStream(Document nextElement) {
        return false;
    }

    @Override
    public byte[] serialize(Document element) {
        return element.toJson().getBytes();
    }

    @Override
    public TypeInformation<Document> getProducedType() {
        return BasicTypeInfo.getInfoFor(Document.class);
    }
}
