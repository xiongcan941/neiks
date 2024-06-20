package com.bbs.cloud.user.message.handler;

import com.bbs.cloud.common.message.essay.enums.EssayMessageTypeEnum;
import com.bbs.cloud.common.message.user.UserMessageDTO;
import com.bbs.cloud.common.message.user.enums.UserMessageTypeEnum;
import com.bbs.cloud.common.util.JsonUtils;
import com.bbs.cloud.user.dto.ScoreCardDTO;
import com.bbs.cloud.user.dto.UserLogRecordDTO;
import com.bbs.cloud.user.mapper.ScoreCardMapper;
import com.bbs.cloud.user.mapper.UserLogRecordMapper;
import com.bbs.cloud.user.message.MessageHandler;
import com.bbs.cloud.user.message.MessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCommentMessageHandler implements MessageHandler {
    final static Logger logger = LoggerFactory.getLogger(MessageReceiver.class);
    @Autowired
    private ScoreCardMapper scoreCardMapper;
    @Autowired
    private UserLogRecordMapper userLogRecordMapper;

    @Override
    public void handler(UserMessageDTO userMessageDTO) {
        logger.info("用户通过发布评论增加积分所产生的消息:{}", JsonUtils.objectToJson(userMessageDTO));
        try {
            String userId = userMessageDTO.getUserId();
            Integer score = JsonUtils.jsonToPojo(userMessageDTO.getMessage(), Integer.class);

            ScoreCardDTO scoreCardDTO = scoreCardMapper.queryScoreCardDTO(userId);
            scoreCardDTO.setScore(scoreCardDTO.getScore()+score);
            scoreCardMapper.updateScoreCard(scoreCardDTO);

            UserLogRecordDTO userLogRecordDTO = new UserLogRecordDTO(userId,"用户通过发布评论获得"+score+"积分");
            userLogRecordMapper.insertUserLogRecordDTO(userLogRecordDTO);
        } catch (Exception e) {
            logger.info("用户通过发布评论增加积分所产生的消息异常:{}", JsonUtils.objectToJson(userMessageDTO));
            e.printStackTrace();
        }

    }
    @Override
    public String getMessageType() {
        return  UserMessageTypeEnum.BBS_CLOUD_USER_ADD_COMMENT.getType();
    }
}
