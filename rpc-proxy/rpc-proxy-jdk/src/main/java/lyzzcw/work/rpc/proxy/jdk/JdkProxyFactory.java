package lyzzcw.work.rpc.proxy.jdk;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.proxy.api.BaseProxyFactory;
import lyzzcw.work.rpc.proxy.api.ProxyFactory;
import lyzzcw.work.rpc.spi.annotation.SPIClass;

import java.lang.reflect.Proxy;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/4 17:29
 * Description: JDK动态代理工厂类
 */
@SPIClass
@Slf4j
public class JdkProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    @Override
    public <T> T getProxy(Class<T> clazz) {
        if(log.isDebugEnabled()){
            log.debug("based on jdk dynamic proxy");
        }
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                objectProxy
        );
    }
}
