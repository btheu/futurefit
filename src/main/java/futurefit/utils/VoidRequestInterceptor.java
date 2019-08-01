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
@Deprecated
public class VoidRequestInterceptor implements RequestInterceptor {

    @Override
    public void intercept(RequestFacade request) {
    }

}
