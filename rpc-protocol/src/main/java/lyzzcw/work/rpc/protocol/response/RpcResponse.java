package lyzzcw.work.rpc.protocol.response;


import lombok.Data;
import lyzzcw.work.rpc.protocol.base.RpcMessage;

/**
 * @author lzy
 * @version 1.0.0
 * @description RPC的响应类，对应的请求id在响应头中
 */
@Data
public class RpcResponse extends RpcMessage {
    private static final long serialVersionUID = 425335064405584525L;

    private String error;
    private Object result;

    public boolean isError() {
        return error != null;
    }

}