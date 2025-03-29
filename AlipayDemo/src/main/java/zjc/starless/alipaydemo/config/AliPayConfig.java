package zjc.starless.alipaydemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置类
 */

@Data
@Component
@ConfigurationProperties(prefix = "alipay") // 读取配置文件中的 alipay 配置
public class AliPayConfig {
    private String appId;
    private String appPrivateKey;
    private String alipayPublicKey;
    private String notifyUrl;

}
