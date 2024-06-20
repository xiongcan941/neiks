package com.bbs.cloud.token.controller;

import com.bbs.cloud.common.result.HttpResult;
import com.bbs.cloud.token.param.LoginParam;
import com.bbs.cloud.token.param.RegisterParam;
import com.bbs.cloud.token.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("token")
public class TokenController {

    @Autowired
    private TokenService tokenService;


    @PostMapping("/register")
    public HttpResult register(@RequestBody RegisterParam param) {

        return tokenService.register(param);
    }

    @PostMapping("/login")
    public HttpResult login(@RequestBody LoginParam param) {

        return tokenService.login(param);
    }

    @PostMapping("/logout")
    public HttpResult logout() {

        return tokenService.logout();
    }

}
