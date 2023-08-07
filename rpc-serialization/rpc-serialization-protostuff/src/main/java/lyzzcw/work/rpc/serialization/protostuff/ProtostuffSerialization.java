/**
 * Copyright 2020-9999 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lyzzcw.work.rpc.serialization.protostuff;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.common.exception.SerializerException;
import lyzzcw.work.rpc.serialization.api.Serialization;
import lyzzcw.work.rpc.spi.annotation.SPIClass;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lzy
 * @version 1.0.0
 * @description protostuff序列化
 */
@SPIClass
@Slf4j
public class ProtostuffSerialization implements Serialization {

    private Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private Objenesis objenesis = new ObjenesisStd(true);

    @SuppressWarnings("unchecked")
    private <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }

    /**
     * 序列化（对象 -> 字节数组）
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> byte[] serialize(T obj) {
        if(log.isDebugEnabled()){
            log.debug("execute protostuff serialize...");
        }
        if (obj == null){
            throw new SerializerException("serialize object is null");
        }
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化（字节数组 -> 对象）
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        if(log.isDebugEnabled()){
            log.debug("execute protostuff deserialize...");
        }
        if (data == null){
            throw new SerializerException("deserialize data is null");
        }
        try {
            T message = (T) objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }
}