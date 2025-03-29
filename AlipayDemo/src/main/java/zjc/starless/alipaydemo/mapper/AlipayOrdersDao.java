package zjc.starless.alipaydemo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import zjc.starless.alipaydemo.model.AlipayOrders;

/**
 * (AlipayOrders)表数据库访问层
 *
 * @author AtlasKK
 * @since 2025-03-26 22:16:03
 */
@Mapper
public interface AlipayOrdersDao extends BaseMapper<AlipayOrders> {


}

