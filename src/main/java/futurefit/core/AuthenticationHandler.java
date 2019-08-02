package futurefit.core;

import retrofit.RequestInterceptor.RequestFacade;

/**
 * 
 * @author Benoit Theunissen
 *
 */
@Deprecated
public interface AuthenticationHandler {

    void handler(RequestFacade requestFacade, Object response);

}
