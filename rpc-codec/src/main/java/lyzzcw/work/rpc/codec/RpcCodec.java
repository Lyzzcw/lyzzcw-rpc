package lyzzcw.work.rpc.codec;


import lyzzcw.work.rpc.serialization.api.Serialization;
import lyzzcw.work.rpc.serialization.jdk.JdkSerialization;
import lyzzcw.work.rpc.spi.loader.ExtensionLoader;

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

}
