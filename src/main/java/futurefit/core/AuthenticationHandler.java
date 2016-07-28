package futurefit.core;

import retrofit.RequestInterceptor.RequestFacade;

public interface AuthenticationHandler {

	void handler(RequestFacade requestFacade, Object response);

}
