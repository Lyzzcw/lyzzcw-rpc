/**
 * Copyright 2022-9999 the original author or authors.
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
package lyzzcw.work.rpc.disuse.fifo.strategy;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.disuse.api.DisuseStrategy;
import lyzzcw.work.rpc.disuse.api.connection.ConnectionInfo;
import lyzzcw.work.rpc.spi.annotation.SPIClass;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author lzy
 * @version 1.0.0
 * @description 判断被存储的时间，离目前最远的数据优先被淘汰。
 */
@SPIClass
@Slf4j
public class FifoDisuseStrategy implements DisuseStrategy {
    private final Comparator<ConnectionInfo> connectionTimeComparator = (o1, o2) -> {
        return o1.getConnectionTime() - o2.getConnectionTime() > 0 ? 1 : -1;
    };
    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        log.info("execute fifo disuse strategy...");
        if (connectionList.isEmpty()) return null;
        Collections.sort(connectionList, connectionTimeComparator);
        return connectionList.get(0);
    }
}
