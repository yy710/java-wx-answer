package com.yunkesoftware.www.adm.aspect;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.annotation.EasyExcelExport;
import com.yunkesoftware.www.converter.XCellStyle;
import com.yunkesoftware.www.result.CommonResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Aspect
@Component
public class ExcelExportAspect {
    /**
     * 是否导出
     */
    private static final String IS_EXPORT = "Is-export";
    /**
     * 对应字段名
     */
    private static final String FIELD_NAME = "Field-name";


    @AfterReturning(returning = "obj", value = "@annotation(excelExport)")
    public void excelExport(Object obj, EasyExcelExport excelExport) throws IOException {
        //获取request与response对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        //确定是否导出
        if (Boolean.parseBoolean(request.getHeader(IS_EXPORT))) {

            HttpServletResponse response = attributes.getResponse();

            JSONArray data = JSON.parseObject(JSON.toJSONString(obj)).getJSONObject("data").getJSONArray("records");
            if (data != null && (((Page) ((CommonResult) obj).getData()).getRecords().size() > 0)) {

                Class<?> aClass = ((Page) ((CommonResult) obj).getData()).getRecords().get(0).getClass();
                String fileName = URLEncoder.encode("".equals(excelExport.fileName()) ? UUID.randomUUID().toString() : excelExport.fileName(), StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename*=utf-8''" + fileName + ExcelTypeEnum.XLSX.getValue());

                List<?> objects = data.toJavaList(aClass);

                String header = request.getHeader(FIELD_NAME);
                if (header != null) {
                    String[] split = header.split(",");
                    EasyExcel.write(response.getOutputStream(), aClass).includeColumnFiledNames(Arrays.asList(split)).sheet("Sheet1")

                        .registerWriteHandler(new XCellStyle())
                        .doWrite(objects);

                } else {
                    EasyExcel.write(response.getOutputStream(), aClass).sheet("Sheet1")
                        .registerWriteHandler(new XCellStyle())
                        .doWrite(objects);
                }

            }
        }
    }

    private Class<?> getFirstRecordClass(JSONArray data) {
        Object firstRecord = data.get(0);
        return firstRecord.getClass();
    }

    private String sanitizeFileName(String fileName) throws IOException {
        String sanitized =
            URLEncoder.encode("".equals(fileName) ? UUID.randomUUID().toString() : fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        return sanitized;
    }
}
