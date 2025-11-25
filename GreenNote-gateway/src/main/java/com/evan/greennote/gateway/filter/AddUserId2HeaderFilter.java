package com.evan.greennote.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Order(-90)
public class AddUserId2HeaderFilter implements GlobalFilter {

    private static final String HEADER_USER_ID="userId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("===============> TokenConvertFilter");

        Long userId=null;
        try{
            userId= StpUtil.getLoginIdAsLong();
        }catch(Exception e){
            return chain.filter(exchange);
        }

        log.info("## 当前登录用户 ID：{}",userId);

        Long finalUserId=userId;
        ServerWebExchange newExchange=exchange.mutate()
                .request(builder->builder.header(HEADER_USER_ID,String.valueOf(finalUserId)))
                .build();
        return chain.filter(newExchange);
    }
}
