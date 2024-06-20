package com.bbs.cloud.user.message.handler;

import com.bbs.cloud.common.contant.RedisContant;
import com.bbs.cloud.common.enums.gift.GiftEnum;
import com.bbs.cloud.common.message.user.UserMessageDTO;
import com.bbs.cloud.common.message.user.enums.UserMessageTypeEnum;
import com.bbs.cloud.common.util.CommonUtil;
import com.bbs.cloud.common.util.JedisUtil;
import com.bbs.cloud.common.util.JsonUtils;
import com.bbs.cloud.user.contant.UserContant;
import com.bbs.cloud.user.dto.BackpackDTO;
import com.bbs.cloud.user.dto.BackpackGiftDTO;
import com.bbs.cloud.user.dto.ScoreCardDTO;
import com.bbs.cloud.user.mapper.BackpackGiftMapper;
import com.bbs.cloud.user.mapper.BackpackMapper;
import com.bbs.cloud.user.mapper.ScoreCardMapper;
import com.bbs.cloud.user.message.MessageHandler;
import com.bbs.cloud.user.message.MessageReceiver;
import com.sun.xml.internal.bind.v2.TODO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserLikedMessageHandler implements MessageHandler {
    final static Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

    @Autowired
    private ScoreCardMapper scoreCardMapper;

    @Autowired
    private JedisUtil jedisUtil;
    //1被点赞的人积分增加(score_card);
    @Override
    public void handler(UserMessageDTO userMessageDTO) {
        logger.info("接受用户被点赞需增加积分所产生的消息, message:{}", JsonUtils.objectToJson(userMessageDTO));
        try {
            String userId = userMessageDTO.getUserId();

            ScoreCardDTO scoreCardDTO = scoreCardMapper.queryScoreCardDTO(userId);
            scoreCardDTO.setScore(scoreCardDTO.getScore()+JsonUtils.jsonToPojo(userMessageDTO.getMessage(),Integer.class));
            scoreCardMapper.updateScoreCard(scoreCardDTO);
        } catch (Exception e) {
            logger.info("接受用户被点赞需增加积分所产生的消息发生异常,  message:{}", JsonUtils.objectToJson(userMessageDTO));
            e.printStackTrace();
        }
    }

    @Override
    public String getMessageType() {
        return UserMessageTypeEnum.BBS_CLOUD_USER_LIKED.getType();
    }
}
