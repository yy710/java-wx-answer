package com.yunkesoftware.www.adm.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;

public class OrderDvyTypeConverter implements Converter<Integer> {
    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Integer> context) throws Exception {
        String devTypeDesc = "未知";
        switch (context.getValue()) {
            case 1 -> devTypeDesc = "快递";
            case 2 -> devTypeDesc = "自取";
        }
        return new WriteCellData<>(devTypeDesc);
    }
}
