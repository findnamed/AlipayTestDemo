package zjc.starless.alipaydemo.service.impl;


import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import zjc.starless.alipaydemo.mapper.AlipayOrdersBufferDao;
import zjc.starless.alipaydemo.model.AlipayOrdersBuffer;
import zjc.starless.alipaydemo.model.GenericResponse;
import zjc.starless.alipaydemo.service.ISubmitOrderService;
import zjc.starless.alipaydemo.service.OrderTimeoutService;

import java.util.UUID;

import static java.lang.Math.random;

@Service
public class SubmitOrderService implements ISubmitOrderService {

    @Resource
    private AlipayOrdersBufferDao alipayOrdersBufferDao;

    @Resource
    private OrderTimeoutService orderTimeoutService;

    // 测试起见订单超时时间设置为 60秒
    private static final long DEFAULT_ORDER_TIMEOUT = 60L;

    @Override
    public GenericResponse submitOrder() {
        // 生成六位UUID加上当前时间戳
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        String outTradeNo = "order_" + uuid + System.currentTimeMillis();

        // 从0-200随机生成数字然后转换成分
        Long totalAmount = (long) (random() * 200 * 100);

        // 创建订单缓冲记录
        AlipayOrdersBuffer orderBuffer = new AlipayOrdersBuffer();
        orderBuffer.setOutTradeNo(outTradeNo);
        orderBuffer.setInvoiceAmount(totalAmount);
        orderBuffer.setSubject("测试商品" + uuid);
        orderBuffer.setBody("这是商品描述");
        orderBuffer.setTradeStatus("WAIT_BUYER_PAY");
        // 初始设置订单超时时间
        orderBuffer.setRemainingTime(DEFAULT_ORDER_TIMEOUT);

        boolean success = alipayOrdersBufferDao.insert(orderBuffer) > 0;

        if (success) {
            // 设置订单超时取消
            orderTimeoutService.addOrderTimeout(outTradeNo, DEFAULT_ORDER_TIMEOUT);
            return GenericResponse.success(orderBuffer);
        } else {
            return GenericResponse.error("订单创建失败");
        }
    }
}
