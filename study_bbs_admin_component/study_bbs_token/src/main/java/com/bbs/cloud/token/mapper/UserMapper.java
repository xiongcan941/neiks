package com.bbs.cloud.token.mapper;

import com.bbs.cloud.token.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    void insertUser(UserDTO userDTO);

    UserDTO queryUserByUsername(@Param("username") String username);

}
