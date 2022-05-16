package com.niici.springcloud.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * 网关过滤器 -- 用于日志记录
 * 实现GlobalFilter Ordered接口，可用于日志记录和统一鉴权
 */
@Slf4j
@Component
public class GatewayLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("----------gateway log filter----------");
        // 打印请求路径
        log.info("contextpath: {}", exchange.getRequest().getPath().value());
        return chain.filter(exchange);
    }

    /**
     * 指定过滤器执行顺序，值越小, 越先执行
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
