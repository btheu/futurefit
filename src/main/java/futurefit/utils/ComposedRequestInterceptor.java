package futurefit.utils;

import java.util.ArrayList;

import retrofit.RequestInterceptor;

/**
 * Usefull for aggregating {@link RequestInterceptor}
 * 
 * @author Benoit Theunissen
 *
 */
public class ComposedRequestInterceptor extends ArrayList<RequestInterceptor> implements RequestInterceptor {

    private static final long serialVersionUID = 116634643912928295L;

    public void intercept(RequestFacade request) {
        for (RequestInterceptor req : this) {
            req.intercept(request);
        }
    }

}
