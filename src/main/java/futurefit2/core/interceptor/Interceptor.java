package futurefit2.core.interceptor;

import java.lang.reflect.Method;

public interface Interceptor {

    Object intercept(RequestInvocation invocation);

    public static interface RequestInvocation {

        Object invoke();

        Method method();

        Object target();

        Object[] arguments();

    }

}
