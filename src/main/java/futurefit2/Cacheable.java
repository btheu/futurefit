package futurefit2;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Duration;

/**
 * 
 * @author Benoit Theunissen
 *
 */
@Target({ METHOD })
@Retention(RUNTIME)
public @interface Cacheable {

    String cache();

    /**
     * <p>
     * String representation of {@link Duration}
     * 
     * @see Duration
     * @return String representation of a Duration (aka: PT11H, PT21.345S, P3D)
     */
    String duration() default "PT1H";

    int heap() default 100;

}
