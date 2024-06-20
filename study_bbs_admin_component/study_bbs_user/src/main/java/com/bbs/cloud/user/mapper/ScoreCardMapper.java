package com.bbs.cloud.user.mapper;

import com.bbs.cloud.user.dto.ScoreCardDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ScoreCardMapper {

    void insertScoreCard(ScoreCardDTO scoreCardDTO);

    ScoreCardDTO queryScoreCardDTO(@Param("userId") String userId);

    void updateScoreCard(ScoreCardDTO scoreCardDTO);
}
