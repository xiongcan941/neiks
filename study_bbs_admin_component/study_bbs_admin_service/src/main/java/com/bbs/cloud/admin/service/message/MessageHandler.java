package com.bbs.cloud.admin.service.message;

import com.bbs.cloud.admin.service.message.dto.OrderMessageDTO;

public interface MessageHandler {

    void handler(OrderMessageDTO orderMessageDTO);

    Integer getServiceType();
}
