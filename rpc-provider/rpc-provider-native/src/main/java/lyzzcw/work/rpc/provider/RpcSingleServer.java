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

    public RpcSingleServer(String serverAddress,String scanPackage,String reflectType) {
        //调用父类构造方法
        super(serverAddress,reflectType);
        try {
            this.handlerMap = RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService(scanPackage);
            if(log.isDebugEnabled()){
                log.debug("RpcSingleServer handlerMap: {}",handlerMap);
            }
        }catch (Exception e) {
            log.error("Rpc server init error",e);
        }
    }
}
