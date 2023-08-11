package lyzzcw.work.rpc.common.helper;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/3 9:31
 * Description: RPC服务帮助类
 */
public class RpcServiceHelper {
    /**
     * 拼接字符串
     * @param serviceName 服务名称
     * @param serviceVersion 服务版本号
     * @param group 服务分组
     * @return 服务名称#服务版本号#服务分组
     */
    public static String buildServiceKey(String serviceName, String serviceVersion, String group) {
        return String.join("#", serviceName, serviceVersion, group);
    }

    /**
     * 拼接字符串
     * @param serviceName 服务名称
     * @param serviceVersion 服务版本号
     * @param group 服务分组
     * @return 服务名称#服务版本号#服务分组
     */
    public static String buildNacosServiceKey(String serviceName, String serviceVersion, String group) {
        return String.join("_", serviceName, serviceVersion, group);
    }
}
