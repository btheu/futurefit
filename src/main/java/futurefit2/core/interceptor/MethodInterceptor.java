package futurefit2.core.interceptor;

import java.lang.annotation.Annotation;

import futurefit2.core.RequestFacade;

/**
 * 
 * @author Benoit Theunissen
 *
 */
public interface MethodInterceptor {

    public void intercept(RequestFacade requestFacade, Annotation[] annotations, Object response);

}
