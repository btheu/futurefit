package futurefit2.core.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import futurefit2.Intercept;
import futurefit2.core.ProxyRequestFacade;
import futurefit2.core.RequestFacadeCallback;
import futurefit2.utils.ReflectionUtil;

public class DefaultMethodInterceptor implements RequestInterceptor {

    private RequestFacadeCallback callback;

    public DefaultMethodInterceptor(RequestFacadeCallback callback) {
        this.callback = callback;
    }

    @Override
    public Object intercept(RequestInvocation invocation) {

        Object response = invocation.invoke();

        try {
            Method method = invocation.method();

            Annotation[] annotations = method.getAnnotations();

            Intercept interceptAnnotation = ReflectionUtil.findAnnotation(Intercept.class, annotations);
            if (interceptAnnotation != null && interceptAnnotation.handler() != null) {

                Class<? extends MethodInterceptor> handler = interceptAnnotation.handler();
                MethodInterceptor newInstance = handler.newInstance();

                ProxyRequestFacade requestFacade = new ProxyRequestFacade();

                newInstance.intercept(requestFacade, annotations, response);

                callback.apply(requestFacade);
            }

        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return response;
    }

}
