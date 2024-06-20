package com.bbs.cloud.user.message.handler;


import com.bbs.cloud.common.message.user.UserMessageDTO;
import com.bbs.cloud.common.message.user.dto.ScoreConvertMoneyMessage;
import com.bbs.cloud.common.message.user.enums.UserMessageTypeEnum;
import com.bbs.cloud.common.util.JsonUtils;
import com.bbs.cloud.user.dto.ActivityGoldDTO;
import com.bbs.cloud.user.dto.BackpackDTO;
import com.bbs.cloud.user.dto.ScoreCardDTO;
import com.bbs.cloud.user.dto.UserLogRecordDTO;
import com.bbs.cloud.user.mapper.ActivityGoldMapper;
import com.bbs.cloud.user.mapper.BackpackMapper;
import com.bbs.cloud.user.mapper.ScoreCardMapper;
import com.bbs.cloud.user.mapper.UserLogRecordMapper;
import com.bbs.cloud.user.message.MessageHandler;
import com.bbs.cloud.user.message.MessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScoreExchangeGoldMessageHandler implements MessageHandler {
    final static Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

    @Autowired
    private UserLogRecordMapper userLogRecordMapper;

    @Autowired
    private ScoreCardMapper scoreCardMapper;
    @Autowired
    private BackpackMapper backpackMapper;
    @Autowired
    private ActivityGoldMapper activityGoldMapper;

    @Override
    public void handler(UserMessageDTO userMessageDTO) {
        /**
         * 1.更新数据库中(activity_gold)金币的数量
         * 2.把积分兑换金币的行为记录到用户的日志中(user_log_record)
         * 3，更新数据库中消耗的积分(score_card)
         * 4.更新用户背包中金币的数量(backpack)
         */
        logger.info("用户通过积分兑换金币产生的消息:{}", JsonUtils.objectToJson(userMessageDTO));
        try {
            String userId = userMessageDTO.getUserId();
            String type = userMessageDTO.getType();
            ScoreConvertMoneyMessage scoreConvertMoneyMessage = JsonUtils.jsonToPojo(userMessageDTO.getMessage(), ScoreConvertMoneyMessage.class);
            //1.更新数据库中(activity_gold)金币的数量
            String activityId = scoreConvertMoneyMessage.getActivityId();
            Integer gold = scoreConvertMoneyMessage.getGold();
            ActivityGoldDTO activityGoldDTO = activityGoldMapper.queryActivityGoldDTOByActivityId(activityId);
            activityGoldDTO.setUnusedQuota(activityGoldDTO.getUnusedQuota() - gold);
            activityGoldDTO.setUsedQuota(activityGoldDTO.getUsedQuota() + gold);
            activityGoldMapper.updateActivityGoldDTO(activityGoldDTO);

            // 2.把积分兑换金币的行为记录到用户的日志中(user_log_record)
            UserLogRecordDTO userLogRecordDTO = new UserLogRecordDTO(userId,"恭喜用户通过积分兑换金币："+gold+"金币");
            userLogRecordMapper.insertUserLogRecordDTO(userLogRecordDTO);

            //3，更新数据库中消耗的积分(score_card)
            ScoreCardDTO scoreCardDTO = scoreCardMapper.queryScoreCardDTO(userId);
            scoreCardDTO.setScore(scoreCardDTO.getScore() - scoreConvertMoneyMessage.getConsumeScore());

            //4.更新用户背包中金币的数量(backpack)
            BackpackDTO backpackDTO = backpackMapper.queryBackpackDTO(userId);
            backpackDTO.setGold(backpackDTO.getGold()+gold);
            backpackMapper.updateBackpack(backpackDTO);

        } catch (Exception e) {
            logger.info("用户通过积分兑换金币产生的消息异常:{}", JsonUtils.objectToJson(userMessageDTO));
            e.printStackTrace();
        }

    }

    @Override
    public String getMessageType() {
        return UserMessageTypeEnum.BBS_CLOUD_USER_SCORE_CONVERT_MONEY.getType();
    }
}
