package lyzzcw.work.rpc.demo.spring.boot.consumer.service;

import lyzzcw.work.rpc.demo.api.UserService;
import lyzzcw.work.rpc.demo.entity.User;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/21 20:47
 * Description: No Description
 */
public class FallBackService implements UserService {
    @Override
    public User getUserInfo(String username, long userId) {
        //quick fallback
        return User.builder()
                .username("fallback")
                .password("fallback")
                .userId(10086L)
                .age(35)
                .email("www.fallback.com")
                .build();
    }
}
