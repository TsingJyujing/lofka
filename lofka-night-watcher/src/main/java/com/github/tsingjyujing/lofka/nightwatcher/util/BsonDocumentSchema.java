package com.github.tsingjyujing.lofka.nightwatcher.util;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.bson.BSON;
import org.bson.BasicBSONObject;
import org.bson.Document;

import java.io.IOException;

/**
 * 以Bson方式传递Document的序列化反序列化
 */
public class BsonDocumentSchema implements DeserializationSchema<Document>, SerializationSchema<Document> {

    @Override
    public Document deserialize(byte[] message) throws IOException {
        return new Document(BSON.decode(message).toMap());
    }

    @Override
    public boolean isEndOfStream(Document nextElement) {
        return false;
    }

    @Override
    public byte[] serialize(Document element) {
        return BSON.encode(new BasicBSONObject(element));
    }

    @Override
    public TypeInformation<Document> getProducedType() {
        return BasicTypeInfo.getInfoFor(Document.class);
    }
}
