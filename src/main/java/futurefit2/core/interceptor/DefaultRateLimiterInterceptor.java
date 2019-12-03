package futurefit2.core.interceptor;

import com.google.common.util.concurrent.RateLimiter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultRateLimiterInterceptor implements RequestInterceptor {

    private RateLimiter rateLimiter;

    public DefaultRateLimiterInterceptor(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public Object intercept(RequestInvocation invocation) {
        log.debug("> acquiring lock on rate limiter");
        this.rateLimiter.acquire();
        log.debug("< acquiring lock on rate limiter OK");

        return invocation.invoke();
    }

}
