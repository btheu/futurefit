package futurefit2.core.interceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;

import futurefit2.core.RequestFacadeCallback;
import futurefit2.core.interceptor.Interceptor.RequestInvocation;

/**
 * 
 * @author Benoit Theunissen
 *
 * @param <T>
 */
public class InterceptorProxyInvocationHandler<T> implements InvocationHandler {

    private T delegate;

    private RequestFacadeCallback callback;

    public InterceptorProxyInvocationHandler(T delegate, RequestFacadeCallback callback) {
        this.delegate = delegate;
        this.callback = callback;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        DefaultRequestInvocation dri = new DefaultRequestInvocation(delegate, method, args);

        return dri.invoke();
    }

    public static class DefaultRequestInvocation implements RequestInvocation {

        Iterator<? extends Interceptor> iterators;

        private boolean executed = false;

        private Object   target;
        private Method   method;
        private Object[] args;

        public DefaultRequestInvocation(Object target, Method method, Object[] args) {
            this.target = target;
            this.method = method;
            this.args = args;
            iterators = Collections.singleton(new DefaultResquestInterceptor()).iterator();
        }

        @Override
        public Object invoke() {
            if (executed) {
                throw new RuntimeException("Execution was already made");
            }

            Interceptor next = iterators.next();

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
