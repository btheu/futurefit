package futurefit2.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import futurefit2.Intercept;

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

        Object invoke = method.invoke(this.delegate, args);

        Intercept annotation = method.getAnnotation(Intercept.class);

        if (annotation != null) {

            Class<? extends RequestInterceptor> handler = annotation.handler();
            if (handler != null) {

                RequestInterceptor newInstance = handler.newInstance();

                ProxyRequestFacade requestFacade = new ProxyRequestFacade();

                newInstance.intercept(requestFacade, method.getAnnotations(), invoke);

                callback.apply(requestFacade);
            }
        }

        return invoke;
    }

}
