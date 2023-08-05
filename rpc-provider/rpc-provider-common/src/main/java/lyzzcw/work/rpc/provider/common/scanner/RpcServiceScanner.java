package lyzzcw.work.rpc.provider.common.scanner;

import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.rpc.annotation.RpcService;
import lyzzcw.work.rpc.common.helper.RpcServiceHelper;
import lyzzcw.work.rpc.common.scanner.ClassScanner;
import lyzzcw.work.rpc.protocol.meta.ServiceMeta;
import lyzzcw.work.rpc.registry.api.RegistryService;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/3 9:11
 * Description: @RpcService注解扫描器
 */
@Slf4j
public class RpcServiceScanner extends ClassScanner {
    /**
     * 扫描指定包下的类，并筛选使用@RpcService注解标注的类
     */
    public static Map<String,Object> doScannerWithRpcServiceAnnotationFilterAndRegistryService(
            String host, int port, String scanPackage, RegistryService registryService) throws Exception {
        Map<String,Object> handlerMap = new HashMap<String,Object>();
        List<String> classNameList = getClassNameList(scanPackage,true);
        if(CollectionUtils.isEmpty(classNameList)){
            return handlerMap;
        }
        classNameList.stream().forEach(className -> {
            try{
                Class<?> clazz = Class.forName(className);
                RpcService rpcService = clazz.getAnnotation(RpcService.class);
                if(null != rpcService){
                    //优先使用interfaceClass, interfaceClass的name为空，再使用interfaceClassName
                    String serviceName = getServiceName(rpcService);
                    String serviceVersion = rpcService.version();
                    String serviceGroup = rpcService.group();
                    //向注册中心注册元数据
                    ServiceMeta serviceMeta = new ServiceMeta(serviceName,serviceVersion,serviceGroup,host,port);
                    registryService.register(serviceMeta);
                    //handlerMap的key先简单存储为serviceName+version+group
                    handlerMap.put(RpcServiceHelper.buildServiceKey(serviceName, serviceVersion, serviceGroup), clazz.newInstance());
                }
            }catch (Exception e){
                log.error("scan classes throws exception",e);
            }
        });
        return handlerMap;
    }

    /**
     * 获取serviceName
     */
    private static String getServiceName(RpcService rpcService){
        //优先使用interfaceClass
        Class clazz = rpcService.interfaceClass();
        if (clazz == null || clazz == void.class){
            return rpcService.interfaceClassName();
        }
        String serviceName = clazz.getName();
        if (serviceName == null || serviceName.trim().isEmpty()){
            serviceName = rpcService.interfaceClassName();
        }
        return serviceName;
    }
}
