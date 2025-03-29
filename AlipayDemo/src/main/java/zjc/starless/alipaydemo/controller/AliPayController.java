package zjc.starless.alipaydemo.controller;


import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import zjc.starless.alipaydemo.model.AliPayRequest;
import zjc.starless.alipaydemo.model.GenericResponse;
import zjc.starless.alipaydemo.service.AliPayService;
import zjc.starless.alipaydemo.service.ISubmitOrderService;
import zjc.starless.alipaydemo.service.OrderTimeoutService;
import zjc.starless.alipaydemo.service.impl.AlipayOrderService;

import java.io.IOException;

@RestController
@RequestMapping("/alipay")
// 允许前端跨域访问
public class AliPayController {

    @Resource
    private AliPayService aliPayService;

    @Resource
    private ISubmitOrderService submitOrderService;

    @Resource
    private OrderTimeoutService orderTimeoutService;

    @Resource
    private AlipayOrderService alipayOrderService;


    /**
     * 提交订单
     */
    @GetMapping("/submit-order")
    public GenericResponse submitOrder() {
        return submitOrderService.submitOrder();
    }


    @PostMapping("/order")
    public GenericResponse<Object> placeOrderForPCWeb(@RequestBody AliPayRequest aliPayRequest) {
        try {
            return aliPayService.placeOrderForPCWeb(aliPayRequest);
        } catch (IOException e) {
            return GenericResponse.error("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 异步回调
     */
    @PostMapping("/callback/async")
    public String asyncCallback(HttpServletRequest request) {
        return aliPayService.orderCallbackInAsync(request);
    }

    /**
     * 异步回调 用于展示支付结果或重定向到成功页面
     */
    @GetMapping("/callback/sync")
    public void syncCallback(HttpServletRequest request, HttpServletResponse response) {
        aliPayService.orderCallbackInSync(request, response);
    }

    /**
     * 获取订单剩余时间
     */
    @GetMapping("/order/remaining-time/{outTradeNo}")
    public GenericResponse<Long> getOrderRemainingTime(@PathVariable String outTradeNo) {
        long remainingTime = orderTimeoutService.getOrderRemainingTime(outTradeNo);
        if (remainingTime < 0) {
            return GenericResponse.error("订单不存在或已超时");
        }
        return GenericResponse.success(remainingTime);
    }

    /**
     * 获取订单缓冲表里的信息
     */
    @GetMapping("/order-buffer-list")
    public GenericResponse getOrderBufferList() {
        return alipayOrderService.getOrderBufferList();
    }
}