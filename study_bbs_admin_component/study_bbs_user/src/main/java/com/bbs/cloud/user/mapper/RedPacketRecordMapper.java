package com.bbs.cloud.user.mapper;

import com.bbs.cloud.user.dto.RedPacketRecordDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RedPacketRecordMapper {

    void insertRedPacketRecord(RedPacketRecordDTO redPacketRecordDTO);

}
