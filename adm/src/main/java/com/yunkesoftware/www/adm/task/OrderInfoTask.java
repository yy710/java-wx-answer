package com.yunkesoftware.www.adm.task;


import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;



@Configuration
public class OrderInfoTask {


    /**
     * 自动确认收货
     * 订单发货后15天自动确认收货
     */
    @Scheduled(cron = "0 0 10 * * ?")
    private void autoReceive() {
        LocalDate nowDate = LocalDate.now();
        // 自提订单24小时自动确认收货

    }
}
