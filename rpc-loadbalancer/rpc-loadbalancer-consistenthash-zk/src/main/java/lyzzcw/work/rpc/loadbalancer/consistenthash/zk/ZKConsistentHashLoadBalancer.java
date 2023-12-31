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
package lyzzcw.work.rpc.loadbalancer.consistenthash.zk;



import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.loadbalancer.api.ServiceLoadBalancer;
import lyzzcw.work.rpc.spi.annotation.SPIClass;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author lzy
 * @version 1.0.0
 * @description 基于Zookeeper的一致性Hash
 */
@SPIClass
@Slf4j
public class ZKConsistentHashLoadBalancer<T> implements ServiceLoadBalancer<T> {

    private final static int VIRTUAL_NODE_SIZE = 10;
    private final static String VIRTUAL_NODE_SPLIT = "#";


    @Override
    public T select(List<T> servers, int hashCode, String ip) {
        if(log.isDebugEnabled()){
            log.debug("Load balancing strategy based on Zookeeper's consistent hash algorithm...");
        }
        TreeMap<Integer, T> ring = makeConsistentHashRing(servers);
        return allocateNode(ring, hashCode);
    }

    private T allocateNode(TreeMap<Integer, T> ring, int hashCode) {
        Map.Entry<Integer, T> entry = ring.ceilingEntry(hashCode);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        if (entry == null){
            throw new RuntimeException("not discover useful service, please register service in registry center.");
        }
        return entry.getValue();
    }

    private TreeMap<Integer, T> makeConsistentHashRing(List<T> servers) {
        TreeMap<Integer, T> ring = new TreeMap<>();
        for (T instance : servers) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                ring.put((buildServiceInstanceKey(instance) + VIRTUAL_NODE_SPLIT + i).hashCode(), instance);
            }
        }
        return ring;
    }

    private String buildServiceInstanceKey(T instance) {
        return Objects.toString(instance);
    }
}
