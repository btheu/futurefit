package futurefit.core;

import retrofit.RequestInterceptor;

/**
 * 
 * @author Benoit Theunissen
 *
 */
public abstract class AuthentificationRequestInterceptor implements RequestInterceptor {

    protected AuthentificationRequestFacade authentificationRequestFacade;

    public void setAuthentificationRequestFacade(AuthentificationRequestFacade authentificationRequestFacade) {
        this.authentificationRequestFacade = authentificationRequestFacade;
    }

}
