package lyzzcw.work.rpc.demo.spring.boot.provider.impl;


import lyzzcw.work.rpc.annotation.RpcService;
import lyzzcw.work.rpc.demo.api.UserService;
import lyzzcw.work.rpc.demo.entity.User;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/17 11:24
 * Description: No Description
 */
@RpcService(interfaceClass = UserService.class,
        interfaceClassName = "lyzzcw.work.rpc.demo.api.UserService",
        version = "2.0.0",
        group = "lzy",
        weight = 2)
public class UserServiceImpl implements UserService {
    @Override
    public User getUserInfo(String username, long userId) {
        //search from db
        return User.builder()
                .username(username)
                .password("FSGWQ%@!!%L-201@")
                .userId(userId)
                .age(29)
                .email("lyzzcw@aliyun.com")
                .build();
    }
}
