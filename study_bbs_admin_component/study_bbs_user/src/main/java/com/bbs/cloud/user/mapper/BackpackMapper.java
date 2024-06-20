package com.bbs.cloud.user.mapper;

import com.bbs.cloud.user.dto.BackpackDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BackpackMapper {

    void insertBackpack(BackpackDTO backpack);

    BackpackDTO queryBackpackDTO(@Param("userId") String userId);

    void updateBackpack(BackpackDTO backpackDTO);
}
