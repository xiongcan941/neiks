package com.bbs.cloud.user.message;

import com.bbs.cloud.common.message.user.UserMessageDTO;

public interface MessageHandler {

    public void handler(UserMessageDTO userMessageDTO);

    public String getMessageType();

}
