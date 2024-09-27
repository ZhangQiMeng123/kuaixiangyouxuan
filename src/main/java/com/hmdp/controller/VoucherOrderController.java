package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/voucher-order")
@Slf4j
public class VoucherOrderController {
    @Autowired
    private IVoucherOrderService voucherOrderService;
    /**
     * 秒杀下单
     * @param voucherId
     * @return
     */
    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        log.info("实现秒杀下单");
        return voucherOrderService.getSeckillVoucherDetail(voucherId);
    }
}
