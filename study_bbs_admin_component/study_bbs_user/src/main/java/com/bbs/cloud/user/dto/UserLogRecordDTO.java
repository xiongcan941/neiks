package com.bbs.cloud.user.dto;

import com.bbs.cloud.common.util.CommonUtil;

import java.util.Date;

/**
 * 用户日志记录
 */
public class UserLogRecordDTO {

    private String id;

    private String userId;

    private Date createDate;

    public String message;

    public UserLogRecordDTO() {

    }

    public UserLogRecordDTO(String userId, String message) {
        this.userId = userId;
        this.message = message;
        this.createDate = new Date();
        this.id = CommonUtil.createUUID();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
