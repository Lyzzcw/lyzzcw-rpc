package lyzzcw.work.rpc.demo.spring.annotation.consumer.service;

import lyzzcw.work.rpc.annotation.RpcReference;
import lyzzcw.work.rpc.demo.api.UserService;
import lyzzcw.work.rpc.demo.entity.User;
import org.springframework.stereotype.Service;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/17 16:25
 * Description: No Description
 */
@Service
public class UserServiceImpl {
    @RpcReference(registryType = "zookeeper",
            registryAddress = "127.0.0.1:2181",
            loadBalanceType = "robin",
            version = "2.0.0",
            group = "lzy",
            serializationType = "protostuff",
            proxy = "cglib",
            timeout = 30000,
            async = false,
            oneway = false)
    private UserService userService;

    public User getUser(String username,long userId) {
        return userService.getUserInfo(username,userId);
    }
}
