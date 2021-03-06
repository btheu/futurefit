package futurefit2;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicated these method returns a pojo from Estivate mapping.
 * 
 * @author Benoit Theunissen
 *
 */
@Target({ METHOD })
@Retention(RUNTIME)
public @interface Estivate {

}
