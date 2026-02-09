package com.yunkesoftware.www.web.controller;

import com.yunkesoftware.www.result.CommonResult;

import com.yunkesoftware.www.web.query.CreateSignQuery;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.web.bind.annotation.*;



@CrossOrigin
@RestController
@RequestMapping("/sdk")
public class JsSdkController {
    @Resource
    private WxMpService wxMpService;

    @GetMapping("/get")
    @Operation(summary = "获取凭证")
    public CommonResult<String> JsapiTicket() throws WxErrorException {
        String jsapiTicket = wxMpService.getJsapiTicket();
        return CommonResult.success(jsapiTicket);
    }

    @Operation(summary = "获取凭证(强制刷新)")
    @GetMapping("/getJsapiTicket/forceRefresh")
    public CommonResult<String> getJsapiTicket(boolean forceRefresh) throws WxErrorException {
        String jsapiTicket = wxMpService.getJsapiTicket(forceRefresh);
        return CommonResult.success(jsapiTicket);
    }

    @Operation(summary = "获取签名")
    @PostMapping("/create")
    public CommonResult<Object> JsapiSignature(@RequestBody CreateSignQuery query) throws WxErrorException {
        WxJsapiSignature jsapiSignature = wxMpService.createJsapiSignature(query.getUrl());
        return CommonResult.success(jsapiSignature);
    }


}
