package lyzzcw.work.rpc.protocol.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lzy
 * @version 1.0.0
 * @description 协议的类型
 */
@Getter
@AllArgsConstructor
public enum RpcType {
    //请求消息
    REQUEST(1),
    //响应消息
    RESPONSE(2),
    //从服务消费者发起的心跳数据
    HEARTBEAT_CONSUMER_TO_PROVIDER_PING(3),
    //服务提供者响应服务消费者的心跳数据
    HEARTBEAT_PROVIDER_TO_CONSUMER_PONG(4),
    //从服务提供者发起的心跳数据
    HEARTBEAT_PROVIDER_TO_CONSUMER_PING(5),
    //服务消费者响应服务提供者的心跳数据
    HEARTBEAT_CONSUMER_TO_PROVIDER_PONG(6);
    ;

    private final int type;

    public static RpcType findByType(int type) {
        for (RpcType rpcType : RpcType.values()) {
            if (rpcType.getType() == type) {
                return rpcType;
            }
        }
        return null;
    }

}