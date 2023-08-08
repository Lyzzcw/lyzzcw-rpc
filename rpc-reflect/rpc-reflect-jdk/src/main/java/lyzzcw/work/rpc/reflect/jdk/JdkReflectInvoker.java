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
package lyzzcw.work.rpc.reflect.jdk;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.reflect.api.ReflectInvoker;
import lyzzcw.work.rpc.spi.annotation.SPIClass;

import java.lang.reflect.Method;

/**
 * @author lzy
 * @version 1.0.0
 * @description JDK反射调用方法的类
 */
@SPIClass
@Slf4j
public class JdkReflectInvoker implements ReflectInvoker {


    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("use jdk reflect type invoke method...");
        }
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }
}
