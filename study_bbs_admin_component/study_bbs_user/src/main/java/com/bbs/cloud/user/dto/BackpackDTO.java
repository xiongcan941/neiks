package com.bbs.cloud.user.dto;

/**
 * 背包
 */
public class BackpackDTO {

    private String id;

    private String userId;

    /**
     * 背包总值-只计算现金
     */
    private Integer gold;

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

    public Integer getGold() {
        return gold;
    }

    public void setGold(Integer gold) {
        this.gold = gold;
    }
}
