package lyzzcw.work.rpc.codec;


import lyzzcw.work.rpc.serialization.api.Serialization;
import lyzzcw.work.rpc.serialization.jdk.JdkSerialization;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/3 14:18
 * Description: 实现编解码的接口，提供序列化和反序列化的默认方法
 */
public interface RpcCodec {

    default Serialization getJdkSerialization(){
        return new JdkSerialization();
    }

}
