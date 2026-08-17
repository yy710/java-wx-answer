package com.yunkesoftware.www.web.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yunkesoftware.www.enums.UserWalletEventEnum;
import com.yunkesoftware.www.enums.UserWalletTypeEnum;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.web.dto.DailyTaskRequests;
import com.yunkesoftware.www.web.entity.DailyTaskActionToken;
import com.yunkesoftware.www.web.entity.DailyTaskClaim;
import com.yunkesoftware.www.web.entity.DailyTaskConfig;
import com.yunkesoftware.www.web.entity.DailyTaskQuizQuestion;
import com.yunkesoftware.www.web.entity.DailyTaskQuizSession;
import com.yunkesoftware.www.web.entity.DailyTaskVideoSession;
import com.yunkesoftware.www.web.entity.Introduce;
import com.yunkesoftware.www.web.entity.PosterGeneration;
import com.yunkesoftware.www.web.entity.Topic;
import com.yunkesoftware.www.web.entity.TopicItem;
import com.yunkesoftware.www.web.entity.UserWallet;
import com.yunkesoftware.www.web.entity.Video;
import com.yunkesoftware.www.web.mapper.DailyTaskActionTokenMapper;
import com.yunkesoftware.www.web.mapper.DailyTaskClaimMapper;
import com.yunkesoftware.www.web.mapper.DailyTaskConfigMapper;
import com.yunkesoftware.www.web.mapper.DailyTaskQuizQuestionMapper;
import com.yunkesoftware.www.web.mapper.DailyTaskQuizSessionMapper;
import com.yunkesoftware.www.web.mapper.DailyTaskVideoSessionMapper;
import com.yunkesoftware.www.web.mapper.IntroduceMapper;
import com.yunkesoftware.www.web.mapper.TopicItemMapper;
import com.yunkesoftware.www.web.mapper.TopicMapper;
import com.yunkesoftware.www.web.mapper.UserWalletMapper;
import com.yunkesoftware.www.web.mapper.UserWalletRecordMapper;
import com.yunkesoftware.www.web.mapper.VideoMapper;
import com.yunkesoftware.www.web.mapper.PosterGenerationMapper;
import com.yunkesoftware.www.web.service.DailyTaskConfigService;
import com.yunkesoftware.www.web.service.DailyTaskService;
import com.yunkesoftware.www.web.service.RewardPositiveService;
import com.yunkesoftware.www.web.vo.RewardPositiveResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DailyTaskServiceImpl implements DailyTaskService {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ACTION_TOKEN_TTL_MINUTES = 10;
    private static final BigDecimal MAX_HEARTBEAT_JUMP = new BigDecimal("15");
    private static final long MAX_HEARTBEAT_GAP_SECONDS = 60;
    private static final String QUIZ = "QUIZ";
    private static final String VIDEO = "VIDEO";
    private static final String AFFAIR = "AFFAIR";
    private static final String SHARE = "SHARE_OR_SAVE";

    @Resource private DailyTaskConfigService configService;
    @Resource private DailyTaskConfigMapper configMapper;
    @Resource private DailyTaskClaimMapper claimMapper;
    @Resource private DailyTaskQuizSessionMapper quizSessionMapper;
    @Resource private DailyTaskQuizQuestionMapper quizQuestionMapper;
    @Resource private DailyTaskVideoSessionMapper videoSessionMapper;
    @Resource private DailyTaskActionTokenMapper actionTokenMapper;
    @Resource private TopicMapper topicMapper;
    @Resource private TopicItemMapper topicItemMapper;
    @Resource private VideoMapper videoMapper;
    @Resource private IntroduceMapper introduceMapper;
    @Resource private UserWalletMapper walletMapper;
    @Resource private UserWalletRecordMapper walletRecordMapper;
    @Resource private PosterGenerationMapper generationMapper;
    @Resource private RewardPositiveService rewardPositiveService;
    @Resource private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> status(String userId) {
        LocalDate date = today();
        DailyTaskConfig config = config();
        DailyTaskQuizSession quiz = quizSessionMapper.selectOne(new LambdaQueryWrapper<DailyTaskQuizSession>()
                .eq(DailyTaskQuizSession::getUserId, userId).eq(DailyTaskQuizSession::getTaskDate, date));
        DailyTaskVideoSession video = videoSessionMapper.selectOne(new LambdaQueryWrapper<DailyTaskVideoSession>()
                .eq(DailyTaskVideoSession::getUserId, userId).eq(DailyTaskVideoSession::getTaskDate, date));
        DailyTaskClaim affair = claimMapper.selectByUserDateType(userId, date.toString(), AFFAIR);
        DailyTaskClaim share = claimMapper.selectByUserDateType(userId, date.toString(), SHARE);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskDate", date.toString());
        result.put("config", configMap(config));
        Map<String, Object> quizMap = new LinkedHashMap<>();
        quizMap.put("started", quiz != null);
        quizMap.put("completed", quiz != null && !"STARTED".equals(quiz.getStatus()));
        quizMap.put("sessionId", quiz == null ? null : quiz.getId());
        quizMap.put("status", quiz == null ? null : quiz.getStatus());
        quizMap.put("deadlineAt", quiz == null ? null : format(quiz.getDeadlineAt()));
        quizMap.put("questionCount", quiz == null ? config.getQuizQuestionCount() : quiz.getQuestionCount());
        quizMap.put("timeLimitSeconds", quiz == null ? config.getQuizTimeLimitSeconds() : quiz.getTimeLimitSeconds());
        if (quiz != null) quizMap.put("questions", quizQuestions(quiz, !"STARTED".equals(quiz.getStatus())));
        else quizMap.put("questions", List.of());
        result.put("quiz", quizMap);
        Map<String, Object> videoMap = new LinkedHashMap<>();
        videoMap.put("started", video != null);
        videoMap.put("completed", video != null && "CLAIMED".equals(video.getStatus()));
        videoMap.put("sessionId", video == null ? null : video.getId());
        videoMap.put("videoId", video == null ? null : video.getVideoId());
        videoMap.put("requiredWatchSeconds", video == null ? 0 : video.getRequiredWatchSeconds());
        videoMap.put("creditedWatchSeconds", video == null ? 0 : video.getCreditedWatchSeconds());
        videoMap.put("version", video == null ? null : video.getVersion());
        videoMap.put("status", video == null ? null : video.getStatus());
        videoMap.put("eligible", video != null && ("QUALIFIED".equals(video.getStatus()) || "CLAIMED".equals(video.getStatus())));
        result.put("video", videoMap);
        result.put("affair", Map.of("completed", affair != null));
        result.put("share", Map.of("completed", share != null));
        result.putAll(walletSummary(userId, BigDecimal.ZERO, BigDecimal.ZERO, "FULL"));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startQuiz(String userId) {
        DailyTaskConfig config = config();
        requireEnabled(config, config.getQuizEnabled(), "DAILY_TASK_DISABLED", "每日答题已关闭");
        LocalDate date = today();
        if (claimMapper.selectByUserDateType(userId, date.toString(), QUIZ) != null
                || quizSessionMapper.selectByUserDateForUpdate(userId, date.toString()) != null) {
            fail("DAILY_CLAIMED", "今日答题已开始，不能重新抽题");
        }
        int count = positive(config.getQuizQuestionCount(), 2);
        List<Topic> topics = topicMapper.listDailyTaskTopics(count);
        if (topics == null || topics.size() < count) fail("QUIZ_POOL_INSUFFICIENT", "题库启用数量不足，请联系运营配置");
        LocalDateTime started = now();
        DailyTaskQuizSession session = new DailyTaskQuizSession().setId(IdUtil.getSnowflakeNextIdStr()).setUserId(userId)
                .setTaskDate(date).setStatus("STARTED").setQuestionCount(count)
                .setRewardPerCorrect(money(config.getQuizRewardPerCorrect(), "10.00"))
                .setTimeLimitSeconds(positive(config.getQuizTimeLimitSeconds(), 120)).setStartedAt(started)
                .setDeadlineAt(started.plusSeconds(positive(config.getQuizTimeLimitSeconds(), 120))).setCreatedAt(started);
        try {
            quizSessionMapper.insert(session);
        } catch (DuplicateKeyException duplicate) {
            fail("DAILY_CLAIMED", "今日答题已开始，不能重新抽题");
        }
        List<Map<String, Object>> questions = new ArrayList<>();
        for (int index = 0; index < topics.size(); index++) {
            Topic topic = topics.get(index);
            List<TopicItem> options = topicItemMapper.selectList(new LambdaQueryWrapper<TopicItem>()
                    .eq(TopicItem::getTopicId, topic.getId()).orderByAsc(TopicItem::getSeq));
            TopicItem answer = options.stream().filter(item -> Boolean.TRUE.equals(item.getAnswerFlag())).findFirst().orElse(null);
            if (answer == null) fail("QUIZ_POOL_INSUFFICIENT", "题库存在没有正确答案的题目");
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("content", topic.getTitle());
            List<Map<String, String>> safeOptions = options.stream().map(item -> Map.of("key", item.getId(), "text", item.getTitle())).toList();
            snapshot.put("options", safeOptions);
            DailyTaskQuizQuestion question = new DailyTaskQuizQuestion().setId(IdUtil.getSnowflakeNextIdStr())
                    .setSessionId(session.getId()).setSequenceNo(index + 1).setTopicId(topic.getId())
                    .setQuestionSnapshot(json(snapshot)).setCorrectOptionSnapshot(answer.getId())
                    .setRequestedPoints(BigDecimal.ZERO).setAwardedPoints(BigDecimal.ZERO);
            quizQuestionMapper.insert(question);
            Map<String, Object> apiQuestion = new LinkedHashMap<>();
            apiQuestion.put("sequenceNo", index + 1); apiQuestion.put("topicId", topic.getId());
            apiQuestion.put("content", topic.getTitle()); apiQuestion.put("options", safeOptions);
            questions.add(apiQuestion);
        }
        return Map.of("sessionId", session.getId(), "deadlineAt", format(session.getDeadlineAt()), "questions", questions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = QuizExpiredException.class)
    public Map<String, Object> submitQuiz(String userId, DailyTaskRequests.QuizSubmit request) {
        DailyTaskConfig config = config();
        requireEnabled(config, config.getQuizEnabled(), "DAILY_TASK_DISABLED", "每日答题已关闭");
        LocalDate date = today();
        DailyTaskQuizSession session = quizSessionMapper.selectByUserDateForUpdate(userId, date.toString());
        if (session == null || !String.valueOf(session.getId()).equals(request == null ? null : request.getSessionId())) fail("QUIZ_INVALID_ANSWERS", "答题会话不存在或已失效");
        List<DailyTaskQuizQuestion> questions = quizQuestionMapper.listBySession(session.getId());
        if (!"STARTED".equals(session.getStatus())) return walletSummary(userId, sumRequested(questions), sumAwarded(questions), "ALREADY_CLAIMED");
        if (now().isAfter(session.getDeadlineAt())) {
            session.setStatus("EXPIRED").setSubmittedAt(now()); quizSessionMapper.updateById(session);
            insertTaskClaim(userId, date, QUIZ, "quiz:" + session.getId(), BigDecimal.ZERO, BigDecimal.ZERO, "QUIZ_EXPIRED", null);
            throw new QuizExpiredException();
        }
        List<DailyTaskRequests.QuizAnswer> answers = request == null ? null : request.getAnswers();
        if (answers == null || answers.size() != questions.size()) fail("QUIZ_INVALID_ANSWERS", "必须一次提交全部题目答案");
        Set<Integer> sequenceSet = new HashSet<>();
        for (DailyTaskRequests.QuizAnswer answer : answers) {
            if (answer == null || answer.getSequenceNo() == null || !sequenceSet.add(answer.getSequenceNo())
                    || answer.getSequenceNo() < 1 || answer.getSequenceNo() > questions.size()) fail("QUIZ_INVALID_ANSWERS", "答案顺序不正确");
        }
        Map<Integer, DailyTaskRequests.QuizAnswer> answerMap = new HashMap<>();
        answers.forEach(answer -> answerMap.put(answer.getSequenceNo(), answer));
        BigDecimal requestedTotal = BigDecimal.ZERO, awardedTotal = BigDecimal.ZERO;
        String reason = "NOT_ELIGIBLE";
        for (DailyTaskQuizQuestion question : questions) {
            DailyTaskRequests.QuizAnswer answer = answerMap.get(question.getSequenceNo());
            boolean correct = answer.getOption() != null && answer.getOption().equals(question.getCorrectOptionSnapshot());
            BigDecimal requested = correct ? money(session.getRewardPerCorrect(), "10.00") : BigDecimal.ZERO;
            BigDecimal awarded = BigDecimal.ZERO;
            if (correct) {
                RewardPositiveResult reward = rewardPositiveService.reward(userId, question.getId(), UserWalletEventEnum.DAILY_QUIZ_CORRECT.getKey(), requested);
                awarded = new BigDecimal(reward.getAwardedPoints()); reason = reward.getAwardReason();
            }
            question.setUserOption(answer.getOption()).setCorrect(correct).setRequestedPoints(requested).setAwardedPoints(awarded);
            quizQuestionMapper.updateById(question);
            requestedTotal = requestedTotal.add(requested); awardedTotal = awardedTotal.add(awarded);
        }
        if (requestedTotal.compareTo(BigDecimal.ZERO) > 0 && awardedTotal.compareTo(requestedTotal) == 0) reason = "FULL";
        else if (requestedTotal.compareTo(BigDecimal.ZERO) > 0 && awardedTotal.compareTo(BigDecimal.ZERO) > 0 && awardedTotal.compareTo(requestedTotal) < 0) reason = "PARTIAL_DAILY_CAP";
        session.setStatus("SUBMITTED").setSubmittedAt(now()); quizSessionMapper.updateById(session);
        insertTaskClaim(userId, date, QUIZ, "quiz:" + session.getId(), requestedTotal, awardedTotal, reason, null);
        Map<String, Object> response = walletSummary(userId, requestedTotal, awardedTotal, reason);
        response.put("questions", quizQuestions(session, true));
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startVideo(String userId, DailyTaskRequests.VideoStart request) {
        DailyTaskConfig config = config();
        requireEnabled(config, config.getVideoEnabled(), "DAILY_TASK_DISABLED", "消保视频已关闭");
        LocalDate date = today();
        DailyTaskVideoSession existing = videoSessionMapper.selectByUserDateForUpdate(userId, date.toString());
        if (existing != null) return videoApi(existing, true);
        String requestedId = request == null ? null : request.getVideoId();
        Video video;
        if (requestedId != null && !requestedId.isBlank()) {
            video = videoMapper.selectOne(new LambdaQueryWrapper<Video>().eq(Video::getId, requestedId)
                    .eq(Video::getStatus, true).eq(Video::getDailyTaskEnabled, true));
        } else {
            video = videoMapper.selectOne(new LambdaQueryWrapper<Video>().eq(Video::getStatus, true)
                    .eq(Video::getDailyTaskEnabled, true).gt(Video::getDurationSeconds, 0).last("ORDER BY RAND() LIMIT 1"));
        }
        if (video == null || video.getDurationSeconds() == null || video.getDurationSeconds() <= 0) fail("VIDEO_DURATION_UNTRUSTED", "视频缺少可信时长");
        BigDecimal ratio = config.getVideoMinWatchRatio() == null ? new BigDecimal("0.5000") : config.getVideoMinWatchRatio();
        int required = Math.max(1, ratio.multiply(BigDecimal.valueOf(video.getDurationSeconds())).setScale(0, RoundingMode.CEILING).intValue());
        LocalDateTime started = now();
        DailyTaskVideoSession session = new DailyTaskVideoSession().setId(IdUtil.getSnowflakeNextIdStr()).setUserId(userId)
                .setTaskDate(date).setVideoId(video.getId()).setVideoDurationSeconds(video.getDurationSeconds()).setMinWatchRatio(ratio)
                .setRequiredWatchSeconds(required).setCreditedWatchSeconds(0).setLastPositionSeconds(BigDecimal.ZERO)
                .setStatus("STARTED").setVersion(1L).setStartedAt(started);
        try { videoSessionMapper.insert(session); } catch (DuplicateKeyException duplicate) {
            DailyTaskVideoSession retry = videoSessionMapper.selectByUserDateForUpdate(userId, date.toString());
            return videoApi(retry, true);
        }
        return videoApi(session, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> videoProgress(String userId, DailyTaskRequests.VideoProgress request) {
        DailyTaskConfig config = config();
        requireEnabled(config, config.getVideoEnabled(), "DAILY_TASK_DISABLED", "消保视频已关闭");
        if (request == null || request.getSessionId() == null || request.getVersion() == null || request.getPositionSeconds() == null) fail("VIDEO_HEARTBEAT_CONFLICT", "视频心跳参数无效");
        if (request.getClientAt() != null && Math.abs(System.currentTimeMillis() - request.getClientAt()) > 5 * 60 * 1000L) fail("VIDEO_HEARTBEAT_CONFLICT", "客户端时间偏差过大");
        DailyTaskVideoSession session = videoSessionMapper.selectByUserDateForUpdate(userId, today().toString());
        if (session == null || !session.getId().equals(request.getSessionId())) fail("VIDEO_HEARTBEAT_CONFLICT", "视频会话不存在");
        if ("CLAIMED".equals(session.getStatus()) || "EXPIRED".equals(session.getStatus())) fail("VIDEO_HEARTBEAT_CONFLICT", "视频会话已结束");
        if (!request.getVersion().equals(session.getVersion())) fail("VIDEO_HEARTBEAT_CONFLICT", "视频心跳版本冲突");
        BigDecimal position = request.getPositionSeconds();
        if (position.compareTo(BigDecimal.ZERO) < 0 || position.compareTo(BigDecimal.valueOf(session.getVideoDurationSeconds()).add(BigDecimal.ONE)) > 0) fail("VIDEO_HEARTBEAT_CONFLICT", "播放位置无效");
        BigDecimal previous = session.getLastPositionSeconds() == null ? BigDecimal.ZERO : session.getLastPositionSeconds();
        BigDecimal delta = position.subtract(previous);
        if (delta.compareTo(new BigDecimal("-0.25")) < 0 || delta.compareTo(MAX_HEARTBEAT_JUMP) > 0) fail("VIDEO_HEARTBEAT_CONFLICT", "播放跳跃超过心跳容差");
        LocalDateTime current = now();
        long gap = session.getLastHeartbeatAt() == null ? 0 : Math.max(0, Duration.between(session.getLastHeartbeatAt(), current).getSeconds());
        if (session.getLastHeartbeatAt() != null && gap > MAX_HEARTBEAT_GAP_SECONDS) fail("VIDEO_HEARTBEAT_CONFLICT", "心跳间隔过长，请继续正常播放");
        long sinceStart = Math.max(0, Duration.between(session.getStartedAt(), current).getSeconds());
        BigDecimal allowance = session.getLastHeartbeatAt() == null ? BigDecimal.valueOf(Math.min(15, sinceStart)) : BigDecimal.valueOf(gap);
        BigDecimal validDelta = delta.max(BigDecimal.ZERO).min(allowance);
        int credited = Math.min(session.getVideoDurationSeconds(), BigDecimal.valueOf(session.getCreditedWatchSeconds()).add(validDelta).setScale(0, RoundingMode.FLOOR).intValue());
        String nextStatus = credited >= session.getRequiredWatchSeconds() ? "QUALIFIED" : "STARTED";
        int updated = videoSessionMapper.update(new LambdaUpdateWrapper<DailyTaskVideoSession>()
                .eq(DailyTaskVideoSession::getId, session.getId()).eq(DailyTaskVideoSession::getVersion, session.getVersion())
                .set(DailyTaskVideoSession::getCreditedWatchSeconds, credited).set(DailyTaskVideoSession::getLastPositionSeconds, previous.max(position))
                .set(DailyTaskVideoSession::getLastHeartbeatAt, current).set(DailyTaskVideoSession::getStatus, nextStatus)
                .set(DailyTaskVideoSession::getVersion, session.getVersion() + 1));
        if (updated != 1) fail("VIDEO_HEARTBEAT_CONFLICT", "视频心跳版本冲突");
        session.setCreditedWatchSeconds(credited).setLastPositionSeconds(previous.max(position)).setLastHeartbeatAt(current)
                .setStatus(nextStatus).setVersion(session.getVersion() + 1);
        return videoApi(session, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> claimVideo(String userId, DailyTaskRequests.VideoClaim request) {
        DailyTaskConfig config = config();
        requireEnabled(config, config.getVideoEnabled(), "DAILY_TASK_DISABLED", "消保视频已关闭");
        DailyTaskVideoSession session = videoSessionMapper.selectByUserDateForUpdate(userId, today().toString());
        if (session == null || request == null || !session.getId().equals(request.getSessionId())) fail("VIDEO_HEARTBEAT_CONFLICT", "视频会话不存在");
        if ("CLAIMED".equals(session.getStatus())) {
            DailyTaskClaim existing = claimMapper.selectByUserDateType(userId, today().toString(), VIDEO);
            return existing == null ? walletSummary(userId, config.getVideoRewardPoints(), BigDecimal.ZERO, "ALREADY_CLAIMED")
                    : walletSummary(userId, existing.getRequestedPoints(), existing.getAwardedPoints(), "ALREADY_CLAIMED");
        }
        if (!"QUALIFIED".equals(session.getStatus())) fail("VIDEO_NOT_ENOUGH_WATCH", "视频有效观看时长不足");
        BigDecimal requested = money(config.getVideoRewardPoints(), "20.00");
        // 事件 10 的幂等粒度是 user + taskDate，而不是客户端可见的视频会话 ID。
        // 会话 ID 仍保存在 daily_task_claim.source 中用于审计；结算事件必须固定到自然日，
        // 这样并发申领不会因为产生两个会话/请求而突破每日一次规则。
        RewardPositiveResult reward = rewardPositiveService.reward(userId, today().toString(), UserWalletEventEnum.DAILY_VIDEO.getKey(), requested);
        session.setStatus("CLAIMED").setClaimedAt(now()); videoSessionMapper.updateById(session);
        insertTaskClaim(userId, today(), VIDEO, "video:" + session.getVideoId(), requested, new BigDecimal(reward.getAwardedPoints()), reward.getAwardReason(), reward.getWalletRecordId());
        return rewardMap(reward);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> claimAffair(String userId, DailyTaskRequests.AffairClaim request) {
        DailyTaskConfig config = config(); requireEnabled(config, config.getAffairEnabled(), "DAILY_TASK_DISABLED", "民生实事任务已关闭");
        LocalDate date = today(); DailyTaskClaim existing = claimMapper.selectByUserDateType(userId, date.toString(), AFFAIR);
        if (existing != null) return walletSummary(userId, existing.getRequestedPoints(), existing.getAwardedPoints(), "ALREADY_CLAIMED");
        String affairId = request == null ? null : request.getAffairId();
        Introduce affair = introduceMapper.selectOne(new LambdaQueryWrapper<Introduce>().eq(Introduce::getId, affairId).eq(Introduce::getStatus, true));
        if (affair == null) fail("AFFAIR_NOT_FOUND", "民生实事不存在或已停用");
        BigDecimal requested = money(config.getAffairRewardPoints(), "10.00");
        // 事件 11 的幂等粒度是 user + taskDate；实事 ID 只作为来源审计，不作为奖励事件 ID。
        RewardPositiveResult reward = rewardPositiveService.reward(userId, date.toString(), UserWalletEventEnum.DAILY_AFFAIR.getKey(), requested);
        insertTaskClaim(userId, date, AFFAIR, "affair:" + affairId, requested, new BigDecimal(reward.getAwardedPoints()), reward.getAwardReason(), reward.getWalletRecordId());
        return rewardMap(reward);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> prepareShare(String userId, DailyTaskRequests.SharePrepare request) {
        DailyTaskConfig config = config(); requireEnabled(config, config.getShareEnabled(), "DAILY_TASK_DISABLED", "保存/分享任务已关闭");
        if (request == null || !Set.of("SAVE", "SHARE").contains(request.getActionType()) || !Set.of("POSTER", "VIDEO", "H5").contains(request.getAssetType())
                || request.getAssetId() == null || request.getAssetId().isBlank()) fail("ACTION_TOKEN_INVALID", "保存/分享参数无效");
        validateShareAsset(userId, request.getAssetType(), request.getAssetId());
        byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LocalDateTime expires = now().plusMinutes(ACTION_TOKEN_TTL_MINUTES);
        DailyTaskActionToken row = new DailyTaskActionToken().setId(IdUtil.getSnowflakeNextIdStr()).setTokenHash(sha256(token))
                .setUserId(userId).setActionType(request.getActionType()).setAssetType(request.getAssetType()).setAssetId(request.getAssetId())
                .setExpiresAt(expires).setCreatedAt(now());
        actionTokenMapper.insert(row);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("expiresAt", format(expires));
        result.put("actionType", row.getActionType());
        result.put("assetType", row.getAssetType());
        result.put("assetId", row.getAssetId());
        result.put("asset", Map.of("type", row.getAssetType(), "id", row.getAssetId()));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> claimShare(String userId, DailyTaskRequests.ShareClaim request) {
        DailyTaskConfig config = config();
        requireEnabled(config, config.getShareEnabled(), "DAILY_TASK_DISABLED", "保存/分享任务已关闭");
        if (request == null || request.getToken() == null || request.getToken().isBlank()) fail("ACTION_TOKEN_INVALID", "分享令牌无效");
        if (!Set.of("SAVE", "SHARE").contains(request.getActionType()) || !Set.of("POSTER", "VIDEO", "H5").contains(request.getAssetType())
                || request.getAssetId() == null || request.getAssetId().isBlank()) fail("ACTION_TOKEN_MISMATCH", "动作或资产参数无效");
        DailyTaskActionToken token = actionTokenMapper.selectForUpdate(sha256(request.getToken()));
        if (token == null) fail("ACTION_TOKEN_INVALID", "分享令牌不存在");
        if (!userId.equals(token.getUserId())) fail("ACTION_TOKEN_MISMATCH", "分享令牌不属于当前用户");
        if (!request.getActionType().equals(token.getActionType()) || !request.getAssetType().equals(token.getAssetType())
                || !request.getAssetId().equals(token.getAssetId())) fail("ACTION_TOKEN_MISMATCH", "动作或资产与令牌不匹配");
        if (token.getUsedAt() != null) fail("ACTION_TOKEN_REUSED", "分享令牌已经使用");
        if (now().isAfter(token.getExpiresAt())) fail("ACTION_TOKEN_EXPIRED", "分享令牌已过期");
        LocalDate date = today(); DailyTaskClaim existing = claimMapper.selectByUserDateType(userId, date.toString(), SHARE);
        if (existing != null) {
            token.setUsedAt(now());
            actionTokenMapper.updateById(token);
            return walletSummary(userId, existing.getRequestedPoints(), existing.getAwardedPoints(), "ALREADY_CLAIMED");
        }
        token.setUsedAt(now()); actionTokenMapper.updateById(token);
        BigDecimal requested = money(config.getShareRewardPoints(), "10.00");
        // 保存和分享共用一个自然日事件，防止两个不同令牌并发绕过合并日限次。
        RewardPositiveResult reward = rewardPositiveService.reward(userId, date.toString(), UserWalletEventEnum.DAILY_SHARE_OR_SAVE.getKey(), requested);
        insertTaskClaim(userId, date, SHARE, token.getActionType().toLowerCase() + ":" + token.getAssetType().toLowerCase() + ":" + token.getAssetId(), requested,
                new BigDecimal(reward.getAwardedPoints()), reward.getAwardReason(), reward.getWalletRecordId());
        return rewardMap(reward);
    }

    private Map<String, Object> rewardMap(RewardPositiveResult reward) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("completed", reward.isCompleted()); map.put("requestedPoints", reward.getRequestedPoints()); map.put("awardedPoints", reward.getAwardedPoints());
        map.put("awardReason", reward.getAwardReason()); map.put("dailyEarnedPoints", reward.getDailyEarnedPoints());
        map.put("dailyRemainingPoints", reward.getDailyRemainingPoints()); map.put("walletPoints", reward.getWalletPoints());
        map.put("walletRecordId", reward.getWalletRecordId()); map.put("duplicate", reward.isDuplicate());
        return map;
    }

    private Map<String, Object> walletSummary(String userId, BigDecimal requested, BigDecimal awarded, String reason) {
        UserWallet wallet = walletMapper.selectOne(new LambdaQueryWrapper<UserWallet>().eq(UserWallet::getUserId, userId).eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));
        BigDecimal amount = wallet == null ? BigDecimal.ZERO : wallet.getAmount();
        LocalDate date = today(); BigDecimal earned = wallet == null ? BigDecimal.ZERO : positiveBetween(wallet.getId(), date);
        if (earned == null) earned = BigDecimal.ZERO;
        DailyTaskConfig config = config(); BigDecimal limit = money(config.getDailyPositiveMaxPoints(), "60.00");
        Map<String, Object> map = new LinkedHashMap<>(); map.put("completed", true); map.put("requestedPoints", format(requested)); map.put("awardedPoints", format(awarded));
        map.put("awardReason", reason); map.put("dailyEarnedPoints", format(earned)); map.put("dailyRemainingPoints", format(maxZero(limit.subtract(earned)))); map.put("walletPoints", format(amount));
        return map;
    }

    private List<Map<String, Object>> quizQuestions(DailyTaskQuizSession session, boolean includeAnswers) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DailyTaskQuizQuestion question : quizQuestionMapper.listBySession(session.getId())) {
            Map<String, Object> snapshot = readJson(question.getQuestionSnapshot());
            Map<String, Object> item = new LinkedHashMap<>(); item.put("sequenceNo", question.getSequenceNo()); item.put("topicId", question.getTopicId());
            item.put("content", snapshot.get("content")); item.put("options", snapshot.get("options"));
            if (includeAnswers) { item.put("isCorrect", question.getCorrect()); item.put("awardedPoints", format(question.getAwardedPoints())); }
            list.add(item);
        }
        return list;
    }

    private Map<String, Object> videoApi(DailyTaskVideoSession session, boolean resumed) {
        Map<String, Object> map = new LinkedHashMap<>(); map.put("sessionId", session.getId()); map.put("videoId", session.getVideoId());
        map.put("durationSeconds", session.getVideoDurationSeconds()); map.put("requiredWatchSeconds", session.getRequiredWatchSeconds());
        map.put("creditedWatchSeconds", session.getCreditedWatchSeconds()); map.put("version", session.getVersion()); map.put("status", session.getStatus());
        map.put("eligible", "QUALIFIED".equals(session.getStatus()) || "CLAIMED".equals(session.getStatus())); map.put("resumed", resumed); return map;
    }

    private void insertTaskClaim(String userId, LocalDate date, String type, String source, BigDecimal requested, BigDecimal awarded, String reason, String walletRecordId) {
        DailyTaskClaim row = new DailyTaskClaim().setId(IdUtil.getSnowflakeNextIdStr()).setUserId(userId).setTaskDate(date).setTaskType(type)
                .setSource(source.length() > 64 ? source.substring(0, 64) : source).setRequestedPoints(money(requested, "0.00"))
                .setAwardedPoints(money(awarded, "0.00")).setAwardReason(reason).setWalletRecordId(walletRecordId).setCompletedAt(now());
        try { claimMapper.insert(row); } catch (DuplicateKeyException ignored) { /* 同一事务重试返回既有完成记录 */ }
    }

    private DailyTaskConfig config() {
        DailyTaskConfig config = configService.get();
        if (config == null) config = new DailyTaskConfig().setId(1).setEnabled(false).setQuizEnabled(true).setVideoEnabled(true).setAffairEnabled(true).setShareEnabled(true)
                .setWalletMaxPoints(new BigDecimal("4000.00")).setDailyPositiveMaxPoints(new BigDecimal("60.00")).setQuizDailyAttempts(1).setQuizQuestionCount(2)
                .setQuizRewardPerCorrect(new BigDecimal("10.00")).setQuizTimeLimitSeconds(120).setVideoDailyCount(1).setVideoRewardPoints(new BigDecimal("20.00"))
                .setVideoMinWatchRatio(new BigDecimal("0.5000")).setAffairDailyCount(1).setAffairRewardPoints(new BigDecimal("10.00"))
                .setShareDailyCount(1).setShareRewardPoints(new BigDecimal("10.00")).setPosterCacheDays(7).setVersion(1L);
        return config;
    }

    private void validateShareAsset(String userId, String assetType, String assetId) {
        if ("H5".equals(assetType)) {
            if (!"home".equals(assetId)) fail("ACTION_ASSET_NOT_FOUND", "H5 资产未发布");
            return;
        }
        if ("VIDEO".equals(assetType)) {
            Video video = videoMapper.selectOne(new LambdaQueryWrapper<Video>().eq(Video::getId, assetId).eq(Video::getStatus, true));
            if (video == null) fail("ACTION_ASSET_NOT_FOUND", "视频不存在或已停用");
            return;
        }
        PosterGeneration generation = generationMapper.selectOne(new LambdaQueryWrapper<PosterGeneration>()
                .eq(PosterGeneration::getId, assetId).eq(PosterGeneration::getUserId, userId));
        if (generation == null || !"SUCCEEDED".equals(generation.getStatus()) || generation.getExpiresAt() == null || !generation.getExpiresAt().isAfter(now())) {
            fail("ACTION_ASSET_NOT_FOUND", "海报不存在或已过期");
        }
    }

    private BigDecimal positiveBetween(String walletId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay(ZONE).toLocalDateTime();
        LocalDateTime end = date.plusDays(1).atStartOfDay(ZONE).toLocalDateTime();
        return walletRecordMapper.sumPositiveBetween(walletId, start, end);
    }

    private Map<String, Object> configMap(DailyTaskConfig config) {
        Map<String, Object> map = new LinkedHashMap<>(); map.put("id", config.getId()); map.put("enabled", Boolean.TRUE.equals(config.getEnabled()));
        map.put("quizEnabled", Boolean.TRUE.equals(config.getQuizEnabled())); map.put("videoEnabled", Boolean.TRUE.equals(config.getVideoEnabled()));
        map.put("affairEnabled", Boolean.TRUE.equals(config.getAffairEnabled())); map.put("shareEnabled", Boolean.TRUE.equals(config.getShareEnabled()));
        map.put("walletMaxPoints", format(config.getWalletMaxPoints())); map.put("dailyPositiveMaxPoints", format(config.getDailyPositiveMaxPoints()));
        map.put("quizDailyAttempts", config.getQuizDailyAttempts()); map.put("quizQuestionCount", config.getQuizQuestionCount()); map.put("quizRewardPerCorrect", format(config.getQuizRewardPerCorrect()));
        map.put("quizTimeLimitSeconds", config.getQuizTimeLimitSeconds()); map.put("videoDailyCount", config.getVideoDailyCount()); map.put("videoRewardPoints", format(config.getVideoRewardPoints()));
        map.put("videoMinWatchRatio", config.getVideoMinWatchRatio() == null ? "0.5000" : config.getVideoMinWatchRatio().setScale(4, RoundingMode.DOWN).toPlainString());
        map.put("affairDailyCount", config.getAffairDailyCount()); map.put("affairRewardPoints", format(config.getAffairRewardPoints())); map.put("shareDailyCount", config.getShareDailyCount());
        map.put("shareRewardPoints", format(config.getShareRewardPoints())); map.put("posterCacheDays", config.getPosterCacheDays()); map.put("version", config.getVersion()); return map;
    }

    private static void requireEnabled(DailyTaskConfig config, Boolean enabled, String code, String message) { if (!Boolean.TRUE.equals(config.getEnabled()) || !Boolean.TRUE.equals(enabled)) fail(code, message); }
    private static void fail(String code, String message) { throw new YunKeException(ExceptionEnum.FAIL, code + ":" + message); }
    private static LocalDate today() { return LocalDate.now(ZONE); }
    private static LocalDateTime now() { return LocalDateTime.now(ZONE); }
    private static String format(LocalDateTime value) { return value == null ? null : value.format(TIME); }
    private static String format(BigDecimal value) { return money(value, "0.00").toPlainString(); }
    private static BigDecimal money(BigDecimal value, String fallback) { return (value == null ? new BigDecimal(fallback) : value).setScale(2, RoundingMode.DOWN); }
    private static int positive(Integer value, int fallback) { return value == null || value < 1 ? fallback : value; }
    private static BigDecimal maxZero(BigDecimal value) { return value.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : value.setScale(2, RoundingMode.DOWN); }
    private static String json(Object object) { try { return new ObjectMapper().writeValueAsString(object); } catch (JsonProcessingException e) { throw new IllegalStateException(e); } }
    private Map<String, Object> readJson(String value) { try { return objectMapper.readValue(value, Map.class); } catch (Exception e) { return Map.of("content", "", "options", List.of()); } }
    private static String sha256(String value) { try { return HexFormatHolder.hex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); } }
    private static BigDecimal sumRequested(List<DailyTaskQuizQuestion> rows) { return rows.stream().map(row -> row.getRequestedPoints() == null ? BigDecimal.ZERO : row.getRequestedPoints()).reduce(BigDecimal.ZERO, BigDecimal::add); }
    private static BigDecimal sumAwarded(List<DailyTaskQuizQuestion> rows) { return rows.stream().map(row -> row.getAwardedPoints() == null ? BigDecimal.ZERO : row.getAwardedPoints()).reduce(BigDecimal.ZERO, BigDecimal::add); }
    private static final class HexFormatHolder { static String hex(byte[] bytes) { StringBuilder result = new StringBuilder(bytes.length * 2); for (byte value : bytes) result.append(String.format("%02x", value)); return result.toString(); } }
    private static final class QuizExpiredException extends YunKeException {
        private QuizExpiredException() { super(ExceptionEnum.FAIL, "QUIZ_EXPIRED:答题已超时"); }
    }
}
