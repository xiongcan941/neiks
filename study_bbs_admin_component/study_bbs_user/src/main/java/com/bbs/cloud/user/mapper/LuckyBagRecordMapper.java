package com.bbs.cloud.user.mapper;

import com.bbs.cloud.user.dto.LuckyBagRecordDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LuckyBagRecordMapper {

    void insertLuckyBagRecordDTO(LuckyBagRecordDTO luckyBagRecordDTO);


}
