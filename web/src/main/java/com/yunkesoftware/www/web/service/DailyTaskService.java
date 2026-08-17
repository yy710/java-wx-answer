package com.yunkesoftware.www.web.service;

import com.yunkesoftware.www.web.dto.DailyTaskRequests;

import java.util.Map;

public interface DailyTaskService {
    Map<String, Object> status(String userId);
    Map<String, Object> startQuiz(String userId);
    Map<String, Object> submitQuiz(String userId, DailyTaskRequests.QuizSubmit request);
    Map<String, Object> startVideo(String userId, DailyTaskRequests.VideoStart request);
    Map<String, Object> videoProgress(String userId, DailyTaskRequests.VideoProgress request);
    Map<String, Object> claimVideo(String userId, DailyTaskRequests.VideoClaim request);
    Map<String, Object> claimAffair(String userId, DailyTaskRequests.AffairClaim request);
    Map<String, Object> prepareShare(String userId, DailyTaskRequests.SharePrepare request);
    Map<String, Object> claimShare(String userId, DailyTaskRequests.ShareClaim request);
}
