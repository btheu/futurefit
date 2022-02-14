package futurefit2.core.interceptor;

import java.lang.reflect.Method;

public interface RequestInterceptor {

    Object intercept(RequestInvocation invocation);

    public static interface RequestInvocation {

        String baseUrl();

        Object invoke();

        Method method();

        Object target();

        Object[] arguments();

    }

}
