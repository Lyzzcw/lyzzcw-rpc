package lyzzcw.work.rpc.demo.api;

import lyzzcw.work.rpc.demo.entity.User;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/17 11:23
 * Description: demo服务接口
 */
public interface UserService {
    User getUserInfo(String username, long userId);
}
