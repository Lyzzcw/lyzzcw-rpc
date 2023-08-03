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
    //心跳消息
    HEARTBEAT(3),
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