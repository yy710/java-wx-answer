package com.yunkesoftware.www.adm.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;

public class BooleanConverter implements Converter<Boolean> {

    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Boolean> context) throws Exception {
        return new WriteCellData<>(context.getValue() ? "是" : "否");
    }
}
