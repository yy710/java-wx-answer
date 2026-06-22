package com.yunkesoftware.www.web.service.impl;


import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yunkesoftware.www.enums.UserWalletEventEnum;
import com.yunkesoftware.www.enums.UserWalletTypeEnum;
import com.yunkesoftware.www.exception.ExceptionEnum;
import com.yunkesoftware.www.exception.YunKeException;
import com.yunkesoftware.www.utils.NickNameUtil;
import com.yunkesoftware.www.utils.ViewNumUtils;
import com.yunkesoftware.www.web.entity.InviteSet;
import com.yunkesoftware.www.web.entity.User;
import com.yunkesoftware.www.web.entity.UserWallet;
import com.yunkesoftware.www.web.entity.UserWalletRecord;
import com.yunkesoftware.www.web.mapper.InviteSetMapper;
import com.yunkesoftware.www.web.mapper.UserMapper;
import com.yunkesoftware.www.web.mapper.UserWalletMapper;
import com.yunkesoftware.www.web.mapper.UserWalletRecordMapper;
import com.yunkesoftware.www.web.service.UserService;
import com.yunkesoftware.www.web.vo.LoginVo;
import com.yunkesoftware.www.web.vo.UserVo;
import jakarta.annotation.Resource;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.service.WxOAuth2Service;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 客户信息 服务实现类
 * </p>
 *
 * @author cuiyq
 * @since 2024-10-24
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private WxMpService wxMpService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserWalletMapper userWalletMapper;
    @Resource
    private UserWalletRecordMapper userWalletRecordMapper;
    @Resource
    private InviteSetMapper inviteSetMapper;


    @Override
    public UserVo info(String userId) {
        UserVo userVo = new UserVo();
        User user = baseMapper.selectById(userId);
        if (user != null) {
            BeanUtils.copyProperties(user, userVo);
            userVo.setPic(normalizeWxAvatarUrl(userVo.getPic()));
        }

        return userVo;
    }

    @Override
    public void updateWithClear(UserVo userVo) {
        User user = new User();
        user.setId(userVo.getId());
        user.setPic(userVo.getPic());
        user.setNickName(userVo.getNickName());
        baseMapper.updateById(user);

    }


    /**
     * 微信授权登录
     */
    public Map<String, Object> wxAuth(LoginVo loginVo) {
        Map<String, Object> resultMap = new HashMap<>();

        String openId = "123456";
        WxOAuth2UserInfo wxUserInfo = null;
        if (StringUtils.hasLength(loginVo.getCode())) {
            try {
                WxOAuth2Service oAuth2Service = wxMpService.getOAuth2Service();
                WxOAuth2AccessToken accessToken = oAuth2Service.getAccessToken(loginVo.getCode());
                openId = accessToken.getOpenId();
                try {
                    wxUserInfo = oAuth2Service.getUserInfo(accessToken, "zh_CN");
                } catch (WxErrorException ignored) {
                }
            } catch (WxErrorException e) {
                throw new YunKeException(ExceptionEnum.FAIL, "微信授权异常-请刷新重试");
            }
        }

        User checkData = baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenId, openId));
        if (checkData == null) {
            checkData = new User();
            fillWxUserInfo(checkData, wxUserInfo, true);
            if (!StringUtils.hasLength(checkData.getNickName())) {
                checkData.setNickName(NickNameUtil.generateOne());
            }
            checkData.setOpenId(openId);
            checkData.setCreateTime(LocalDateTime.now());
            if (StringUtils.hasLength(loginVo.getParentId())) {
                checkData.setParentId(loginVo.getParentId());
            }
            baseMapper.insert(checkData);
            // 有邀请人则赠送邀请人积分
            if (StringUtils.hasLength(loginVo.getParentId())) {
                // 检查上级是否存在
                User parent = baseMapper.selectById(loginVo.getParentId());
                if (parent != null) {
                    InviteSet inviteSet = inviteSetMapper.selectOne(new LambdaQueryWrapper<InviteSet>().last("LIMIT 1"));
                    if (inviteSet != null && inviteSet.getRewardAmount().compareTo(BigDecimal.ZERO) > 0) {
                        UserWallet userWallet = userWalletMapper.selectOne(new LambdaQueryWrapper<UserWallet>()
                                .eq(UserWallet::getUserId, loginVo.getParentId())
                                .eq(UserWallet::getType, UserWalletTypeEnum.INTEGRAL.getKey()));

                        UserWalletRecord walletRecord = new UserWalletRecord();
                        walletRecord.setChangeAmount(inviteSet.getRewardAmount());
                        walletRecord.setEventId(checkData.getId());
                        walletRecord.setEventType(UserWalletEventEnum.INVITE_REWARD.getKey());
                        if (userWallet != null) {
                            // 检查奖励次数是否达上限
                            Long checkCount = userWalletRecordMapper.selectCount(new LambdaQueryWrapper<UserWalletRecord>()
                                    .eq(UserWalletRecord::getWalletId, userWallet.getId())
                                    .eq(UserWalletRecord::getEventType, UserWalletEventEnum.INVITE_REWARD.getKey()));
                            if (checkCount < inviteSet.getRewardLimit()) {
                                //执行奖励赠送
                                BigDecimal afterAmount = userWallet.getAmount().add(inviteSet.getRewardAmount());
                                int updateRow = userWalletMapper.update(new LambdaUpdateWrapper<UserWallet>()
                                        .eq(UserWallet::getId, userWallet.getId())
                                        .eq(UserWallet::getVersion, userWallet.getVersion())
                                        .set(UserWallet::getAmount, afterAmount)
                                        .set(UserWallet::getVersion, userWallet.getVersion() + 1));
                                walletRecord.setStatus(updateRow > 0);
                                walletRecord.setAfterAmount(afterAmount);
                                walletRecord.setWalletId(userWallet.getId());
                                userWalletRecordMapper.insert(walletRecord);
                            }
                        } else {
                            userWallet = new UserWallet();
                            userWallet.setUserId(loginVo.getParentId());
                            userWallet.setType(UserWalletTypeEnum.INTEGRAL.getKey());
                            userWallet.setAmount(inviteSet.getRewardAmount());
                            userWallet.setVersion(0);
                            int insertRow = userWalletMapper.insert(userWallet);
                            walletRecord.setStatus(insertRow > 0);
                            walletRecord.setAfterAmount(userWallet.getAmount());
                            walletRecord.setWalletId(userWallet.getId());
                            userWalletRecordMapper.insert(walletRecord);
                        }
                    }
                }
            }
        } else {
            String oldNickName = checkData.getNickName();
            String oldPic = checkData.getPic();
            checkData.setPic(normalizeWxAvatarUrl(checkData.getPic()));
            boolean overwriteGeneratedProfile = !StringUtils.hasLength(checkData.getPic())
                    && NickNameUtil.isGenerated(checkData.getNickName());
            fillWxUserInfo(checkData, wxUserInfo, overwriteGeneratedProfile);
            if (!Objects.equals(oldNickName, checkData.getNickName()) || !Objects.equals(oldPic, checkData.getPic())) {
                baseMapper.updateById(checkData);
            }
        }
        StpUtil.login(checkData.getId());

        SaTokenInfo resultToken = StpUtil.getTokenInfo();
        resultToken.setTag("0000");
        resultMap.put("userInfo", checkData);
        resultMap.put("token", resultToken);
        return resultMap;
    }

    private void fillWxUserInfo(User user, WxOAuth2UserInfo wxUserInfo, boolean overwrite) {
        if (wxUserInfo == null) {
            return;
        }
        if (StringUtils.hasLength(wxUserInfo.getNickname()) && (overwrite || !StringUtils.hasLength(user.getNickName()))) {
            user.setNickName(wxUserInfo.getNickname());
        }
        String headImgUrl = normalizeWxAvatarUrl(wxUserInfo.getHeadImgUrl());
        if (StringUtils.hasLength(headImgUrl) && (overwrite || !StringUtils.hasLength(user.getPic()))) {
            user.setPic(headImgUrl);
        }
    }

    private String normalizeWxAvatarUrl(String headImgUrl) {
        if (!StringUtils.hasLength(headImgUrl)) {
            return headImgUrl;
        }
        if (headImgUrl.startsWith("http://")) {
            return "https://" + headImgUrl.substring("http://".length());
        }
        return headImgUrl;
    }

    @Override
    public void addViewNum() {
        ViewNumUtils.incrementRealViewNum(redisTemplate);
    }

    @Override
    public Integer getViewNum() {
        return ViewNumUtils.getDisplayViewNum(redisTemplate);
    }

}
