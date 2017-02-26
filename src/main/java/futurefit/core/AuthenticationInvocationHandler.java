package futurefit.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import futurefit.Authentification;

/**
 * 
 * @author Benoit Theunissen
 *
 * @param <T>
 */
public class AuthenticationInvocationHandler<T> implements InvocationHandler {

    private T delegate;
    private AuthenticationExtractor extractor;

    public AuthenticationInvocationHandler(T delegate, AuthenticationExtractor extractor) {
        this.delegate = delegate;
        this.extractor = extractor;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object invoke = method.invoke(this.delegate, args);

        Authentification annotation = method.getAnnotation(Authentification.class);

        if (annotation != null) {

            Class<? extends AuthenticationHandler> handler = annotation.handler();
            if (handler != null) {

                AuthenticationHandler newInstance = handler.newInstance();

                AuthentificationRequestFacade authentificationRequestFacade = new AuthentificationRequestFacade();

                newInstance.handler(authentificationRequestFacade, invoke);

                this.extractor.extracted(authentificationRequestFacade);
            }
        }

        return invoke;
    }

}
