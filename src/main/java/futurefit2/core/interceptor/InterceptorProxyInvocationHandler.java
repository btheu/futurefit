package futurefit2.core.interceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ehcache.CacheManager;

import futurefit2.core.RequestFacadeCallback;
import futurefit2.core.interceptor.RequestInterceptor.RequestInvocation;

/**
 * 
 * @author Benoit Theunissen
 *
 * @param <T>
 */
public class InterceptorProxyInvocationHandler<T> implements InvocationHandler {

    private T delegate;

    private RequestFacadeCallback callback;

    private CacheManager cacheManager;

    public InterceptorProxyInvocationHandler(T delegate, RequestFacadeCallback callback, CacheManager cacheManager) {
        this.delegate = delegate;
        this.callback = callback;
        this.cacheManager = cacheManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        DefaultRequestInvocation dri = new DefaultRequestInvocation(delegate, method, args, callback, cacheManager);

        return dri.invoke();
    }

    public static class DefaultRequestInvocation implements RequestInvocation {

        Iterator<? extends RequestInterceptor> iterators;

        private boolean executed = false;

        private Object   target;
        private Method   method;
        private Object[] args;

        public DefaultRequestInvocation(Object target, Method method, Object[] args, RequestFacadeCallback callback,
                CacheManager cacheManager) {
            this.target = target;
            this.method = method;
            this.args = args;

            List<RequestInterceptor> list = new ArrayList<RequestInterceptor>();
            list.add(new DefaultMethodInterceptor(callback));
            list.add(new DefaultCacheableInterceptor(cacheManager));
            list.add(new DefaultResquestInterceptor());

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
