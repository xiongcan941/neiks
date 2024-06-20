package com.bbs.cloud.user.exception;

import com.bbs.cloud.common.error.ExceptionCode;

public enum UserException implements ExceptionCode {

    RED_PACKET_GETED(300001, "已经参与过抢红包活动", "RED_PACKET_GETED"),

    RED_PACKET_BE_PLUNDERED(300002, "红包已抢完", "RED_PACKET_BE_PLUNDERED"),

    LUCKY_BAG_GETED(300003, "已经参与过抢福袋活动", "LUCKY_BAG_GETED"),

    LUCKY_BAG_BE_PLUNDERED(300004, "已经参与过抢福袋活动", "LUCKY_BAG_BE_PLUNDERED"),

    LUCKY_BAG__GETED(300005, "福袋已抢完", "LUCKY_BAG_BE_PLUNDERED"),

    SCORE_NOT_MEET(300006, "积分额度不足", "SCORE_NOT_MEET"),

    GOLD_NOT_MEET(300006, "金币额度不足", "GOLD_NOT_MEET"),

    SCORE_CONVERT_LUCKY_BAG_IS_NULL(300007, "福袋额度不足", "SCORE_CONVERT_LUCKY_BAG_IS_NULL"),



    SCORE_CONVERT_GOLD_PARAM_GOLD_INVALID(300008, "请填写有效金币值", "SCORE_CONVERT_GOLD_PARAM_GOLD_INVALID"),

    SCORE_CONVERT_GOLD_PARAM_ACTIVITY_ID_IS_NOT_NULL(300009, "活动ID不能为空", "SCORE_CONVERT_GOLD_PARAM_ACTIVITY_ID_IS_NOT_NULL"),

    SCORE_CONVERT_GOLD_PARAM_ACTIVITY_ID_INVALID(300010, "活动ID格式不正确", "SCORE_CONVERT_GOLD_PARAM_ACTIVITY_ID_INVALID"),



    BEFORE(301001, "活动金币扣减之前异常", "BEFORE"),

    CENTER(301002, "活动金币扣减后, 用户金币增加前发生异常", "CENTER"),

    AFTER(301003, "活动金币扣减后, 用户金币增加后发生异常", "CENTER"),

    ;

    private Integer code;

    private String message;

    private String name;

    UserException(Integer code, String message, String name) {
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
