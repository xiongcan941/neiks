package com.bbs.cloud.user.dto;

/**
 * 背包礼物
 */
public class BackpackGiftDTO {

    private String id;

    /**
     * 所属背包ID
     */
    private String backpackId;

    /**
     * 礼物类型
     * {@link com.bbs.cloud.common.enums.gift.GiftEnum}
     */
    private Integer giftType;

    /**
     * 礼物数量
     */
    private Integer amount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBackpackId() {
        return backpackId;
    }

    public void setBackpackId(String backpackId) {
        this.backpackId = backpackId;
    }

    public Integer getGiftType() {
        return giftType;
    }

    public void setGiftType(Integer giftType) {
        this.giftType = giftType;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
