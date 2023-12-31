package lyzzcw.work.rpc.provider;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.provider.common.scanner.RpcServiceScanner;
import lyzzcw.work.rpc.provider.common.server.base.BaseServer;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/3 9:38
 * Description: 以Java原生方式启动启动Rpc
 */
@Slf4j
public class RpcSingleServer extends BaseServer {

    public RpcSingleServer(String serverAddress,
                           String serverRegistryAddress,
                           String registryAddress,
                           String registryType,
                           String registryLoadBalanceType,
                           String scanPackage,
                           String reflectType,
                           int heartbeatInterval,
                           int scanNotActiveChannelInterval,
                           boolean enableResultCache,
                           int resultCacheExpire,
                           int corePoolSize,
                           int maximumPoolSize,
                           String flowType,
                           int maxConnections,
                           String disuseStrategyType,
                           boolean enableBuffer,
                           int bufferSize,
                           boolean enableRateLimiter,
                           String rateLimiterType,
                           int permits,
                           int milliSeconds,
                           String rateLimiterFailStrategy,
                           boolean enableFusing,
                           String fusingType,
                           double totalFailure,
                           int fusingMilliSeconds,
                           String exceptionPostProcessorType) {
        //调用父类构造方法
        super(serverAddress,
                serverRegistryAddress,
                registryAddress,
                registryType,
                registryLoadBalanceType,
                reflectType,
                heartbeatInterval,
                scanNotActiveChannelInterval,
                enableResultCache,
                resultCacheExpire,
                corePoolSize,
                maximumPoolSize,
                flowType,
                maxConnections,
                disuseStrategyType,
                enableBuffer,
                bufferSize,
                enableRateLimiter,
                rateLimiterType,
                permits,
                milliSeconds,
                rateLimiterFailStrategy,
                enableFusing,
                fusingType,
                totalFailure,
                fusingMilliSeconds,
                exceptionPostProcessorType);
        try {
            this.handlerMap =
                    RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService(
                            this.serverRegistryHost,this.serverRegistryPort,scanPackage,this.registryService);
            if(log.isDebugEnabled()){
                log.debug("RpcSingleServer handlerMap: {}",handlerMap);
            }
        }catch (Exception e) {
            log.error("Rpc server init error",e);
        }
    }
}
