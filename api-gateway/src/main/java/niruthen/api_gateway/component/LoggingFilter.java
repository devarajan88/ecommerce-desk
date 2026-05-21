package niruthen.api_gateway.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final String START_TIME = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Capture start time
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());

        String ipAddress = request.getRemoteAddress() != null 
                ? request.getRemoteAddress().getAddress().getHostAddress() 
                : "Unknown IP";
        
        String query = request.getURI().getQuery() != null ? "?" + request.getURI().getQuery() : "";
        
        log.info("Incoming Request => IP: {}, Method: {}, Path: {}",
                ipAddress, request.getMethod(), request.getURI().getPath() + query);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            Long startTime = exchange.getAttribute(START_TIME);
            long executionTime = (startTime != null) ? (System.currentTimeMillis() - startTime) : 0;

            log.info("Outgoing Response => Path: {}, Status Code: {}, Execution Time: {} ms",
                    request.getURI().getPath(), response.getStatusCode(), executionTime);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // Execute first to capture the most accurate execution time
    }
}
