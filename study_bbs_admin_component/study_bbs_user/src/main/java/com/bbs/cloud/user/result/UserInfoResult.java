package com.bbs.cloud.user.result;

import com.bbs.cloud.user.result.vo.GiftVO;

import java.util.List;

public class UserInfoResult {

    private Integer gold;

    private List<GiftVO> giftVOS;

    private Integer score;

    public Integer getGold() {
        return gold;
    }

    public void setGold(Integer gold) {
        this.gold = gold;
    }

    public List<GiftVO> getGiftVOS() {
        return giftVOS;
    }

    public void setGiftVOS(List<GiftVO> giftVOS) {
        this.giftVOS = giftVOS;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
