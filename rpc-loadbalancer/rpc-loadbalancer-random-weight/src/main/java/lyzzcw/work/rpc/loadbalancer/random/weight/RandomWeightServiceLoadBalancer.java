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
package lyzzcw.work.rpc.loadbalancer.random.weight;


import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.loadbalancer.api.ServiceLoadBalancer;
import lyzzcw.work.rpc.spi.annotation.SPIClass;

import java.util.List;
import java.util.Random;

/**
 * @author lzy
 * @version 1.0.0
 * @description 加权随机
 */
@SPIClass
@Slf4j
public class RandomWeightServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    @Override
    public T select(List<T> servers, int hashCode) {
        if (log.isDebugEnabled()){
            log.info("Load balancing policy based on weighted random algorithm...");
        }
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        hashCode = Math.abs(hashCode);
        int count = hashCode % servers.size();
        if (count <= 1) {
            count = servers.size();
        }
        Random random = new Random();
        int index = random.nextInt(count);
        return servers.get(index);
    }
}
