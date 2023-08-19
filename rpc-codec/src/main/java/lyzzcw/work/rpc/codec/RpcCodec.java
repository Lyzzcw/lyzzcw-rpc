package lyzzcw.work.rpc.codec;


import lyzzcw.work.rpc.flow.processor.FlowPostProcessor;
import lyzzcw.work.rpc.protocol.header.RpcHeader;
import lyzzcw.work.rpc.serialization.api.Serialization;
import lyzzcw.work.rpc.serialization.jdk.JdkSerialization;
import lyzzcw.work.rpc.spi.loader.ExtensionLoader;
import lyzzcw.work.rpc.threadpool.FlowPostProcessorThreadPool;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/3 14:18
 * Description: 实现编解码的接口，提供序列化和反序列化的默认方法
 */
public interface RpcCodec {

    /**
     * 根据serializationType通过SPI获取序列化句柄
     * @param serializationType 序列化方式
     * @return Serialization对象
     */
    default Serialization getSerialization(String serializationType){
        return ExtensionLoader.getExtension(Serialization.class, serializationType);
    }

    /**
     * 异步调用流控分析后置处理器
     * @param postProcessor
     * @param header
     */
    default void postFlowProcessor(FlowPostProcessor postProcessor, RpcHeader header){
        FlowPostProcessorThreadPool.submit(() -> {
            postProcessor.postRpcHeaderProcessor(header);
        });
    }

}
