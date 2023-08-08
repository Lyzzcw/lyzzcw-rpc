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
package lyzzcw.work.rpc.proxy.javassist;

import javassist.util.proxy.MethodHandler;
import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.proxy.api.BaseProxyFactory;
import lyzzcw.work.rpc.proxy.api.ProxyFactory;
import lyzzcw.work.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author lzy
 * @version 1.0.0
 * @description Javassist动态代理
 */
@SPIClass
@Slf4j
public class JavassistProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    private javassist.util.proxy.ProxyFactory proxyFactory = new javassist.util.proxy.ProxyFactory();

    @Override
    public <T> T getProxy(Class<T> clazz) {
        try {
            if(log.isDebugEnabled()){
                log.debug("based on javassist dynamic proxy");
            }
            //设置代理类的父类
            proxyFactory.setInterfaces(new Class[]{clazz});
            proxyFactory.setHandler(new MethodHandler() {
                @Override
                public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                    return objectProxy.invoke(self, thisMethod, args);
                }
            });
            // 通过字节码技术动态创建子类实例
            return (T) proxyFactory.createClass().newInstance();
        }catch (Exception e){
            log.error("javassist proxy throws exception:", e);
        }
        return null;
    }
}
