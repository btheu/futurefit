package futurefit.core;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * @author Benoit Theunissen
 *
 */
@Target({ METHOD })
@Retention(RUNTIME)
public @interface Authentification {

    Class<? extends AuthenticationHandler> handler();

}
