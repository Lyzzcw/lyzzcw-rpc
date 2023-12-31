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
package lyzzcw.work.rpc.loadbalancer.least.connections;


import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.loadbalancer.api.ServiceLoadBalancer;
import lyzzcw.work.rpc.loadbalancer.context.ConnectionsContext;
import lyzzcw.work.rpc.protocol.meta.ServiceMeta;
import lyzzcw.work.rpc.spi.annotation.SPIClass;

import java.util.List;

/**
 * @author lzy
 * @version 1.0.0
 * @description 基于最少连接数的负载均衡策略
 */
@SPIClass
@Slf4j
public class LeastConnectionsServiceLoadBalancer implements ServiceLoadBalancer<ServiceMeta> {
    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String ip) {
        if(log.isDebugEnabled()){
            log.debug("A load balancing policy based on the least number of connections");
        }
        if (servers == null || servers.isEmpty()){
            return null;
        }
        ServiceMeta serviceMeta = this.getNullServiceMeta(servers);
        if (serviceMeta == null){
            serviceMeta = this.getServiceMeta(servers);
        }
        return serviceMeta;
    }

    private ServiceMeta getServiceMeta(List<ServiceMeta> servers) {
        ServiceMeta serviceMeta = servers.get(0);
        Integer serviceMetaCount = ConnectionsContext.getValue(serviceMeta);
        for (int i = 1; i < servers.size(); i++){
            ServiceMeta meta = servers.get(i);
            Integer metaCount = ConnectionsContext.getValue(meta);
            if (serviceMetaCount > metaCount){
                serviceMetaCount = metaCount;
                serviceMeta = meta;
            }
        }
        return serviceMeta;
    }

    //获取服务元数据列表中连接数为空的元数据，说明没有连接
    private ServiceMeta getNullServiceMeta(List<ServiceMeta> servers){
        for (int i = 0; i < servers.size(); i++){
            ServiceMeta serviceMeta = servers.get(i);
            if (ConnectionsContext.getValue(serviceMeta) == null){
                return serviceMeta;
            }
        }
        return null;
    }
}
