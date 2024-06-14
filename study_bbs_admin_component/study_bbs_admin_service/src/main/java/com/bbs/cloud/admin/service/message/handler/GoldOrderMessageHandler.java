package com.bbs.cloud.admin.service.message.handler;

import com.bbs.cloud.admin.common.util.CommonUtil;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.service.contant.ServiceContant;
import com.bbs.cloud.admin.service.controller.ServiceController;
import com.bbs.cloud.admin.service.dto.ServiceGoldDTO;
import com.bbs.cloud.admin.service.enums.ServiceTypeEnum;
import com.bbs.cloud.admin.service.mapper.ServiceGoldMapper;
import com.bbs.cloud.admin.service.message.MessageHandler;
import com.bbs.cloud.admin.service.message.dto.OrderMessageDTO;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoldOrderMessageHandler implements MessageHandler {

    final static Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private ServiceGoldMapper serviceGoldMapper;

    @Override
    public void handler(OrderMessageDTO orderMessageDTO) {
        logger.info("开始处理充值服务，message{}", JsonUtils.objectToJson(orderMessageDTO));
        try {
            ServiceGoldDTO serviceGoldDTO = serviceGoldMapper.queryServiceGoldDTO(ServiceContant.SERVICE_GOLD_NAME);
            /**
             * 租户还没有购买金币服务
             */
            if(serviceGoldDTO == null) {
                serviceGoldDTO = new ServiceGoldDTO();
                serviceGoldDTO.setId(CommonUtil.createUUID());
                serviceGoldDTO.setName(ServiceContant.SERVICE_GOLD_NAME);
                serviceGoldDTO.setGold(ServiceContant.DEFAULT_SERVICE_GOLD);
                serviceGoldDTO.setUnusedGold(ServiceContant.DEFAULT_SERVICE_UNUSED_GOLD);
                serviceGoldDTO.setUsedGold(ServiceContant.DEFAULT_SERVICE_USED_GOLD);
                serviceGoldMapper.insertServiceGold(serviceGoldDTO);
            } else {

                /**
                 * TODO 去查询过去活动中金币的使用情况，进行库存更新
                 */
            }
        } catch (Exception e) {
            logger.error("开始处理充值服务订单发生异常，message{}", JsonUtils.objectToJson(orderMessageDTO));
            e.printStackTrace();
        }
    }

    @Override
    public Integer getServiceType() {
        return ServiceTypeEnum.RECHARGE_MESSAGE.getType();
    }
}
