package zjc.starless.alipaydemo.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zjc.starless.alipaydemo.config.AliPayConfig;
import zjc.starless.alipaydemo.model.AliPayRequest;
import zjc.starless.alipaydemo.model.GenericResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AliPayService {

    @Resource
    private AliPayConfig aliPayConfig;

    @Resource
    private IAlipayOrderService alipayOrderService;

    @Resource
    private OrderTimeoutService orderTimeoutService;


    // 支付宝网关（沙箱环境）
    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    public GenericResponse<Object> placeOrderForPCWeb(AliPayRequest aliPayRequest) throws IOException {
        // 构建支付宝客户端
        AlipayClient alipayClient = new DefaultAlipayClient(GATEWAY_URL, aliPayConfig.getAppId(), aliPayConfig.getAppPrivateKey(), "json", "UTF-8", aliPayConfig.getAlipayPublicKey(), "RSA2");

        // 创建API请求对象
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
//        request.setReturnUrl("http://localhost:5173/payment/return"); // 前端同步回调地址
        request.setReturnUrl("http://localhost:8080/alipay/callback/sync"); // 直接使用后端验证
        request.setNotifyUrl(aliPayConfig.getNotifyUrl()); // 后端异步回调地址

        // 构造请求参数
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", aliPayRequest.getOutTradeNo());
        bizContent.put("total_amount", aliPayRequest.getTotalAmount());
        bizContent.put("subject", aliPayRequest.getSubject());
        bizContent.put("body", aliPayRequest.getBody());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY"); // PC支付产品码

        request.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));

        try {
            // 调用SDK生成表单
            String form = alipayClient.pageExecute(request).getBody();
            return GenericResponse.success(form);
        } catch (AlipayApiException e) {
            log.error("支付宝下单失败", e);
            return GenericResponse.error("支付宝下单失败: " + e.getMessage());
        }
    }

    /**
     * 依赖于后端的回调，用于处理支付业务信息，支付宝通过公网回调该接口
     */
    public String orderCallbackInAsync(HttpServletRequest request) {
        // 获取支付宝回调参数
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append((i == values.length - 1) ? values[i] : values[i] + ",");
            }
            params.put(key, valueStr.toString());
        }

        try {
            // 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(params, aliPayConfig.getAlipayPublicKey(), "UTF-8", "RSA2");
            log.info("signVerified: {}", signVerified);
            if (signVerified) {
                // 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验
                String tradeStatus = params.get("trade_status");
                String outTradeNo = params.get("out_trade_no");
                log.info(params.toString());
//                System.out.println(params);
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // 支付成功，更新订单状态
                    log.info("订单支付成功: {}， 支付宝订单号为：{}", outTradeNo, params.get("trade_no"));
                    // 支付成功，取消超时任务
                    orderTimeoutService.removeOrderTimeout(outTradeNo);
                    // 这里应该添加更新订单状态的逻辑
                    alipayOrderService.insertOrder(params);
                    boolean success = alipayOrderService.updateOrderStatus(params);
                    if (success) {
                        log.info("订单缓存表状态更新成功");
                    } else {
                        log.error("订单缓存表状态更新失败");
                    }
                }
                return "success"; // 返回给支付宝的响应
            } else {
                log.error("支付宝回调签名验证失败");
                return "failure";
            }
        } catch (AlipayApiException e) {
            log.error("处理支付宝异步回调异常", e);
            return "failure";
        }
    }

    /**
     * 依赖于用户浏览器的回调，用于改善用户体验
     */
    public void orderCallbackInSync(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(key, valueStr);
        }

        try {
            // 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(params, aliPayConfig.getAlipayPublicKey(), "UTF-8", "RSA2");

            if (signVerified) {
                // 同步回调主要是用于页面跳转，通常不做业务处理
                log.info("同步回调验签成功");
                // 重定向到前端的支付结果页
                response.sendRedirect("http://localhost:5173/payment/result?success=true");
            } else {
                log.error("同步回调验签失败");
                response.sendRedirect("http://localhost:5173/payment/result?success=false");
            }
        } catch (Exception e) {
            log.error("处理同步回调异常", e);
            try {
                response.sendRedirect("http://localhost:5173/payment/result?success=false");
            } catch (IOException ex) {
                log.error("重定向异常", ex);
            }
        }
    }
}