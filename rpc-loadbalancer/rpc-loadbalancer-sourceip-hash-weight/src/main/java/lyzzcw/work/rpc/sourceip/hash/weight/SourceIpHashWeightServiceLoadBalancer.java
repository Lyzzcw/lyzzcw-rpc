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
package lyzzcw.work.rpc.sourceip.hash.weight;


import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.loadbalancer.api.ServiceLoadBalancer;
import lyzzcw.work.rpc.spi.annotation.SPIClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author lzy
 * @version 1.0.0
 * @description 基于源IP地址加权Hash的负载均衡策略
 */
@SPIClass
@Slf4j
public class SourceIpHashWeightServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    private final Logger logger = LoggerFactory.getLogger(SourceIpHashWeightServiceLoadBalancer.class);
    @Override
    public T select(List<T> servers, int hashCode, String sourceIp) {
        if(log.isDebugEnabled()){
            logger.debug("Load balancing policy based on source IP address-weighted hash...");
        }
        if (servers == null || servers.isEmpty()){
            return null;
        }
        //传入的IP地址为空，则默认返回第一个服务实例m
        if (StringUtils.isEmpty(sourceIp)){
            return servers.get(0);
        }
        int count = Math.abs(hashCode) % servers.size();
        if (count == 0){
            count = servers.size();
        }
        int resultHashCode = Math.abs(sourceIp.hashCode() + hashCode);
        return servers.get(resultHashCode % count);
    }
}
