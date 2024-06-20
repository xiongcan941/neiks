package com.bbs.cloud.user.mapper;

import com.bbs.cloud.user.dto.UserLogRecordDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserLogRecordMapper {

    void insertUserLogRecordDTO(UserLogRecordDTO userLogRecordDTO);

}
