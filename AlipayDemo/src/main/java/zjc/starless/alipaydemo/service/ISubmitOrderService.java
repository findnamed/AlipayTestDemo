package zjc.starless.alipaydemo.service;


import zjc.starless.alipaydemo.model.GenericResponse;

public interface ISubmitOrderService {
    // 下单操作（加载到缓存表中）
    GenericResponse submitOrder();
}
