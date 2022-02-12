package futurefit2.core.interceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.util.concurrent.RateLimiter;

import futurefit2.core.RequestFacadeCallback;
import futurefit2.core.cache.CacheManager;
import futurefit2.core.interceptor.RequestInterceptor.RequestInvocation;

/**
 * 
 * @author Benoit Theunissen
 *
 * @param <T>
 */
public class InterceptorProxyInvocationHandler<T> implements InvocationHandler {

    private T delegate;

    private String baseUrl;

    private RequestFacadeCallback callback;

    private CacheManager cacheManager;

    private RateLimiter rateLimiter;

    private List<RequestInterceptor> interceptors;

    public InterceptorProxyInvocationHandler(T delegate, RequestFacadeCallback callback, CacheManager cacheManager,
            RateLimiter rateLimiter, String baseUrl, List<RequestInterceptor> interceptors) {
        this.baseUrl = baseUrl;
        this.delegate = delegate;
        this.callback = callback;
        this.cacheManager = cacheManager;
        this.rateLimiter = rateLimiter;
        this.interceptors = interceptors;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        DefaultRequestInvocation dri = new DefaultRequestInvocation(delegate, method, args, callback, cacheManager,
                baseUrl, rateLimiter, interceptors);

        return dri.invoke();
    }

    public static class DefaultRequestInvocation implements RequestInvocation {

        Iterator<? extends RequestInterceptor> iterators;

        private boolean executed = false;

        private String baseUrl;
        private Object target;
        private Method method;
        private Object[] args;

        public DefaultRequestInvocation(Object target, Method method, Object[] args, RequestFacadeCallback callback,
                CacheManager cacheManager, String baseUrl, RateLimiter rateLimiter,
                List<RequestInterceptor> interceptors) {
            this.target = target;
            this.method = method;
            this.args = args;
            this.baseUrl = baseUrl;

            List<RequestInterceptor> list = new ArrayList<RequestInterceptor>();
            list.add(new DefaultExceptionInterceptor(baseUrl));
            list.addAll(interceptors);
            list.add(new DefaultMethodInterceptor(callback));
            list.add(new DefaultCacheableInterceptor(cacheManager));
            list.add(new DefaultRateLimiterInterceptor(rateLimiter));
            list.add(new DefaultRequestInterceptor());

            iterators = list.iterator();
        }

        @Override
        public Object invoke() {
            if (executed) {
                throw new RuntimeException("Execution was already made");
            }

            RequestInterceptor next = iterators.next();

            return next.intercept(DefaultRequestInvocation.this);
        }

        @Override
        public String baseUrl() {
            return baseUrl;
        }

        @Override
        public Method method() {
            return method;
        }

        @Override
        public Object target() {
            return target;
        }

        @Override
        public Object[] arguments() {
            return args;
        }

    }

}
