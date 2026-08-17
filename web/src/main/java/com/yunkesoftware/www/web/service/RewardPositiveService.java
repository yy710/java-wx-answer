package com.yunkesoftware.www.web.service;

import com.yunkesoftware.www.web.vo.RewardPositiveResult;

import java.math.BigDecimal;

public interface RewardPositiveService {
    RewardPositiveResult reward(String userId, String eventId, Integer eventType, BigDecimal requestedPoints);
}
