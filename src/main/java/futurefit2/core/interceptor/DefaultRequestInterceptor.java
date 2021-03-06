package futurefit2.core.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DefaultRequestInterceptor implements RequestInterceptor {

    @Override
    public Object intercept(RequestInvocation invocation) {

        Method method = invocation.method();

        Object target = invocation.target();

        Object[] args = invocation.arguments();

        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
