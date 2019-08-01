package futurefit2;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import futurefit2.core.RequestInterceptor;

@Target({ METHOD })
@Retention(RUNTIME)
public @interface Intercept {

    Class<? extends RequestInterceptor> handler();

}
