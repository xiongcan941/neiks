package com.bbs.cloud.token.mapper;

import com.bbs.cloud.token.dto.LoginTicketDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginTicketMapper {

    void insertTicket(LoginTicketDTO ticketDTO);

    LoginTicketDTO getTicketDTOByTicket(@Param("ticket") String ticket);

    LoginTicketDTO getTicketDTOByUserId(@Param("userId") String userId);

    void updateTicket(LoginTicketDTO ticketDTO);
}
