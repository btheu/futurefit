package fr.free.neomcfly.futurefit.core;

import retrofit.RequestInterceptor;

public abstract class AuthentificationRequestInterceptor implements
		RequestInterceptor {

	protected AuthentificationRequestFacade authentificationRequestFacade;

	public void setAuthentificationRequestFacade(
			AuthentificationRequestFacade authentificationRequestFacade) {
		this.authentificationRequestFacade = authentificationRequestFacade;
	}

}
