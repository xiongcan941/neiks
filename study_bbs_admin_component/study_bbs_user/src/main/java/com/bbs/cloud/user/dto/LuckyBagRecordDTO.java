package com.bbs.cloud.user.dto;

import java.util.Date;

/**
 * 抢福袋记录
 */
public class LuckyBagRecordDTO {

    private String id;

    private String userId;

    private String activityId;

    private String luckyBagId;

    private Integer giftType;

    private Date createDate;

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

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getLuckyBagId() {
        return luckyBagId;
    }

    public void setLuckyBagId(String luckyBagId) {
        this.luckyBagId = luckyBagId;
    }

    public Integer getGiftType() {
        return giftType;
    }

    public void setGiftType(Integer giftType) {
        this.giftType = giftType;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
