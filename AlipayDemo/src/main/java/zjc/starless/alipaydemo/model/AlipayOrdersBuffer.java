package zjc.starless.alipaydemo.model;


import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

/**
 * (AlipayOrdersBuffer)缓冲表
 *
 * @author AtlasKK
 * @since 2025-03-26 22:14:06
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("alipay_orders_buffer")
public class AlipayOrdersBuffer extends AlipayOrders {
    //主键ID
    private Long id;
    //商户订单号，唯一标识商户的订单。
    private String outTradeNo;
    //支付宝交易号，唯一标识支付宝的交易。
    private String tradeNo;
    //商品名称
    private String subject;
    // 商品描述
    private String body;
    //支付方ID
    private Long buyerId;
    //卖家的支付宝用户ID
    private Long sellerId;
    //订单实际支付的金额
    private Long invoiceAmount;
    //交易状态，TRADE_SUCCESS 表示支付成功。
    private String tradeStatus;
    //通知的唯一标识符
    private String notifyId;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;

    // 订单剩余时间
    @TableField(exist = false)
    private Long remainingTime;

}

