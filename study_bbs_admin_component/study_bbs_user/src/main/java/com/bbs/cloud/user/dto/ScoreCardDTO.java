package com.bbs.cloud.user.dto;

/**
 * 积分卡
 */
public class ScoreCardDTO {

    private String id;

    private String userId;

    /**
     * 积分
     */
    private Integer score;

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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
