package com.yunkesoftware.www.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class NickNameUtil {
    // 单字（清新/自然/中性可爱）
    private static final String[] SINGLE = {
            "星", "月", "风", "云", "雨", "雪", "光", "影", "林", "山",
            "川", "海", "石", "火", "木", "叶", "芽", "果", "米", "豆",
            "舟", "帆", "桥", "灯", "钟", "墨", "白", "灰", "青", "玄",
            "环", "果", "摇", "木", "兮", "然", "贝", "紫", "珊", "崖"
    };

    // 双字（简洁可爱，无甜腻感）
    private static final String[] DOUBLE = {
            "星野", "林风", "云川", "雨墨", "雪芽", "光舟", "影桥", "山米",
            "海豆", "火叶", "木果", "青芽", "白川", "灰林", "玄月", "风铃",
            "晨露", "夕照", "溪石", "苔痕", "鹿鸣", "鸦羽", "鲸落", "萤火",
            "松果", "栗子", "麦穗", "竹节", "桃核", "梅枝", "梨涡", "杏眼"
    };

    private static final Random rand = ThreadLocalRandom.current();

    /**
     * 生成一个 1～4 字的昵称，可爱但不娘炮
     */
    public static String generateOne() {
        int lenType = rand.nextInt(100);

        if (lenType < 30) {
            // 1字 + 1字（2字）
            return SINGLE[rand.nextInt(SINGLE.length)] + SINGLE[rand.nextInt(SINGLE.length)];
        } else if (lenType < 60) {
            // 直接用双字词（2字）
            return DOUBLE[rand.nextInt(DOUBLE.length)];
        } else if (lenType < 85) {
            // 双字 + 单字（3字）
            String nick = DOUBLE[rand.nextInt(DOUBLE.length)] + SINGLE[rand.nextInt(SINGLE.length)];
            // 10% 概率加数字（变成4字符，如“星野7”）
            if (rand.nextInt(10) == 0) {
                return nick.substring(0, 2) + rand.nextInt(10); // 如“星野3”
            }
            return nick;
        } else {
            // 单字 + 双字（3字） 或 2+2（4字）
            if (rand.nextBoolean()) {
                // 单 + 双（3字）
                return SINGLE[rand.nextInt(SINGLE.length)] + DOUBLE[rand.nextInt(DOUBLE.length)];
            } else {
                // 双 + 双（4字，但可能重复，所以少用）
                return DOUBLE[rand.nextInt(DOUBLE.length)] + DOUBLE[rand.nextInt(DOUBLE.length)];
            }
        }
    }
}
