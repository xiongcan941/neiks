package com.bbs.cloud.token.dto;

import com.bbs.cloud.common.enums.user.LoginTicketStatusEnum;

import java.util.Date;

/**
 * 用户令牌
 */
public class LoginTicketDTO {

    private String id;

    private String userId;
    /**
     * 令牌有效期
     */
    private Date expired;

    /**
     * {@link LoginTicketStatusEnum}
     */
    private Integer status;

    private String ticket;

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

    public Date getExpired() {
        return expired;
    }

    public void setExpired(Date expired) {
        this.expired = expired;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}
