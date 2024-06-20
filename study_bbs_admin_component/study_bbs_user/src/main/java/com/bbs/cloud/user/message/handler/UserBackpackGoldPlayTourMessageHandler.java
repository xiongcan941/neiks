package com.bbs.cloud.user.message.handler;

import com.bbs.cloud.common.message.user.UserMessageDTO;
import com.bbs.cloud.common.message.user.dto.BackpackGiftPlayTourMessage;
import com.bbs.cloud.common.message.user.dto.BackpackGoldPlayTourMessage;
import com.bbs.cloud.common.message.user.enums.UserMessageTypeEnum;
import com.bbs.cloud.common.util.JsonUtils;
import com.bbs.cloud.user.dto.BackpackDTO;
import com.bbs.cloud.user.dto.BackpackGiftDTO;
import com.bbs.cloud.user.mapper.BackpackGiftMapper;
import com.bbs.cloud.user.mapper.BackpackMapper;
import com.bbs.cloud.user.message.MessageHandler;
import com.bbs.cloud.user.message.MessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserBackpackGoldPlayTourMessageHandler implements MessageHandler {
    final static Logger logger = LoggerFactory.getLogger(MessageReceiver.class);


    @Autowired
    private BackpackMapper backpackMapper;

    @Override
    public void handler(UserMessageDTO userMessageDTO) {
        logger.info("用户通过打赏扣除背包金币数量所产生的消息, message:{}", JsonUtils.objectToJson(userMessageDTO));
        try {
            //扣减DB的数据库用户的背包金币
            String userId = userMessageDTO.getUserId();
            BackpackGoldPlayTourMessage backpackGoldPlayTourMessage = JsonUtils.jsonToPojo(
                    userMessageDTO.getMessage(), BackpackGoldPlayTourMessage.class);

            BackpackDTO backpackDTO = backpackMapper.queryBackpackDTO(userId);
            backpackDTO.setGold(backpackDTO.getGold()-backpackGoldPlayTourMessage.getGold());
            backpackMapper.updateBackpack(backpackDTO);

        } catch (Exception e) {
            logger.info("用户通过打赏扣除背包礼物数量所产生的消息发生异常,  message:{}", JsonUtils.objectToJson(userMessageDTO));
            e.printStackTrace();
        }
    }

    @Override
    public String getMessageType() {
        return UserMessageTypeEnum.BBS_CLOUD_BACKPACK_GOLD_PLAY_TOUR.getType();
    }
}
