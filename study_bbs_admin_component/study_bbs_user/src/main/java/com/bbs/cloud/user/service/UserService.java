package com.bbs.cloud.user.service;

import com.bbs.cloud.common.contant.RabbitContant;
import com.bbs.cloud.common.contant.RedisContant;
import com.bbs.cloud.common.enums.gift.GiftEnum;
import com.bbs.cloud.common.error.CommonExceptionEnum;
import com.bbs.cloud.common.error.HttpException;
import com.bbs.cloud.common.local.HostHolder;
import com.bbs.cloud.common.message.activity.ActivityMessageDTO;
import com.bbs.cloud.common.message.activity.dto.UpdateLuckyBagMessage;
import com.bbs.cloud.common.message.activity.dto.UpdateRedPacketMessage;
import com.bbs.cloud.common.message.activity.enums.ActivityMessageTypeEnum;
import com.bbs.cloud.common.message.user.UserMessageDTO;
import com.bbs.cloud.common.message.user.dto.RobLuckyBagMessage;
import com.bbs.cloud.common.message.user.dto.RobRedPacketMessage;
import com.bbs.cloud.common.message.user.dto.ScoreConvertLuckyBagMessage;
import com.bbs.cloud.common.message.user.dto.ScoreConvertMoneyMessage;
import com.bbs.cloud.common.message.user.enums.UserMessageTypeEnum;
import com.bbs.cloud.common.result.HttpResult;
import com.bbs.cloud.common.util.CommonUtil;
import com.bbs.cloud.common.util.JedisUtil;
import com.bbs.cloud.common.util.JsonUtils;
import com.bbs.cloud.common.util.RedisLockHelper;
import com.bbs.cloud.common.vo.UserVO;
import com.bbs.cloud.user.contant.UserContant;
import com.bbs.cloud.user.dto.LuckyBagDTO;
import com.bbs.cloud.user.dto.RedPacketDTO;
import com.bbs.cloud.user.exception.UserException;
import com.bbs.cloud.user.message.MessageReceiver;
import com.bbs.cloud.user.param.ScoreExchangeGoldParam;
import com.bbs.cloud.user.result.UserInfoResult;
import com.bbs.cloud.user.result.vo.GiftVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private HostHolder hostHolder;

    final static Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

    @Autowired
    private JedisUtil jedisUtil;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisLockHelper redisLockHelper;

    public HttpResult queryUserInfo() {
        UserVO userVO = hostHolder.getUser();
        String userId = userVO.getId();
        logger.info("开始查询用户背包, 背包礼物列表, 积分卡信息, userId:{}", userId);

        UserInfoResult userInfoResult = new UserInfoResult();
        Integer gold = jedisUtil.get(RedisContant.BBS_CLOUD_USER_BACKPACK_KEY + userId, Integer.class);
        userInfoResult.setGold(gold);

        Integer score = jedisUtil.get(RedisContant.BBS_CLOUD_USER_SCORE_CARD_KEY + userId, Integer.class);
        userInfoResult.setScore(score);

        List<GiftVO> giftVOList = new ArrayList<>();
        Map<Integer, GiftEnum> giftEnumMap = GiftEnum.getGiftsMap();
        for(Integer giftType : giftEnumMap.keySet()) {
            GiftEnum giftEnum = giftEnumMap.get(giftType);
            Integer amount = jedisUtil.get(RedisContant.getBbsCloudUserBackpackGiftKey(userId, giftType), Integer.class);
            GiftVO giftVO = new GiftVO();
            giftVO.setAmount(amount);
            giftVO.setGiftType(giftType);
            giftVO.setPrice(giftEnum.getPrice());
            giftVO.setName(giftEnum.getName());
            giftVO.setDesc(giftEnum.getDesc());
            giftVOList.add(giftVO);
        }
        userInfoResult.setGiftVOS(giftVOList);

        return new HttpResult(userInfoResult);
    }

    /**
     * 秒杀场景的设计方案
     * @return
     */
    public HttpResult robLuckyBag() {
        UserVO userVO = hostHolder.getUser();
        String userId = userVO.getId();
        logger.info("用户抢福袋操作, userId:{}", userId);

        Long flag = jedisUtil.sadd(RedisContant.BBS_CLOUD_ACTIVITY_LUCKY_BAG_GET_RECORD_SET, userId);

        if(flag == RedisContant.DEFAULT_REDIS_OPERATE_FLAG) {
            logger.info("用户抢福袋操作, 已参与过福袋活动 userId:{}", userId);
            return HttpResult.generateHttpResult(UserException.LUCKY_BAG_GETED);
        }

        String json = jedisUtil.lpop(RedisContant.BBS_CLOUD_ACTIVITY_LUCKY_BAG_LIST);
        if(StringUtils.isEmpty(json)) {
            logger.info("用户抢福袋操作, 没有抢到福袋, userId:{}", userId);
            return HttpResult.generateHttpResult(UserException.LUCKY_BAG__GETED);
        }

        LuckyBagDTO luckyBagDTO = null;
        try {
            luckyBagDTO = JsonUtils.jsonToPojo(json, LuckyBagDTO.class);
            if(luckyBagDTO == null) {
                logger.info("用户抢福袋操作, 福袋json转换为空, userId:{}", userId);
                return HttpResult.generateHttpResult(UserException.LUCKY_BAG__GETED);
            }
        } catch (Exception e) {
            logger.info("用户抢福袋操作, 福袋json转换异常, userId:{}", userId);
            return HttpResult.generateHttpResult(UserException.LUCKY_BAG__GETED);
        }

        Integer giftType = luckyBagDTO.getGiftType();
        jedisUtil.incr(RedisContant.getBbsCloudUserBackpackGiftKey(userId, giftType));

        /**
         * 1、将福袋中的礼物更新到数据库中
         * 2、将抢福袋的行为记录到用户的日志中
         * 3、更新福袋的领取记录
         */
        RobLuckyBagMessage robLuckyBagMessage = new RobLuckyBagMessage(giftType, luckyBagDTO.getId(), luckyBagDTO.getActivityId());
        UserMessageDTO userMessageDTO = UserMessageDTO
                .getUserMessageDTO(
                        UserMessageTypeEnum.BBS_CLOUD_USER_ROB_LUCKY_BAG.getType(),
                        userId,
                        JsonUtils.objectToJson(robLuckyBagMessage)
                );
        rabbitTemplate.convertAndSend(RabbitContant.USER_EXCHANGE_NAME, RabbitContant.USER_ROUTING_KEY, JsonUtils.objectToJson(userMessageDTO));

        /**
         * 将福袋的状态进行更新操作
         */
        UpdateLuckyBagMessage updateLuckyBagMessage = new UpdateLuckyBagMessage(luckyBagDTO.getActivityId(), luckyBagDTO.getId());
        ActivityMessageDTO activityMessageDTO = ActivityMessageDTO.getActivityMessageDTO(
                            ActivityMessageTypeEnum.BBS_CLOUD_UPDATE_LUCKY_BAG.getType(),
                            userId,
                            JsonUtils.objectToJson(updateLuckyBagMessage)
                    );
        rabbitTemplate.convertAndSend(RabbitContant.ACTIVITY_EXCHANGE_NAME, RabbitContant.ACTIVITY_ROUTING_KEY, JsonUtils.objectToJson(activityMessageDTO));


        //拿到礼物的具体信息，进行包装，返回给用户
        GiftEnum giftEnum = GiftEnum.getGiftsMap().get(giftType);
        GiftVO giftVO = new GiftVO();
        giftVO.setAmount(UserContant.DEFAULT_ROB_LUCKY_GIFT_AMOUNT);
        giftVO.setGiftType(giftType);
        giftVO.setName(giftEnum.getName());
        giftVO.setDesc(giftEnum.getDesc());
        giftVO.setPrice(giftEnum.getPrice());

        return new HttpResult(giftVO);
    }


    public HttpResult robRedpacket() {
        UserVO userVO = hostHolder.getUser();
        String userId = userVO.getId();
        logger.info("用户抢红包操作, userId:{}", userId);
        Long flag = jedisUtil.sadd(RedisContant.BBS_CLOUD_ACTIVITY_RED_PACKET_GET_RECORD_SET, userId);

        if(flag == RedisContant.DEFAULT_REDIS_OPERATE_FLAG) {
            logger.info("用户抢红包操作, 已参与过红包活动 userId:{}", userId);
            return HttpResult.generateHttpResult(UserException.RED_PACKET_GETED);
        }

        String json = jedisUtil.lpop(RedisContant.BBS_CLOUD_ACTIVITY_RED_PACKET_LIST);
        if(StringUtils.isEmpty(json)) {
            logger.info("用户抢红包操作, 没有抢到红包, userId:{}", userId);
            return HttpResult.generateHttpResult(UserException.RED_PACKET_BE_PLUNDERED);
        }

        RedPacketDTO redPacketDTO = null;
        try {
            redPacketDTO = JsonUtils.jsonToPojo(json, RedPacketDTO.class);
            if(redPacketDTO == null) {
                logger.info("用户抢红包操作, 红包json转换为空, userId:{}", userId);
                return HttpResult.generateHttpResult(UserException.RED_PACKET_BE_PLUNDERED);
            }
        } catch (Exception e) {
            logger.info("用户抢红包操作, 红包json转换为空, userId:{}", userId);
            return HttpResult.generateHttpResult(UserException.RED_PACKET_BE_PLUNDERED);
        }

        Integer gold = redPacketDTO.getGold();
        jedisUtil.incrBy(RedisContant.BBS_CLOUD_USER_BACKPACK_KEY + userId, Long.valueOf(gold));

        /**
         * 1、将红包中的金币更新到数据库中
         * 2、将抢红包的行为记录到用户的日志中
         * 3、更新红包的领取记录
         */
        RobRedPacketMessage robRedPacketMessage = new RobRedPacketMessage();
        robRedPacketMessage.setGold(gold);
        robRedPacketMessage.setActivityId(redPacketDTO.getActivityId());
        robRedPacketMessage.setRedPacketId(redPacketDTO.getId());
        UserMessageDTO userMessageDTO = UserMessageDTO.getUserMessageDTO(
                        UserMessageTypeEnum.BBS_CLOUD_USER_ROB_RED_PACKET.getType(),
                        userId,
                        JsonUtils.objectToJson(robRedPacketMessage)
                );
        rabbitTemplate.convertAndSend(RabbitContant.USER_EXCHANGE_NAME, RabbitContant.USER_ROUTING_KEY, JsonUtils.objectToJson(userMessageDTO));

        /**
         * 将红包的状态进行更新操作
         */
        UpdateRedPacketMessage updateRedPacketMessage = new UpdateRedPacketMessage();
        updateRedPacketMessage.setRedPacketId(redPacketDTO.getId());
        updateRedPacketMessage.setActivityId(redPacketDTO.getActivityId());
        ActivityMessageDTO activityMessageDTO = ActivityMessageDTO
                .getActivityMessageDTO(
                        ActivityMessageTypeEnum.BBS_CLOUD_USER_ROB_RED_PACKET.getType(),
                        userId,
                        JsonUtils.objectToJson(updateRedPacketMessage)
                );
        rabbitTemplate.convertAndSend(RabbitContant.ACTIVITY_EXCHANGE_NAME, RabbitContant.ACTIVITY_ROUTING_KEY, JsonUtils.objectToJson(activityMessageDTO));


        return new HttpResult(gold);
    }

    public HttpResult scoreExchangeLuckyBag() {
        UserVO userVO = hostHolder.getUser();
        String userId = userVO.getId();
        logger.info("用户积分兑换福袋操作, userId:{}", userId);

        boolean flag = false;
        String key = RedisContant.BBS_CLOUD_USER_SCORE_CARD_LOCK + userId;
        try {
            if(redisLockHelper.lock(key, CommonUtil.createUUID(), 3000l)) {
                /**
                 * 在判断积分数量之前，发生异常
                 */

                Integer score = jedisUtil.get(RedisContant.BBS_CLOUD_USER_SCORE_CARD_KEY + userId, Integer.class);
                if(score < UserContant.DEFAULT_SCORE_CONVERT_LUCKY_BAG) {
                    logger.info("用户积分兑换福袋操作, 积分不足, userId:{}", userId);
                    return HttpResult.generateHttpResult(UserException.SCORE_NOT_MEET);
                }
                jedisUtil.decrBy(RedisContant.BBS_CLOUD_USER_SCORE_CARD_KEY + userId, Long.valueOf(UserContant.DEFAULT_SCORE_CONVERT_LUCKY_BAG));
                flag = true;
                /**
                 * 还有其他逻辑存在
                 */

            } else {

                return HttpResult.generateHttpResult(CommonExceptionEnum.FAIL);
            }
        } catch (Exception e) {
            logger.info("用户积分兑换福袋操作异常, userId:{}", userId);
            if(flag) {
                jedisUtil.incrBy(RedisContant.BBS_CLOUD_USER_SCORE_CARD_KEY + userId, Long.valueOf(UserContant.DEFAULT_SCORE_CONVERT_LUCKY_BAG));
            }
            e.printStackTrace();
            return HttpResult.generateHttpResult(UserException.LUCKY_BAG__GETED);
        } finally {
            redisLockHelper.releaseLock(key);
        }

        String json = jedisUtil.lpop(RedisContant.BBS_CLOUD_ACTIVITY_SCORE_LUCKY_BAG_LIST);

        if(StringUtils.isEmpty(json)) {
            logger.info("用户积分兑换福袋操作, 没有抢到福袋, userId:{}", userId);
            jedisUtil.incrBy(RedisContant.BBS_CLOUD_USER_SCORE_CARD_KEY + userId, Long.valueOf(UserContant.DEFAULT_SCORE_CONVERT_LUCKY_BAG));
            return HttpResult.generateHttpResult(UserException.LUCKY_BAG__GETED);
        }

        LuckyBagDTO luckyBagDTO = null;
        try {
            luckyBagDTO = JsonUtils.jsonToPojo(json, LuckyBagDTO.class);
            if(luckyBagDTO == null) {
                logger.info("用户积分兑换福袋操作, 福袋json转换为空, userId:{}", userId);
                jedisUtil.incrBy(RedisContant.BBS_CLOUD_USER_SCORE_CARD_KEY + userId, Long.valueOf(UserContant.DEFAULT_SCORE_CONVERT_LUCKY_BAG));
                return HttpResult.generateHttpResult(UserException.LUCKY_BAG__GETED);
            }
        } catch (Exception e) {
            logger.info("用户积分兑换福袋操作, 福袋json转换异常, userId:{}", userId);
            jedisUtil.incrBy(RedisContant.BBS_CLOUD_USER_SCORE_CARD_KEY + userId, Long.valueOf(UserContant.DEFAULT_SCORE_CONVERT_LUCKY_BAG));
            return HttpResult.generateHttpResult(UserException.LUCKY_BAG__GETED);
        }

        Integer giftType = luckyBagDTO.getGiftType();
        jedisUtil.incr(RedisContant.getBbsCloudUserBackpackGiftKey(userId, giftType));

        /**
         * 1、将福袋中的礼物更新到数据库中
         * 2、将积分兑换福袋的行为记录到用户的日志中
         * 3、更新数据库中被消耗的积分
         * 4、更新福袋的领取记录
         */
        ScoreConvertLuckyBagMessage scoreConvertLuckyBagMessage = new ScoreConvertLuckyBagMessage();
        scoreConvertLuckyBagMessage.setLuckyBagId(luckyBagDTO.getId());
        scoreConvertLuckyBagMessage.setConsumeScore(UserContant.DEFAULT_SCORE_CONVERT_LUCKY_BAG);
        scoreConvertLuckyBagMessage.setActivityId(luckyBagDTO.getActivityId());
        scoreConvertLuckyBagMessage.setGiftType(giftType);
        UserMessageDTO userMessageDTO = UserMessageDTO
                .getUserMessageDTO(
                        UserMessageTypeEnum.BBS_CLOUD_USER_SCORE_CONVERT_LUCKY_BAG.getType(),
                        userId,
                        JsonUtils.objectToJson(scoreConvertLuckyBagMessage)
                );
        rabbitTemplate.convertAndSend(RabbitContant.USER_EXCHANGE_NAME, RabbitContant.USER_ROUTING_KEY, JsonUtils.objectToJson(userMessageDTO));

        /**
         * 将福袋的状态进行更新操作
         */
        UpdateLuckyBagMessage updateLuckyBagMessage = new UpdateLuckyBagMessage(luckyBagDTO.getActivityId(), luckyBagDTO.getId());
        ActivityMessageDTO activityMessageDTO = ActivityMessageDTO.getActivityMessageDTO(
                ActivityMessageTypeEnum.BBS_CLOUD_UPDATE_LUCKY_BAG.getType(),
                userId,
                JsonUtils.objectToJson(updateLuckyBagMessage)
        );
        rabbitTemplate.convertAndSend(RabbitContant.ACTIVITY_EXCHANGE_NAME, RabbitContant.ACTIVITY_ROUTING_KEY, JsonUtils.objectToJson(activityMessageDTO));


        //拿到礼物的具体信息，进行包装，返回给用户
        GiftEnum giftEnum = GiftEnum.getGiftsMap().get(giftType);
        GiftVO giftVO = new GiftVO();
        giftVO.setAmount(UserContant.DEFAULT_ROB_LUCKY_GIFT_AMOUNT);
        giftVO.setGiftType(giftType);
        giftVO.setName(giftEnum.getName());
        giftVO.setDesc(giftEnum.getDesc());
        giftVO.setPrice(giftEnum.getPrice());

        return new HttpResult(giftVO);
    }

    public HttpResult scoreExchangeGold(ScoreExchangeGoldParam param) {
        UserVO userVO = hostHolder.getUser();
        String userId = userVO.getId();
        logger.info("用户积分兑换金币操作, userId:{}, param:{}", userId, JsonUtils.objectToJson(param));

        String activityId = param.getActivityId();
        if(StringUtils.isEmpty(activityId)) {
            logger.info("用户积分兑换金币操作, 活动ID不能为空 userId:{}, param:{}", userId, JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(UserException.SCORE_CONVERT_GOLD_PARAM_ACTIVITY_ID_IS_NOT_NULL);
        }
        if(activityId.length() != 32) {
            logger.info("用户积分兑换金币操作, 活动ID不正确 userId:{}, param:{}", userId, JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(UserException.SCORE_CONVERT_GOLD_PARAM_ACTIVITY_ID_INVALID);
        }

        Integer gold = param.getGold();
        if(ObjectUtils.isEmpty(gold)) {
            logger.info("用户积分兑换金币操作, 金币不能为空,  userId:{}, param:{}", userId, JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(UserException.SCORE_CONVERT_GOLD_PARAM_GOLD_INVALID);
        }
        if(gold < UserContant.DEFAULT_SCORE_CONVERT_GOLD_MIN_VALUE && gold > UserContant.DEFAULT_SCORE_CONVERT_GOLD_MAX_VALUE) {
            logger.info("用户积分兑换金币操作, 金币小于1或者大于100,  userId:{}, param:{}", userId, JsonUtils.objectToJson(param));
            return HttpResult.generateHttpResult(UserException.SCORE_CONVERT_GOLD_PARAM_GOLD_INVALID);
        }

        Integer consumeScore = gold * UserContant.DEFAULT_SCORE_CONVERT_MONEY;

        String key = RedisContant.BBS_CLOUD_USER_SCORE_CARD_LOCK + userId;

        boolean flag = false;
        try {
            if(redisLockHelper.lock(key, CommonUtil.createUUID(), 3000l)) {
                /**
                 * 在判断积分数量之前，发生异常
                 */

                Integer score = jedisUtil.get(RedisContant.BBS_CLOUD_USER_SCORE_CARD_KEY + userId, Integer.class);

                if(score < consumeScore) {
                    logger.info("用户积分兑换金币操作, 积分不足, userId:{}", userId);
                    return HttpResult.generateHttpResult(UserException.SCORE_NOT_MEET);
                }

                jedisUtil.decrBy(RedisContant.BBS_CLOUD_USER_SCORE_CARD_KEY + userId, Long.valueOf(consumeScore));
                flag = true;
                /**
                 * 还有其他逻辑存在
                 */

            } else {
                return HttpResult.generateHttpResult(CommonExceptionEnum.FAIL);
            }
        } catch (Exception e) {
            logger.info("用户积分兑换金币操作异常, userId:{}", userId);
            if(flag) {
                jedisUtil.incrBy(RedisContant.BBS_CLOUD_USER_SCORE_CARD_KEY + userId, Long.valueOf(consumeScore));
            }
            e.printStackTrace();
            return HttpResult.generateHttpResult(UserException.SCORE_NOT_MEET);
        } finally {
            redisLockHelper.releaseLock(key);
        }


        String lock = RedisContant.BBS_CLOUD_ACTIVITY_SCORE_GOLD_LOCK;
        try {
            if(redisLockHelper.lock(lock, CommonUtil.createUUID(), 3000l)) {
                /**
                 * 在这之前发生异常
                 */


                Integer activityGold = jedisUtil.get(RedisContant.BBS_CLOUD_ACTIVITY_SCORE_GOLD, Integer.class);
                if(activityGold < gold) {
                    jedisUtil.incrBy(RedisContant.BBS_CLOUD_USER_SCORE_CARD_KEY + userId, Long.valueOf(consumeScore));
                    return HttpResult.generateHttpResult(UserException.GOLD_NOT_MEET);
                }
                jedisUtil.decrBy(RedisContant.BBS_CLOUD_ACTIVITY_SCORE_GOLD, Long.valueOf(gold));

                /**
                 * 如果中间还有一些逻辑，发生异常，我们只需要回滚活动的金币，用户的积分
                 */

                jedisUtil.incrBy(RedisContant.BBS_CLOUD_USER_BACKPACK_KEY + userId, Long.valueOf(gold));


                /**
                 * 在之后发生异常，我们就需要把活动的金币退回，用户的积分回滚，用户的金币回滚
                 */

            } else {
                jedisUtil.incrBy(RedisContant.BBS_CLOUD_USER_SCORE_CARD_KEY + userId, Long.valueOf(consumeScore));
                return HttpResult.fail();
            }
        } catch (HttpException e) {
            scoreExchangeGoldErrorHandler(e.getCode());
            return HttpResult.fail();
        } finally {
            redisLockHelper.releaseLock(lock);
        }

        /**
         * TODO MQ的异步处理，由学员自己完成，同时学员可以模拟一些场景或者第三接口在活动金币扣减的过程中，通过策略模式完成补偿机制
         * 以上完成了，对用户积分 和 积分兑换金币活动中的未使用金币 在redis中的扣除
         * 接下来要完成对数据库的操作
         * 1.更新数据库中(activity_gold)金币的数量
         * 2.把积分兑换金币的行为记录到用户的日志中(user_log_record)
         * 3，更新数据库中消耗的积分(score_card)
         * 4.更新用户背包中金币的数量(backpack)
         */
        ScoreConvertMoneyMessage scoreConvertMoneyMessage = new ScoreConvertMoneyMessage();
        scoreConvertMoneyMessage.setGold(gold);
        scoreConvertMoneyMessage.setConsumeScore(UserContant.DEFAULT_SCORE_CONVERT_MONEY);
        scoreConvertMoneyMessage.setActivityId(activityId);
        UserMessageDTO userMessageDTO = UserMessageDTO
                .getUserMessageDTO(
                        UserMessageTypeEnum.BBS_CLOUD_USER_SCORE_CONVERT_MONEY.getType(),
                        userId,
                        JsonUtils.objectToJson(scoreConvertMoneyMessage)
                );
        rabbitTemplate.convertAndSend(RabbitContant.USER_EXCHANGE_NAME, RabbitContant.USER_ROUTING_KEY, JsonUtils.objectToJson(userMessageDTO));

        return new HttpResult(gold);
    }

    private void scoreExchangeGoldErrorHandler(Integer code) {
        if(UserException.BEFORE.getCode().equals(code)) {
            //补偿机制

        } else if(UserException.CENTER.getCode().equals(code)) {
            //补偿机制

        }else if(UserException.AFTER.getCode().equals(code)) {

            //补偿机制
        }
    }
}
