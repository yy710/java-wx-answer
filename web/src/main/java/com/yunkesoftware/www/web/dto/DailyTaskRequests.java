package com.yunkesoftware.www.web.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public final class DailyTaskRequests {
    private DailyTaskRequests() { }

    @Data
    public static class QuizSubmit {
        private String sessionId;
        private List<QuizAnswer> answers;
    }

    @Data
    public static class QuizAnswer {
        private Integer sequenceNo;
        private String option;
    }

    @Data
    public static class VideoStart {
        private String videoId;
    }

    @Data
    public static class VideoProgress {
        private String sessionId;
        private BigDecimal positionSeconds;
        private Long clientAt;
        private Long version;
    }

    @Data
    public static class VideoClaim {
        private String sessionId;
    }

    @Data
    public static class AffairClaim {
        private String affairId;
    }

    @Data
    public static class SharePrepare {
        private String actionType;
        private String assetType;
        private String assetId;
    }

    @Data
    public static class ShareClaim {
        private String token;
        private String actionType;
        private String assetType;
        private String assetId;
    }
}
