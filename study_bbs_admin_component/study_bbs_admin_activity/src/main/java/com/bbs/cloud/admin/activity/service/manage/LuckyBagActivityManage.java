package com.bbs.cloud.admin.activity.service.manage;

import com.bbs.cloud.admin.activity.contant.ActivityContant;
import com.bbs.cloud.admin.activity.dto.ActivityDTO;
import com.bbs.cloud.admin.activity.dto.GiftDTO;
import com.bbs.cloud.admin.activity.dto.LuckyBagDTO;
import com.bbs.cloud.admin.activity.exception.ActivityException;
import com.bbs.cloud.admin.activity.mapper.ActivityMapper;
import com.bbs.cloud.admin.activity.mapper.LuckyBagMapper;
import com.bbs.cloud.admin.activity.param.CreateActivityParam;
import com.bbs.cloud.admin.activity.param.OperatorActivityParam;
import com.bbs.cloud.admin.activity.service.ActivityManage;
import com.bbs.cloud.admin.activity.service.ActivityService;
import com.bbs.cloud.admin.common.contant.RedisContant;
import com.bbs.cloud.admin.common.enums.activity.ActivityStatusEnum;
import com.bbs.cloud.admin.common.enums.activity.ActivityTypeEnum;
import com.bbs.cloud.admin.common.enums.activity.LuckyBagStatusEnum;
import com.bbs.cloud.admin.common.error.CommonExceptionEnum;
import com.bbs.cloud.admin.common.error.HttpException;
import com.bbs.cloud.admin.common.feigh.client.ServiceFeighClient;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.CommonUtil;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.common.util.RedisLockHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
public class LuckyBagActivityManage implements ActivityManage {

    final static Logger logger = LoggerFactory.getLogger(LuckyBagActivityManage.class);

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private LuckyBagMapper luckyBagMapper;

    @Autowired
    private ServiceFeighClient serviceFeighClient;

    @Autowired
    private RedisLockHelper redisLockHelper;

    @Override
    @Transactional(rollbackFor = {HttpException.class,Exception.class})
    public HttpResult createActivity(CreateActivityParam createActivityParam) {
        logger.info("开始创建福袋活动，请求参数:{}", JsonUtils.objectToJson(createActivityParam));
        //获取福袋的数量
        Integer amount = createActivityParam.getAmount();
        if(ObjectUtils.isEmpty(amount)){
            logger.info("开始创建福袋活动，福袋数量为空,请求参数:{}", JsonUtils.objectToJson(createActivityParam));
            return HttpResult.generateHttpResult(ActivityException.LUCKY_BAG_ACTIVITY_AMOUNT_IS_NOT_NULL);
        }
        if(amount < ActivityContant.DEFAULT_LUCKY_BAG_ACTIVITY_MIN_AMOUNT){
            logger.info("开始创建福袋活动，福袋数量小于最小福袋数量,请求参数:{}", JsonUtils.objectToJson(createActivityParam));
            return HttpResult.generateHttpResult(ActivityException.LUCKY_BAG_ACTIVITY_AMOUNT_LESS_THAN_ONE);
        }

        String key = RedisContant.BBS_CLOUD_LOCK_GIFT_KEY;

        try {
            if(redisLockHelper.lock(key,CommonUtil.createUUID(),100000L)){
                HttpResult<Integer> result = serviceFeighClient.queryServiceGiftTotal();
                if(result == null || !CommonExceptionEnum.SUCCESS.getCode().equals(result.getCode()) ||
                        result.getData() == null){
                    logger.info("开始创建福袋活动，远程调用，获取服务组件总数量失败,请求参数:{}，result:{}", JsonUtils.objectToJson(createActivityParam),
                            JsonUtils.objectToJson(result));
                    return HttpResult.generateHttpResult(ActivityException.LUCKY_BAG_ACTIVITY_SERVICE_GIFT_AMOUNT_FAIL);
                }
                Integer total = result.getData();
                if(total < amount){
                    logger.info("开始创建福袋活动，远程调用，获取服务组件总数量不足,请求参数:{}，result:{}", JsonUtils.objectToJson(createActivityParam),
                            JsonUtils.objectToJson(result));
                    return HttpResult.generateHttpResult(ActivityException.LUCKY_BAG_ACTIVITY_SERVICE_GIFT_AMOUNT_NOT_MEET);
                }
                //第一步：创建活动
                logger.info("开始创建福袋活动------开始创建活动,请求参数:{}", JsonUtils.objectToJson(createActivityParam));
                ActivityDTO activityDTO = new ActivityDTO();
                activityDTO.setId(CommonUtil.createUUID());
                activityDTO.setName(createActivityParam.getName());
                activityDTO.setContent(createActivityParam.getContent());
                activityDTO.setActivityType(createActivityParam.getActivityType());
                activityDTO.setAmount(amount);
                activityDTO.setStatus(ActivityStatusEnum.INITIAL.getStatus());
                activityDTO.setCreateDate(new Date());
                activityDTO.setUpdateDate(new Date());
                try {
                    activityMapper.insertActivityDTO(activityDTO);
                } catch (Exception e){
                    logger.info("开始创建福袋活动------添加活动失败,请求参数:{}", JsonUtils.objectToJson(createActivityParam));
                    e.printStackTrace();
                    throw new HttpException(ActivityException.LUCKY_BAG_ACTIVITY_ADD_FAIL);
                }


                //第二步：创建福袋
                List<GiftDTO> GiftDTOList = packLuckBag(amount,activityDTO.getId());

            } else {
                logger.info("开始创建福袋活动------请勿重复操作,请求参数:{}", JsonUtils.objectToJson(createActivityParam));
                return HttpResult.generateHttpResult(ActivityException.ACTIVITY_NOT_REPEAT_MANAGE);
            }
        } catch (HttpException e){
            logger.info("开始创建福袋活动，发生HttpException异常,请求参数:{}", JsonUtils.objectToJson(createActivityParam));
            e.printStackTrace();
            throw e;
        } catch (Exception e){
            logger.info("开始创建福袋活动，发生Exception异常,请求参数:{}", JsonUtils.objectToJson(createActivityParam));
            e.printStackTrace();
            return HttpResult.fail();
        } finally {
            redisLockHelper.releaseLock(key);
        }


        return HttpResult.ok();
    }

