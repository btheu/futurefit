package futurefit.core;

import retrofit.RequestInterceptor.RequestFacade;

/**
 * 
 * @author Benoit Theunissen
 *
 */
public interface AuthenticationHandler {

    void handler(RequestFacade requestFacade, Object response);

}
