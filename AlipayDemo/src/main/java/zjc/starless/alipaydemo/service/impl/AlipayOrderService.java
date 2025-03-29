package zjc.starless.alipaydemo.service.impl;


import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import zjc.starless.alipaydemo.mapper.AlipayOrdersBufferDao;
import zjc.starless.alipaydemo.mapper.AlipayOrdersDao;
import zjc.starless.alipaydemo.model.AlipayOrders;
import zjc.starless.alipaydemo.model.AlipayOrdersBuffer;
import zjc.starless.alipaydemo.model.GenericResponse;
import zjc.starless.alipaydemo.service.IAlipayOrderService;
import zjc.starless.alipaydemo.service.OrderTimeoutService;

import java.util.List;
import java.util.Map;

@Service
public class AlipayOrderService implements IAlipayOrderService {
    @Resource
    private AlipayOrdersDao alipayOrdersDao;

    @Resource
    private AlipayOrdersBufferDao alipayOrdersBufferDao;

    @Resource
    private OrderTimeoutService orderTimeoutService;

    private AlipayOrders getAlipayOrders(Map<String, String> params) {
        AlipayOrders alipayOrders = new AlipayOrders();
        alipayOrders.setSubject(params.get("subject"));
        alipayOrders.setBody(params.get("body"));
        alipayOrders.setOutTradeNo(params.get("out_trade_no"));
        // 金额单位为分
        alipayOrders.setInvoiceAmount((long) (Double.parseDouble(params.get("invoice_amount")) * 100));
        alipayOrders.setNotifyId(params.get("notify_id"));
        alipayOrders.setTradeStatus(params.get("trade_status"));
        alipayOrders.setTradeNo(params.get("trade_no"));
        alipayOrders.setBuyerId(Long.valueOf(params.get("buyer_id")));
        alipayOrders.setSellerId(Long.valueOf(params.get("seller_id")));
        return alipayOrders;
    }

    @Override
    public void insertOrder(Map<String, String> params) {
        AlipayOrders alipayOrders = getAlipayOrders(params);

        try {
            alipayOrdersDao.insert(alipayOrders);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean updateOrderStatus(Map<String, String> params) {
        try {
            AlipayOrders alipayOrders = getAlipayOrders(params);
            return alipayOrdersBufferDao.updateOrderBuffer(alipayOrders) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public GenericResponse getOrderBufferList() {
        List<AlipayOrdersBuffer> alipayOrdersBuffers = alipayOrdersBufferDao.selectOrderBufferList();
        // 从redis zSet中查询订单超时时间
        if (alipayOrdersBuffers != null && !alipayOrdersBuffers.isEmpty()) {
            for (AlipayOrdersBuffer order : alipayOrdersBuffers) {
                // 获取订单剩余时间
                long orderRemainingTime = orderTimeoutService.getOrderRemainingTime(order.getOutTradeNo());
                order.setRemainingTime(orderRemainingTime);
            }
        }
        return GenericResponse.success(alipayOrdersBuffers);
    }
}
