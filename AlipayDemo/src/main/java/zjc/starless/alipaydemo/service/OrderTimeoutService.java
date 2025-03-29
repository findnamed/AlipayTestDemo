package zjc.starless.alipaydemo.service;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import zjc.starless.alipaydemo.mapper.AlipayOrdersBufferDao;
import zjc.starless.alipaydemo.model.AlipayOrdersBuffer;
import zjc.starless.alipaydemo.model.utils.RedisCache;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
//import java.util.Set;

@Service
@Slf4j
public class OrderTimeoutService {
    private static final String ORDER_TIMEOUT_KEY = "order:timeout";

    @Resource
    private RedisCache redisCache;

    @Resource
    private AlipayOrdersBufferDao alipayOrdersBufferDao;


    /**
     * 添加订单超时任务
     *
     * @param outTradeNo     订单号
     * @param timeoutSeconds 超时时间(秒)
     */
    public void addOrderTimeout(String outTradeNo, Long timeoutSeconds) {
        // 当前时间加上超时时间算出超时时间
        double expireTime = Instant.now().getEpochSecond() + timeoutSeconds;
        redisCache.zAdd(ORDER_TIMEOUT_KEY, outTradeNo, expireTime);
        log.info("订单: {} 已添加超时任务，将在 {} 秒后超时", outTradeNo, timeoutSeconds);
    }

    /**
     * 移除订单超时任务（支付成功时调用）
     */
    public void removeOrderTimeout(String outTradeNo) {
        redisCache.zRemove(ORDER_TIMEOUT_KEY, outTradeNo);
        log.info("订单: {} 的超时任务已移除", outTradeNo);
    }

    /**
     * 获取订单剩余支付时间(秒)
     */
    public long getOrderRemainingTime(String outTradeNo) {
        Double expireTime = redisCache.zScore(ORDER_TIMEOUT_KEY, outTradeNo);
        if (expireTime == null) {
            return -1L;
        }

        long currentTime = Instant.now().getEpochSecond();
        long remainingTime = (long) (expireTime - currentTime);
        return remainingTime > 0 ? remainingTime : -1;
    }

    /**
     * 定时扫描并处理超时订单，每10秒执行一次
     */
    @Scheduled(fixedRate = 10000)
    public void processTimeoutOrders() {
        long currentTime = Instant.now().getEpochSecond();
        // 获取score小于当前时间的所有订单
        Set<Object> timeoutOrders = redisCache.zRangeByScore(ORDER_TIMEOUT_KEY, 0, currentTime);
        // 遍历超时订单
        if (timeoutOrders != null && !timeoutOrders.isEmpty()) {
            log.info("发现 {} 个超时订单需要处理", timeoutOrders.size());

            for (Object order : timeoutOrders) {
                String outTradeNo = (String) order;
                try {
                    handleTimeoutOrders(outTradeNo);

                    // 处理完超时订单后从zSet中删除
                    redisCache.zRemove(ORDER_TIMEOUT_KEY, outTradeNo);
                } catch (Exception e) {
                    log.info("处理超时订单 {} 失败: {}", outTradeNo, e.getMessage());
                }
            }
        }

    }

    /**
     * 处理超时订单
     */
    private void handleTimeoutOrders(String outTradeNo) {
        log.info("处理超时订单: {}", outTradeNo);

        // 更新订单状态为已取消
        AlipayOrdersBuffer order = new AlipayOrdersBuffer();
        order.setOutTradeNo(outTradeNo);
        order.setTradeStatus("TRADE_CLOSED");
        order.setUpdateTime(new Date());

        // 更新订单状态
        alipayOrdersBufferDao.updateOrderBuffer(order);

        log.info("订单: {} 已超时关闭", outTradeNo);
    }

}
