package zjc.starless.alipaydemo.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付宝请求参数
 */
@Data
public class AliPayRequest {
    private String outTradeNo; // 订单号
    private BigDecimal totalAmount; // 订单金额
    private String subject; // 订单标题
    private String body; // 订单描述
}