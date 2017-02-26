package futurefit.utils;

import retrofit.RequestInterceptor;

/**
 * <p>
 * RequestInterceptor doing nothing.
 * <p>
 * Usefull with {@link ComposedRequestInterceptor}
 * 
 * @author Benoit Theunissen
 *
 */
public class VoidRequestInterceptor implements RequestInterceptor {

    public void intercept(RequestFacade request) {
    }

}
