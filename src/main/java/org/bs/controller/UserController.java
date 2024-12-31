package org.bs.controller;

import  org.bs.entities.*;
import  org.bs.service.UserService;
import org.bs.utils.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.lang.String;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Map.Entry;
import org.bs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    // 开户
    @PostMapping("/openAccount")
    public ApiResult openAccount(@RequestBody Map<String,String> newAccountInfo) {
        User user = new User();
        user.setPassword(newAccountInfo.get("password"));
        user.setEmail(newAccountInfo.get("email"));
        user.setUserName(newAccountInfo.get("userName"));
        return userService.openAccount(user);
    }

    @PostMapping("/login")
    public ApiResult userLogin(@RequestBody Map<String,String> userLoginInfo) {
        String email = userLoginInfo.get("email");
        String password = userLoginInfo.get("password");
        System.out.println(email);
        System.out.println(password);
        return userService.userLogin(email, password);
    }
    @PostMapping("/userSearch")
    public ApiResult userSearch(@RequestBody Map<String,String> queryBody) {
        System.out.println(queryBody);
        String query = queryBody.get("query");
        return userService.userSearch(query);
    }

    @PostMapping("/setAlarm")
    public ApiResult setAlarm(@RequestBody Map<String,Integer> alarmInfo) {
        Discount discount = new Discount();
        discount.setUser_id(alarmInfo.get("user_id"));
        discount.setGood_id(alarmInfo.get("good_id"));
        discount.setRemind_price(alarmInfo.get("remind_price"));
        return userService.discountRemind(discount);
    }

    @PostMapping("/showPriceHis")
    public ApiResult showPriceHis(@RequestBody Map<String,Integer>  queryBody) {
        return userService.getHistory(queryBody.get("goodId"));
    }
}
