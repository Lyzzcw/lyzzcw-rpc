package lyzzcw.work.rpc.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/17 11:28
 * Description: No Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private String username;
    private String password;
    private Long userId;
    private Integer age;
    private String email;
}
