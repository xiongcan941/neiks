package com.bbs.cloud.admin.service.message.handler;

import com.bbs.cloud.admin.common.enums.gift.GiftEnum;
import com.bbs.cloud.admin.common.error.CommonExceptionEnum;
import com.bbs.cloud.admin.common.feigh.client.ActivityFeighClient;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.CommonUtil;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.service.contant.ServiceContant;
import com.bbs.cloud.admin.service.controller.ServiceController;
import com.bbs.cloud.admin.service.dto.ServiceGiftDTO;
import com.bbs.cloud.admin.service.enums.ServiceTypeEnum;
import com.bbs.cloud.admin.service.mapper.ServiceGiftMapper;
import com.bbs.cloud.admin.service.message.MessageHandler;
import com.bbs.cloud.admin.service.message.dto.OrderMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GiftOrderMessageHandler implements MessageHandler {

    final static Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private ServiceGiftMapper serviceGiftMapper;

    @Autowired
    private ActivityFeighClient activityFeighClient;

    @Override
    public void handler(OrderMessageDTO orderMessageDTO) {
        logger.info("开始处理礼物服务，message{}", JsonUtils.objectToJson(orderMessageDTO));
        try {
            Map<Integer, GiftEnum> giftsMap = GiftEnum.getGiftsMap();
            for(GiftEnum giftEnum : giftsMap.values()) {
                Integer giftType = giftEnum.getGiftType();
                ServiceGiftDTO serviceGiftDTO = serviceGiftMapper.queryGiftDTO(giftType);
                /**
                 * 其他活动没有使用到该礼物
                 */
                if(serviceGiftDTO == null) {
                    serviceGiftDTO = new ServiceGiftDTO();
                    serviceGiftDTO.setId(CommonUtil.createUUID());
                    serviceGiftDTO.setGiftType(giftType);
                    serviceGiftDTO.setAmount(ServiceContant.DEFAULT_SERVICE_GIFT_AMOUNT);
                    serviceGiftDTO.setUnusedAmount(ServiceContant.DEFAULT_SERVICE_UNUSED_GIFT_AMOUNT);
                    serviceGiftDTO.setUsedAmount(ServiceContant.DEFAULT_SERVICE_USED_GIFT_AMOUNT);
                    serviceGiftMapper.insertGiftDTO(serviceGiftDTO);
                    continue;
                } else {
                    logger.info("开始处理礼物服务订单-整理库存，礼物信息：{}", serviceGiftDTO);
                    HttpResult<Integer> result = activityFeighClient.queryServiceGiftTotal(giftType);
                    if(result == null || !CommonExceptionEnum.SUCCESS.getCode().equals(result.getCode()) ||
                        result.getData() == null){
                        logger.info("远程获取活动组件的礼物使用情况，发生异常，礼物信息：{}", serviceGiftDTO);
                        result.setData(0);
                    }
                    Integer usedActivityGiftAmount = result.getData();
                    Integer amount = serviceGiftDTO.getAmount() + ServiceContant.DEFAULT_SERVICE_GIFT_AMOUNT;
                    Integer usedAmount = usedActivityGiftAmount;
                    Integer unusedAmount = amount - usedAmount;
                    serviceGiftDTO.setUsedAmount(usedAmount);
                    serviceGiftDTO.setUnusedAmount(unusedAmount);
                    serviceGiftDTO.setAmount(amount);
                    serviceGiftMapper.updateGiftDTO(serviceGiftDTO);

                }
            }
        } catch (Exception e) {
            logger.error("开始处理礼物服务订单，发生异常，message{}", JsonUtils.objectToJson(orderMessageDTO));
            e.printStackTrace();
        }
    }

    @Override
    public Integer getServiceType() {
        return ServiceTypeEnum.GIFT_MESSAGE.getType();
    }
}
