package com.yunkesoftware.www.web.controller;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yunkesoftware.www.result.CommonResult;

import com.yunkesoftware.www.web.config.WxMpProperties;
import com.yunkesoftware.www.web.query.CreateSignQuery;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;



@CrossOrigin
@RestController
@RequestMapping("/sdk")
public class JsSdkController {
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String JSAPI_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";
    private static final String ACCESS_TOKEN_KEY = "wx:mp:access_token:";
    private static final String JSAPI_TICKET_KEY = "wx:mp:jsapi_ticket:";

    @Resource
    private WxMpProperties wxMpProperties;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/get")
    @Operation(summary = "获取凭证")
    public CommonResult<String> JsapiTicket() {
        String jsapiTicket = loadJsapiTicket(false);
        return CommonResult.success(jsapiTicket);
    }

    @Operation(summary = "获取凭证(强制刷新)")
    @GetMapping("/getJsapiTicket/forceRefresh")
    public CommonResult<String> getJsapiTicket(boolean forceRefresh) {
        String jsapiTicket = loadJsapiTicket(forceRefresh);
        return CommonResult.success(jsapiTicket);
    }

    @Operation(summary = "获取签名")
    @PostMapping("/create")
    public CommonResult<Object> JsapiSignature(@RequestBody CreateSignQuery query,
                                               @RequestParam(defaultValue = "false") boolean forceRefresh) {
        if (query == null || !StringUtils.hasText(query.getUrl())) {
            return CommonResult.validateFailed("签名地址不能为空");
        }
        String jsapiTicket = loadJsapiTicket(forceRefresh);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = RandomUtil.randomString(16);
        String signatureText = "jsapi_ticket=" + jsapiTicket
                + "&noncestr=" + nonceStr
                + "&timestamp=" + timestamp
                + "&url=" + query.getUrl();

        WxMpProperties.MpConfig mpConfig = getMpConfig();
        Map<String, Object> jsapiSignature = new LinkedHashMap<>();
        jsapiSignature.put("appId", mpConfig.getAppId());
        jsapiSignature.put("nonceStr", nonceStr);
        jsapiSignature.put("timestamp", timestamp);
        jsapiSignature.put("url", query.getUrl());
        jsapiSignature.put("signature", SecureUtil.sha1(signatureText));
        return CommonResult.success(jsapiSignature);
    }

    private String loadJsapiTicket(boolean forceRefresh) {
        WxMpProperties.MpConfig mpConfig = getMpConfig();
        String ticketKey = JSAPI_TICKET_KEY + mpConfig.getAppId();
        if (!forceRefresh) {
            Object cachedTicket = redisTemplate.opsForValue().get(ticketKey);
            if (cachedTicket != null && StringUtils.hasText(String.valueOf(cachedTicket))) {
                return String.valueOf(cachedTicket);
            }
        }

        String accessToken = getAccessToken(forceRefresh);
        JSONObject ticketResult = requestJson(String.format(JSAPI_TICKET_URL, encode(accessToken)));
        int errCode = ticketResult.getInt("errcode", 0);
        if (isAccessTokenExpired(errCode)) {
            accessToken = getAccessToken(true);
            ticketResult = requestJson(String.format(JSAPI_TICKET_URL, encode(accessToken)));
            errCode = ticketResult.getInt("errcode", 0);
        }
        if (errCode != 0) {
            throw new IllegalStateException("获取微信 jsapi_ticket 失败：" + ticketResult.getStr("errmsg"));
        }

        String ticket = ticketResult.getStr("ticket");
        Integer expiresIn = ticketResult.getInt("expires_in", 7200);
        redisTemplate.opsForValue().set(ticketKey, ticket, cacheSeconds(expiresIn), TimeUnit.SECONDS);
        return ticket;
    }

    private String getAccessToken(boolean forceRefresh) {
        WxMpProperties.MpConfig mpConfig = getMpConfig();
        String tokenKey = ACCESS_TOKEN_KEY + mpConfig.getAppId();
        if (!forceRefresh) {
            Object cachedToken = redisTemplate.opsForValue().get(tokenKey);
            if (cachedToken != null && StringUtils.hasText(String.valueOf(cachedToken))) {
                return String.valueOf(cachedToken);
            }
        }

        JSONObject tokenResult = requestJson(String.format(ACCESS_TOKEN_URL, encode(mpConfig.getAppId()), encode(mpConfig.getSecret())));
        Integer errCode = tokenResult.getInt("errcode");
        if (errCode != null && errCode != 0) {
            throw new IllegalStateException("获取微信 access_token 失败：" + tokenResult.getStr("errmsg"));
        }

        String accessToken = tokenResult.getStr("access_token");
        Integer expiresIn = tokenResult.getInt("expires_in", 7200);
        redisTemplate.opsForValue().set(tokenKey, accessToken, cacheSeconds(expiresIn), TimeUnit.SECONDS);
        return accessToken;
    }

    private WxMpProperties.MpConfig getMpConfig() {
        if (wxMpProperties.getConfigs() == null || wxMpProperties.getConfigs().isEmpty()) {
            throw new IllegalStateException("未配置微信公众号信息");
        }
        WxMpProperties.MpConfig mpConfig = wxMpProperties.getConfigs().get(0);
        if (!StringUtils.hasText(mpConfig.getAppId()) || !StringUtils.hasText(mpConfig.getSecret())) {
            throw new IllegalStateException("微信公众号 appId 或 secret 不能为空");
        }
        return mpConfig;
    }

    private JSONObject requestJson(String url) {
        return JSONUtil.parseObj(HttpUtil.get(url, 5000));
    }

    private int cacheSeconds(Integer expiresIn) {
        return Math.max((expiresIn == null ? 7200 : expiresIn) - 300, 60);
    }

    private boolean isAccessTokenExpired(int errCode) {
        return errCode == 40001 || errCode == 40014 || errCode == 41001 || errCode == 42001;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
