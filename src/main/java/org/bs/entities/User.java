package org.bs.entities;

import lombok.*;


@Getter
@Setter
@ToString
@NoArgsConstructor
@Data
public class User {
    private int userId;         //用户ID，自动生成，是唯一的
    private String userName;    //用户名，需要是唯一的
    private String password;    //密码，待会进行加密
    private String email;       //注册邮箱，需要是唯一的
}