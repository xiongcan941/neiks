package com.bbs.cloud.user.message.handler;

import com.bbs.cloud.common.enums.gift.GiftEnum;
import com.bbs.cloud.common.message.user.UserMessageDTO;
import com.bbs.cloud.common.message.user.dto.RobLuckyBagMessage;
import com.bbs.cloud.common.message.user.dto.RobRedPacketMessage;
import com.bbs.cloud.common.message.user.enums.UserMessageTypeEnum;
import com.bbs.cloud.common.model.user.RedPacketRecordModel;
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
public class RobRedPacketMessageHandler implements MessageHandler {

    final static Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

    @Autowired
    private BackpackMapper backpackMapper;

    @Autowired
    private BackpackGiftMapper backpackGiftMapper;

    @Autowired
    private UserLogRecordMapper userLogRecordMapper;

    @Autowired
    private RedPacketRecordMapper redPacketRecordMapper;

    @Override
    public void handler(UserMessageDTO userMessageDTO) {
        logger.info("用户抢红包产生的消息:{}", JsonUtils.objectToJson(userMessageDTO));
        try {
            String userId = userMessageDTO.getUserId();
            String message = userMessageDTO.getMessage();
            RobRedPacketMessage robRedPacketMessage = JsonUtils.jsonToPojo(message, RobRedPacketMessage.class);

            Integer gold = robRedPacketMessage.getGold();

            //第一步：更新用户背包的金币
            BackpackDTO backpackDTO = backpackMapper.queryBackpackDTO(userId);
            backpackDTO.setGold(backpackDTO.getGold() + gold);
            backpackMapper.updateBackpack(backpackDTO);

            //第二步：添加领取红包的记录
            RedPacketRecordDTO redPacketRecordDTO = new RedPacketRecordDTO();
            redPacketRecordDTO.setId(CommonUtil.createUUID());
            redPacketRecordDTO.setCreateDate(new Date());
            redPacketRecordDTO.setGold(gold);
            redPacketRecordDTO.setRedPacketId(robRedPacketMessage.getRedPacketId());
            redPacketRecordDTO.setActivityId(robRedPacketMessage.getActivityId());
            redPacketRecordDTO.setUserId(userId);
            redPacketRecordMapper.insertRedPacketRecord(redPacketRecordDTO);

            //第三部：添加用户操作日志
            UserLogRecordDTO userLogRecordDTO = new UserLogRecordDTO(userId,
                    "恭喜用户抢红包获得金币 : " +
                            gold);
            userLogRecordMapper.insertUserLogRecordDTO(userLogRecordDTO);

        } catch (Exception e) {
            logger.info("用户抢红包产生的消息异常:{}", JsonUtils.objectToJson(userMessageDTO));
            e.printStackTrace();
        }
    }

    @Override
    public String getMessageType() {
        return UserMessageTypeEnum.BBS_CLOUD_USER_ROB_LUCKY_BAG.getType();
    }

}
