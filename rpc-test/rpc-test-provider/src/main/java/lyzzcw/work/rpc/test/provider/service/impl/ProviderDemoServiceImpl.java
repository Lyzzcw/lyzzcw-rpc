package lyzzcw.work.rpc.test.provider.service.impl;

import lyzzcw.work.rpc.annotation.RpcService;
import lyzzcw.work.rpc.test.api.DemoService;


/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/3 9:58
 * Description: No Description
 */
@RpcService(
        interfaceClass = DemoService.class,
        interfaceClassName = "lyzzcw.work.rpc.test.api.DemoService",
        version = "1.0.0",
        group = "lzy")
public class ProviderDemoServiceImpl implements DemoService {
    @Override
    public String hello(String name, Integer age) {
        return "success";
    }
}
