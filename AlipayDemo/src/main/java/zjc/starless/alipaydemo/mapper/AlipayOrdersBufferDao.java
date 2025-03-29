package zjc.starless.alipaydemo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import zjc.starless.alipaydemo.model.AlipayOrders;
import zjc.starless.alipaydemo.model.AlipayOrdersBuffer;

import java.util.List;

/**
 * 订单缓冲表Mapper
 */
@Mapper
public interface AlipayOrdersBufferDao extends BaseMapper<AlipayOrdersBuffer> {
    int updateOrderBuffer(AlipayOrders order);

    // 获取订单缓冲列表
    List<AlipayOrdersBuffer> selectOrderBufferList();
}
