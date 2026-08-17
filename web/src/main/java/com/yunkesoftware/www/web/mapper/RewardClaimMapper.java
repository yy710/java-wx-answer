package com.yunkesoftware.www.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunkesoftware.www.web.entity.RewardClaim;
import org.apache.ibatis.annotations.Param;

public interface RewardClaimMapper extends BaseMapper<RewardClaim> {
    RewardClaim selectForUpdate(@Param("userId") String userId, @Param("eventId") String eventId);
}
