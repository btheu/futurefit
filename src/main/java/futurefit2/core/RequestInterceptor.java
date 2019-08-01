package futurefit2.core;

import java.lang.annotation.Annotation;

public interface RequestInterceptor {

    public void intercept(RequestFacade requestFacade, Annotation[] annotations, Object response);

}