package com.yunkesoftware.www.adm.controller;


import com.yunkesoftware.www.adm.service.DataAnalysisService;
import com.yunkesoftware.www.adm.vo.DataAnalysisVo;
import com.yunkesoftware.www.result.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "数据统计接口")
@RequestMapping("/sys/dataAnalysis")
@RestController
public class DataAnalysisController {
    @Resource
    private DataAnalysisService dataAnalysisService;


    @PostMapping("/indexData")
    public CommonResult<DataAnalysisVo> indexData() {
        DataAnalysisVo analysisVo = dataAnalysisService.indexData();

        return CommonResult.success(analysisVo);
    }
}
