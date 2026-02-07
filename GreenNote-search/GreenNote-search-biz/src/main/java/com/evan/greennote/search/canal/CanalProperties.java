package com.evan.greennote.search.canal;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//Canal 配置读取
@ConfigurationProperties(prefix = CanalProperties.PREFIX)
@Component
@Data
public class CanalProperties {
    public static final String PREFIX = "canal";
    //Canal 链接地址
    private String address;
    //数据目标
    private String destination;
    //用户名
    private String username;
    //密码
    private String password;
    //订阅规则
    private String subscribe;
    //一批次拉取数据量，默认 1000 条
    private int batchSize = 1000;
    
    // 连接超时时间（毫秒），默认30秒
    private int connectionTimeout = 30000;
    // 获取数据超时时间（毫秒），默认10秒
    private int fetchTimeout = 10000;
    // 重连间隔时间（毫秒），默认5秒
    private int reconnectInterval = 5000;
    // 最大重连次数，默认10次
    private int maxReconnectAttempts = 10;
}
