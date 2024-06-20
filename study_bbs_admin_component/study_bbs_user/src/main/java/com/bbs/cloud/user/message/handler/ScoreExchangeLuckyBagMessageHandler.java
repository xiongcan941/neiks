package com.bbs.cloud.user.message.handler;

import com.bbs.cloud.common.enums.gift.GiftEnum;
import com.bbs.cloud.common.message.user.UserMessageDTO;
import com.bbs.cloud.common.message.user.dto.RobLuckyBagMessage;
import com.bbs.cloud.common.message.user.dto.ScoreConvertLuckyBagMessage;
import com.bbs.cloud.common.message.user.enums.UserMessageTypeEnum;
import com.bbs.cloud.common.util.CommonUtil;
import com.bbs.cloud.common.util.JsonUtils;
import com.bbs.cloud.user.contant.UserContant;
import com.bbs.cloud.user.dto.*;
import com.bbs.cloud.user.mapper.*;
import com.bbs.cloud.user.message.MessageHandler;
import com.bbs.cloud.user.message.MessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ScoreExchangeLuckyBagMessageHandler implements MessageHandler {

    final static Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

    @Autowired
    private BackpackMapper backpackMapper;

    @Autowired
    private BackpackGiftMapper backpackGiftMapper;

    @Autowired
    private UserLogRecordMapper userLogRecordMapper;

    @Autowired
    private LuckyBagRecordMapper luckyBagRecordMapper;

    @Autowired
    private ScoreCardMapper scoreCardMapper;


    @Override
    public void handler(UserMessageDTO userMessageDTO) {
        logger.info("用户通过积分兑换福袋产生的消息:{}", JsonUtils.objectToJson(userMessageDTO));
        try {
            String userId = userMessageDTO.getUserId();
            String message = userMessageDTO.getMessage();
            ScoreConvertLuckyBagMessage scoreConvertLuckyBagMessage = JsonUtils.jsonToPojo(message, ScoreConvertLuckyBagMessage.class);

            Integer giftType = scoreConvertLuckyBagMessage.getGiftType();

            ScoreCardDTO scoreCardDTO = scoreCardMapper.queryScoreCardDTO(userId);
            scoreCardDTO.setScore(scoreCardDTO.getScore() - scoreConvertLuckyBagMessage.getConsumeScore());

            //第一步：更新用户背包的礼物列表
            BackpackDTO backpackDTO = backpackMapper.queryBackpackDTO(userId);
            BackpackGiftDTO backpackGiftDTO = backpackGiftMapper.queryBackpackGiftDTO(backpackDTO.getId(), giftType);
            backpackGiftDTO.setAmount(backpackGiftDTO.getAmount() + UserContant.DEFAULT_ROB_LUCKY_GIFT_AMOUNT);
            backpackGiftMapper.updateBackpackGift(backpackGiftDTO);

            //第二步：添加领取福袋的记录
            LuckyBagRecordDTO luckyBagRecordDTO = new LuckyBagRecordDTO();
            luckyBagRecordDTO.setId(CommonUtil.createUUID());
            luckyBagRecordDTO.setCreateDate(new Date());
            luckyBagRecordDTO.setGiftType(giftType);
            luckyBagRecordDTO.setLuckyBagId(scoreConvertLuckyBagMessage.getLuckyBagId());
            luckyBagRecordDTO.setActivityId(scoreConvertLuckyBagMessage.getActivityId());
            luckyBagRecordDTO.setUserId(userId);
            luckyBagRecordMapper.insertLuckyBagRecordDTO(luckyBagRecordDTO);

            //第三部：添加用户操作日志
            UserLogRecordDTO userLogRecordDTO = new UserLogRecordDTO(userId,
                    "恭喜用户通过积分兑换福袋获得一个礼物 : " +
                            GiftEnum.getGiftsMap().get(giftType).getDesc());
            userLogRecordMapper.insertUserLogRecordDTO(userLogRecordDTO);

        } catch (Exception e) {
            logger.info("用户通过积分兑换福袋产生的消息异常:{}", JsonUtils.objectToJson(userMessageDTO));
            e.printStackTrace();
        }
    }

    @Override
    public String getMessageType() {
        return UserMessageTypeEnum.BBS_CLOUD_USER_SCORE_CONVERT_LUCKY_BAG.getType();
    }
}
