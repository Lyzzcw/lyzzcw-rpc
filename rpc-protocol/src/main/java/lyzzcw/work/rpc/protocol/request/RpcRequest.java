package lyzzcw.work.rpc.protocol.request;


import lombok.Data;
import lyzzcw.work.rpc.protocol.base.RpcMessage;

/**
 * @author lzy
 * @version 1.0.0
 * @description Rpc请求封装类，对应的请求id在消息头中
 */
@Data
public class RpcRequest extends RpcMessage {
    private static final long serialVersionUID = 5555776886650396129L;

    /**
     * 类名称
     */
    private String className;
    /**
     * 方法名称
     */
    private String methodName;
    /**
     * 参数类型数组
     */
    private Class<?>[] parameterTypes;
    /**
     * 参数数组
     */
    private Object[] parameters;
    /**
     * 版本号
     */
    private String version;
    /**
     * 服务分组
     */
    private String group;

}
