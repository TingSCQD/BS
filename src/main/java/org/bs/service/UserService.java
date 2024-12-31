package org.bs.service;

import org.bs.entities.*;
import org.bs.utils.ApiResult;

import java.io.IOException;
import java.util.List;

public interface UserService {

    // 创建用户
    ApiResult openAccount(User user);

    // 注销账号
    ApiResult deleteAccount(Integer userId);

    // 修改密码
    ApiResult modifyPassword(Integer userId, String oldPassword, String newPassword);

    // 修改用户名
    ApiResult modifyUserName(Integer userId, String newUserName);

    // 用户登录
    ApiResult userLogin(String email, String password);

    ApiResult userSearch(String query);

     ApiResult discountRemind(Discount discount);


    ApiResult getHistory(int goodId);
}
