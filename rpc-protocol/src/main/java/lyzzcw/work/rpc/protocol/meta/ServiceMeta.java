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
package lyzzcw.work.rpc.protocol.meta;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author lzy
 * @version 1.0.0
 * @description 服务元数据，注册到注册中心的元数据信息
 */
public class ServiceMeta implements Serializable {
    private static final long serialVersionUID = 6289735590272020366L;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务版本号
     */
    private String serviceVersion;

    /**
     * 服务地址
     */
    private String serviceAddr;

    /**
     * 服务端口
     */
    private int servicePort;

    /**
     * 服务分组
     */
    private String serviceGroup;

    /**
     * 服务权重
     */
    private int weight;

    public ServiceMeta() {
    }

    public ServiceMeta(String serviceName, String serviceVersion,
                       String serviceAddr, int servicePort,
                       String serviceGroup, int weight) {
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
        this.serviceAddr = serviceAddr;
        this.servicePort = servicePort;
        this.serviceGroup = serviceGroup;
        this.weight = weight;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getServiceAddr() {
        return serviceAddr;
    }

    public void setServiceAddr(String serviceAddr) {
        this.serviceAddr = serviceAddr;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceMeta that = (ServiceMeta) o;
        return servicePort == that.servicePort && weight == that.weight && Objects.equals(serviceName, that.serviceName) && Objects.equals(serviceVersion, that.serviceVersion) && Objects.equals(serviceAddr, that.serviceAddr) && Objects.equals(serviceGroup, that.serviceGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, serviceVersion, serviceAddr, servicePort, serviceGroup, weight);
    }

    @Override
    public String toString() {
        return "ServiceMeta{" +
                "serviceName='" + serviceName + '\'' +
                ", serviceVersion='" + serviceVersion + '\'' +
                ", serviceAddr='" + serviceAddr + '\'' +
                ", servicePort=" + servicePort +
                ", serviceGroup='" + serviceGroup + '\'' +
                ", weight=" + weight +
                '}';
    }
}