    /**
     * 包装福袋
     * @param amount
     * @param activityId
     * @return
     */
    private List<GiftDTO> packLuckBag(Integer amount,String activityId) {
        logger.info("开始创建福袋活动------开始包装福袋，amount：{}，activityId:{}", amount, activityId);
        HttpResult<String> result = serviceFeighClient.queryServiceGiftList();
        if(result == null || !CommonExceptionEnum.SUCCESS.getCode().equals(result.getCode()) ||
                result.getData() == null){
            logger.info("开始创建福袋活动，远程调用，获取服务组件列表失败,amount:{},activityId:{}，result:{}", amount,activityId,
                    JsonUtils.objectToJson(result));
            throw new HttpException(ActivityException.LUCKY_BAG_ACTIVITY_QUERY_SERVICE_GIFT_LIST_ERROR);
        }
        String giftListJson = result.getData();
        List<GiftDTO> giftDTOS = JsonUtils.jsonToList(giftListJson, GiftDTO.class);
        Map<Integer,GiftDTO> giftDTOMap = new HashMap<>();
        giftDTOS.forEach(item -> giftDTOMap.put(item.getGiftType(),item));

        List<LuckyBagDTO> luckyBagDTOList = new ArrayList<>();
        for(int i = 0; i < amount; i ++){
            LuckyBagDTO luckyBagDTO = new LuckyBagDTO();
            luckyBagDTO.setId(CommonUtil.createUUID());
            luckyBagDTO.setActivityId(activityId);
            luckyBagDTO.setStatus(LuckyBagStatusEnum.NORMAL.getStatus());
            luckyBagDTO.setGiftType(randomGiftType());
            luckyBagDTOList.add(luckyBagDTO);
            //更新服务组件礼物的数量
            GiftDTO giftDTO = giftDTOMap.get(luckyBagDTO.getGiftType());
            giftDTO.setUsedAmount(giftDTO.getUsedAmount() + ActivityContant.DEFAULT_LUCKY_BAG_CONSUME_AMOUNT);
            giftDTO.setUnusedAmount(giftDTO.getUnusedAmount() - ActivityContant.DEFAULT_LUCKY_BAG_CONSUME_AMOUNT);
            giftDTOMap.put(luckyBagDTO.getGiftType(),giftDTO);
        }
        List giftDTOList = Arrays.asList(giftDTOMap.values().toArray());
        //第三步：更新服务组件的礼物列表
        logger.info("开始创建福袋活动------更新服务组件礼物列表,请求参数:{}", JsonUtils.objectToJson(giftDTOList));
        HttpResult updateResult = serviceFeighClient.updateServiceGiftList(JsonUtils.objectToJson(giftDTOList));
        if(updateResult == null || !CommonExceptionEnum.SUCCESS.getCode().equals(updateResult.getCode())){
            logger.info("开始创建福袋活动------更新服务组件礼物列表异常,请求参数:{}", JsonUtils.objectToJson(giftDTOList));
            throw new HttpException(ActivityException.RED_PACKET_ACTIVITY_SERVICE_GOLD_UPDATE_FAIL);
        }
        try {
            luckyBagMapper.insertLuckyBag(luckyBagDTOList);
        } catch (Exception e) {
            logger.info("开始创建福袋活动------批量添加福袋异常,请求参数:{}", JsonUtils.objectToJson(giftDTOList));
            logger.info("开始创建福袋活动------批量添加福袋异常，补偿服务组件礼物列表,请求参数:{}", JsonUtils.objectToJson(giftDTOList));
            serviceFeighClient.updateServiceGiftList(JsonUtils.objectToJson(giftDTOS));
            logger.info("开始创建福袋活动------批量添加福袋异常，补偿服务组件礼物列表，补偿成功,请求参数:{}", JsonUtils.objectToJson(giftDTOList));
            throw new HttpException(ActivityException.LUCKY_BAG_ACTIVITY_BATCH_INSERT_LUCKY_BAG_FAIL);
        }
        return giftDTOList;
    }

    @Override
    public HttpResult startActivity(OperatorActivityParam operatorActivityParam) {
        return null;
    }

    /**
     * 生成1-10之间的随机数
     * @return
     */
    private static Integer randomGiftType(){
        int min = 1;
        int max = 10;
        int randomNum = (int) (Math.random() * (max - min + 1)) + min;
        return Integer.valueOf(randomNum);
    }


    @Override
    public HttpResult endActivity(OperatorActivityParam operatorActivityParam) {
        return null;
    }

    @Override
    public Integer getActivityType() {
        return ActivityTypeEnum.LUCKY_BAG.getType();
    }
}
