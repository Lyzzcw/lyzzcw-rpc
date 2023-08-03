package lyzzcw.work.rpc.protocol;

import lombok.Data;
import lyzzcw.work.rpc.protocol.header.RpcHeader;

import java.io.Serializable;

/**
 * @author lzy
 * @version 1.0.0
 * @description Rpc协议
 */
@Data
public class RpcProtocol<T> implements Serializable {
    private static final long serialVersionUID = 292789485166173277L;

    /**
     * 消息头
     */
    private RpcHeader header;
    /**
     * 消息体
     */
    private T body;

}
