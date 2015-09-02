package neomcfly.futurefit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicated these method returns a pojo from Jsoup mapping.
 * 
 * @author theunissenb
 *
 */
@Target({ METHOD })
@Retention(RUNTIME)
public @interface Jsoup {

}
