package futurefit.utils;

import java.util.ArrayList;

import retrofit.RequestInterceptor;

/**
 * Usefull for aggregating {@link MethodInterceptor}
 * 
 * @author Benoit Theunissen
 *
 */
@Deprecated
public class ComposedRequestInterceptor extends ArrayList<RequestInterceptor> implements RequestInterceptor {

    private static final long serialVersionUID = 116634643912928295L;

    @Override
    public void intercept(RequestFacade request) {
        for (RequestInterceptor req : this) {
            req.intercept(request);
        }
    }

}
