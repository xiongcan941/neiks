package com.bbs.cloud.token.exception;

import com.bbs.cloud.common.error.ExceptionCode;

public enum TokenException implements ExceptionCode {

    USERNAME_IS_NOT_NULL(500001, "用户名不能为空", "USERNAME_IS_NOT_NULL"),

    PASSWORD_IS_NOT_NULL(500002, "密码不能为空", "USER_NAME_IS_NOT_NULL"),

    USERNAME_IS_EXIST(500003, "用户名已存在", "USERNAME_IS_EXIST"),

    USERNAME_IS_NOT_EXIST(500004, "用户名不存在", "USERNAME_IS_NOT_EXIST"),

    PASSWORD_NOT_TRUE(500005, "密码不正确", "PASSWORD_NOT_TRUE")

    ;


    private Integer code;

    private String message;

    private String name;

    TokenException(Integer code, String message, String name) {
        this.code = code;
        this.message = message;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
