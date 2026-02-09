package com.yunkesoftware.www.adm.service.impl;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunkesoftware.www.adm.entity.Payment;
import com.yunkesoftware.www.adm.mapper.PaymentMapper;
import com.yunkesoftware.www.adm.service.PaymentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 活动支付信息 服务实现类
 * </p>
 *
 * @author yk
 * @since 2026-01-16
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    @Override
    public Page<Payment> pageByQuery(Payment payment) {
        Page<Payment> pageParam = new Page<>(payment.getPageNum(), payment.getPageSize());
        LambdaQueryWrapper<Payment> queryWrapper = new LambdaQueryWrapper<Payment>()
                .like(StringUtils.hasLength(payment.getTitle()), Payment::getTitle, payment.getTitle());
        Page<Payment> pageResult = baseMapper.selectPage(pageParam, queryWrapper);
        for (Payment record : pageResult.getRecords()) {
            generateQrCode(record);
        }
        return pageResult;
    }


    private void generateQrCode(Payment payment) {
        QrConfig config = new QrConfig(400, 400);
        String img = QrCodeUtil.generateAsBase64(payment.getId(), config, ImgUtil.IMAGE_TYPE_PNG);
        payment.setQrImg(img);
    }
}
