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
package lyzzcw.work.rpc.loadbalancer.sourceip.hash.weight.enhanced;



import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.loadbalancer.base.BaseEnhancedServiceLoadBalancer;
import lyzzcw.work.rpc.protocol.meta.ServiceMeta;
import lyzzcw.work.rpc.spi.annotation.SPIClass;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author lzy
 * @version 1.0.0
 * @description 增强型基于权重的源IP地址Hash的负载均衡策略
 */
@SPIClass
@Slf4j
public class SourceIpHashWeightServiceEnhancedLoadBalancer extends BaseEnhancedServiceLoadBalancer {

    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String ip) {
        if(log.isDebugEnabled()){
            log.debug("Enhanced load-balancing policy based on weight-based hashing of source IP addresses...");
        }
        servers = this.getWeightServiceMetaList(servers);
        if (servers == null || servers.isEmpty()){
            return null;
        }
        //传入的IP地址为空，则默认返回第一个服务实例m
        if (StringUtils.isEmpty(ip)){
            return servers.get(0);
        }
        int resultHashCode = Math.abs(ip.hashCode() + hashCode);
        return servers.get(resultHashCode % servers.size());
    }
}
