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
package lyzzcw.work.rpc.loadbalancer.round.robin.weight.enhanced;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.loadbalancer.base.BaseEnhancedServiceLoadBalancer;
import lyzzcw.work.rpc.protocol.meta.ServiceMeta;
import lyzzcw.work.rpc.spi.annotation.SPIClass;


import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lzy
 * @version 1.0.0
 * @description 增强型加权轮询负载均衡
 */
@SPIClass
@Slf4j
public class RobinWeightServiceEnhancedLoadBalancer extends BaseEnhancedServiceLoadBalancer {
    private volatile AtomicInteger atomicInteger = new AtomicInteger(0);
    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String ip) {
        if(log.isDebugEnabled()){
            log.debug("Load balancing strategy based on enhanced weighted round-robin algorithm...");
        }
        servers = this.getWeightServiceMetaList(servers);
        if (servers == null || servers.isEmpty()){
            return null;
        }
        int index = atomicInteger.incrementAndGet();
        if (index >= Integer.MAX_VALUE - 10000){
            atomicInteger.set(0);
        }
        return servers.get(index % servers.size());
    }
}
