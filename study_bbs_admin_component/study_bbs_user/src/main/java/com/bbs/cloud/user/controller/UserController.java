package com.bbs.cloud.user.controller;

import com.bbs.cloud.common.result.HttpResult;
import com.bbs.cloud.user.param.ScoreExchangeGoldParam;
import com.bbs.cloud.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/info/query")
    public HttpResult queryUserInfo() {

        return userService.queryUserInfo();
    }

    @PostMapping("/luckybag/rob")
    public HttpResult robLuckyBag() {

        return userService.robLuckyBag();
    }


    @PostMapping("/redpacket/rob")
    public HttpResult robRedpacket() {

        return userService.robRedpacket();
    }

    @PostMapping("/score/luckybag/convert")
    public HttpResult scoreExchangeLuckyBag() {

        return userService.scoreExchangeLuckyBag();
    }

    @PostMapping("/score/gold/convert")
    public HttpResult scoreExchangeGold(@RequestBody ScoreExchangeGoldParam param) {

        return userService.scoreExchangeGold(param);
    }

    @PostMapping("/recharge")
    public HttpResult recharge() {

        return null;
    }

}
