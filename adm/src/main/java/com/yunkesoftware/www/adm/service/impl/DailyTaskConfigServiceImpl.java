package com.yunkesoftware.www.adm.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunkesoftware.www.adm.entity.DailyTaskConfig;
import com.yunkesoftware.www.adm.mapper.DailyTaskConfigMapper;
import com.yunkesoftware.www.adm.service.DailyTaskConfigService;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DailyTaskConfigServiceImpl extends ServiceImpl<DailyTaskConfigMapper, DailyTaskConfig> implements DailyTaskConfigService {
    @Resource private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyTaskConfig updateWithVersion(DailyTaskConfig incoming, String operatorId) {
        if (incoming == null || incoming.getVersion() == null) throw new YunKeException(ExceptionEnum.FAIL, "CONFIG_VERSION_CONFLICT:配置必须携带 version");
        validate(incoming);
        DailyTaskConfig current = baseMapper.selectById(1);
        if (current == null) throw new YunKeException(ExceptionEnum.FAIL, "配置表未初始化");
        int updated = baseMapper.update(null, new LambdaUpdateWrapper<DailyTaskConfig>()
                .eq(DailyTaskConfig::getId, 1).eq(DailyTaskConfig::getVersion, incoming.getVersion())
                .set(DailyTaskConfig::getEnabled, incoming.getEnabled()).set(DailyTaskConfig::getQuizEnabled, incoming.getQuizEnabled())
                .set(DailyTaskConfig::getVideoEnabled, incoming.getVideoEnabled()).set(DailyTaskConfig::getAffairEnabled, incoming.getAffairEnabled())
                .set(DailyTaskConfig::getShareEnabled, incoming.getShareEnabled()).set(DailyTaskConfig::getWalletMaxPoints, incoming.getWalletMaxPoints())
                .set(DailyTaskConfig::getDailyPositiveMaxPoints, incoming.getDailyPositiveMaxPoints()).set(DailyTaskConfig::getQuizDailyAttempts, incoming.getQuizDailyAttempts())
                .set(DailyTaskConfig::getQuizQuestionCount, incoming.getQuizQuestionCount()).set(DailyTaskConfig::getQuizRewardPerCorrect, incoming.getQuizRewardPerCorrect())
                .set(DailyTaskConfig::getQuizTimeLimitSeconds, incoming.getQuizTimeLimitSeconds()).set(DailyTaskConfig::getVideoDailyCount, incoming.getVideoDailyCount())
                .set(DailyTaskConfig::getVideoRewardPoints, incoming.getVideoRewardPoints()).set(DailyTaskConfig::getVideoMinWatchRatio, incoming.getVideoMinWatchRatio())
                .set(DailyTaskConfig::getAffairDailyCount, incoming.getAffairDailyCount()).set(DailyTaskConfig::getAffairRewardPoints, incoming.getAffairRewardPoints())
                .set(DailyTaskConfig::getShareDailyCount, incoming.getShareDailyCount()).set(DailyTaskConfig::getShareRewardPoints, incoming.getShareRewardPoints())
                .set(DailyTaskConfig::getPosterCacheDays, incoming.getPosterCacheDays()).set(DailyTaskConfig::getUpdatedBy, operatorId)
                .set(DailyTaskConfig::getVersion, incoming.getVersion() + 1));
        if (updated != 1) throw new YunKeException(ExceptionEnum.FAIL, "CONFIG_VERSION_CONFLICT:配置已被其他管理员修改");
        try { redisTemplate.delete("daily-task:config:v1"); } catch (Exception ignored) { /* Redis 失败不阻断 MySQL 事实来源 */ }
        return baseMapper.selectById(1);
    }

    private void validate(DailyTaskConfig value) {
        if (value.getEnabled() == null || value.getQuizEnabled() == null || value.getVideoEnabled() == null || value.getAffairEnabled() == null || value.getShareEnabled() == null
                || value.getWalletMaxPoints() == null || value.getDailyPositiveMaxPoints() == null || value.getQuizRewardPerCorrect() == null
                || value.getVideoRewardPoints() == null || value.getAffairRewardPoints() == null || value.getShareRewardPoints() == null) {
            throw new YunKeException(ExceptionEnum.FAIL, "每日任务配置字段不完整");
        }
        if (money(value.getWalletMaxPoints()).compareTo(BigDecimal.ZERO) < 0 || money(value.getDailyPositiveMaxPoints()).compareTo(BigDecimal.ZERO) < 0
                || money(value.getQuizRewardPerCorrect()).compareTo(BigDecimal.ZERO) <= 0 || money(value.getVideoRewardPoints()).compareTo(BigDecimal.ZERO) <= 0
                || money(value.getAffairRewardPoints()).compareTo(BigDecimal.ZERO) <= 0 || money(value.getShareRewardPoints()).compareTo(BigDecimal.ZERO) <= 0) {
            throw new YunKeException(ExceptionEnum.FAIL, "积分上限或奖励分值无效");
        }
        if (value.getQuizQuestionCount() == null || value.getQuizQuestionCount() < 1 || value.getQuizDailyAttempts() == null || value.getQuizDailyAttempts() < 0 || value.getQuizTimeLimitSeconds() == null || value.getQuizTimeLimitSeconds() < 1) throw new YunKeException(ExceptionEnum.FAIL, "答题配置无效");
        if (!Integer.valueOf(1).equals(value.getQuizDailyAttempts()) || !Integer.valueOf(1).equals(value.getVideoDailyCount())
                || !Integer.valueOf(1).equals(value.getAffairDailyCount()) || !Integer.valueOf(1).equals(value.getShareDailyCount())) {
            throw new YunKeException(ExceptionEnum.FAIL, "每日任务次数必须保持为1");
        }
        if (value.getVideoDailyCount() == null || value.getVideoMinWatchRatio() == null || value.getVideoMinWatchRatio().compareTo(BigDecimal.ZERO) < 0 || value.getVideoMinWatchRatio().compareTo(BigDecimal.ONE) > 0) throw new YunKeException(ExceptionEnum.FAIL, "视频配置无效");
        if (value.getPosterCacheDays() == null || value.getPosterCacheDays() < 1) throw new YunKeException(ExceptionEnum.FAIL, "海报缓存天数无效");
    }
    private BigDecimal money(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
}
