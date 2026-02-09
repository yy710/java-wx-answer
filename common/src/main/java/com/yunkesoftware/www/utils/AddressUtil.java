package com.yunkesoftware.www.utils;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.*;

public class AddressUtil {
    // 省级行政区列表
    private static final String[] PROVINCES = {
            "北京", "天津", "上海", "重庆", "河北", "山西", "辽宁", "吉林", "黑龙江",
            "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南",
            "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "台湾",
            "内蒙古", "广西", "西藏", "宁夏", "新疆", "香港", "澳门"
    };

    // 综合正则表达式
    private static final String FULL_REGEX =
            "(?<province>" + String.join("|", PROVINCES) + ")(?:省|市|自治区|特别行政区)?" +
                    "(?<city>[^市]+市|[^州]+州|[^盟]+盟|[^地区]+地区)" +
                    "(?<district>[^区]+区|[^县]+县|[^市]+市|[^旗]+旗)?" +
                    "(?<detail>(?:(?!\\d{11}|[\u4e00-\u9fa5]{1,2}收).)+)" + // 关键修改点
                    "(?<name>[\u4e00-\u9fa5]{2,4})" +
                    "(?:收|签)?" +
                    "(?<phone>1[3-9]\\d{9})";


    public static Map<String, String> parseFullInfo(String input) {
        Map<String, String> result = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile(FULL_REGEX);
        Matcher matcher = pattern.matcher(input);

        if(matcher.find()) {
            // 地址部分
            result.put("province", matcher.group("province") + "省");
            result.put("city", matcher.group("city"));
            result.put("county", matcher.group("district"));


            String detail = matcher.group("detail").trim();
            String name = matcher.group("name");
            result.put("detail", detail);
            // 新增信息
            result.put("name",name);
            result.put("phone", matcher.group("phone"));
        }
        return result;
    }

    public static void main(String[] args) {
        String testAddress = "云南省昆明市官渡区樱花语幸福广场赵兴鹏收18088343928";
        Map<String, String> parsed = parseFullInfo(testAddress);
        System.out.println("解析结果: " + parsed);
    }
}


