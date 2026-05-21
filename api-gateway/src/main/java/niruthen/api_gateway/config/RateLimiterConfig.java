package niruthen.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {

    /**
     * Rate limits by User IP Address.
     * Use this as the key-resolver in rate limiter configuration.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // In production traffic arrives through a load balancer / reverse proxy.
            // The real caller IP is in the first value of X-Forwarded-For.
            // Fall back to the direct remote address for local / non-proxied environments.
            String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            String ip = (forwarded != null && !forwarded.isBlank())
                    ? forwarded.split(",")[0].trim()
                    : Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                             .getAddress().getHostAddress();
            return Mono.just(ip);
        };
    }
}
