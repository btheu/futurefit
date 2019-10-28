package futurefit.utils;

import retrofit.RequestInterceptor;

/**
 * <p>
 * MethodInterceptor doing nothing.
 * <p>
 * Usefull with {@link ComposedRequestInterceptor}
 * 
 * @author Benoit Theunissen
 *
 */
@Deprecated
public class VoidRequestInterceptor implements RequestInterceptor {

    @Override
    public void intercept(RequestFacade request) {
    }

}
