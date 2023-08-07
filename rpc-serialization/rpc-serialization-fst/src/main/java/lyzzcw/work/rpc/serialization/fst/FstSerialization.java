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
package lyzzcw.work.rpc.serialization.fst;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.common.exception.SerializerException;
import lyzzcw.work.rpc.serialization.api.Serialization;
import lyzzcw.work.rpc.spi.annotation.SPIClass;
import org.nustaq.serialization.FSTConfiguration;

/**
 * @author lzy
 * @version 1.0.0
 * @description Fst Serialization
 */
@SPIClass
@Slf4j
public class FstSerialization implements Serialization {

    @Override
    public <T> byte[] serialize(T obj) {
        if(log.isDebugEnabled()){
            log.debug("execute fst serialize...");
        }
        if (obj == null){
            throw new SerializerException("serialize object is null");
        }
        FSTConfiguration conf = FSTConfiguration.getDefaultConfiguration();
        return conf.asByteArray(obj);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        if(log.isDebugEnabled()){
            log.debug("execute fst deserialize...");
        }
        if (data == null){
            throw new SerializerException("deserialize data is null");
        }
        FSTConfiguration conf = FSTConfiguration.getDefaultConfiguration();
        return (T) conf.asObject(data);
    }
}
