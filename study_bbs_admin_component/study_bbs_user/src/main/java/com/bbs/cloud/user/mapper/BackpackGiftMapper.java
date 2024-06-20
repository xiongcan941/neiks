package com.bbs.cloud.user.mapper;

import com.bbs.cloud.user.dto.BackpackGiftDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BackpackGiftMapper {

    void insertBackpackGiftDTO(BackpackGiftDTO backpackGiftDTO);



    void updateBackpackGift(BackpackGiftDTO backpackGiftDTO);

    List<BackpackGiftDTO> queryBackpackGiftDTOList(@Param("backpackId") String backpackId);

    BackpackGiftDTO queryBackpackGiftDTO(@Param("backpackId") String backpackId, @Param("giftType") Integer giftType);
}
