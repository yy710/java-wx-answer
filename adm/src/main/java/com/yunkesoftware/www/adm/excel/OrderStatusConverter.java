package com.yunkesoftware.www.adm.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;

public class OrderStatusConverter implements Converter<Integer> {
    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Integer> context) throws Exception {
        String statusDesc = "未知";
        switch (context.getValue()) {
            case 1 -> statusDesc = "待付款";
            case 2 -> statusDesc = "待发货";
            case 3 -> statusDesc = "待收货";
            case 4 -> statusDesc = "待评价";
            case 5 -> statusDesc = "成功";
            case 6 -> statusDesc = "失败";
        }
        return new WriteCellData<>(statusDesc);
    }
}
