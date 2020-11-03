package futurefit2.utils;

import java.lang.reflect.Method;

import lombok.Getter;

/**
 * 
 * @author Benoit Theunissen
 *
 */
@SuppressWarnings("serial")
public class FuturefitException extends RuntimeException {

    @Getter
    protected String url;
    @Getter
    protected Method method;

    public FuturefitException(Method method, String url, Exception e) {
        super("url=" + url, e);
        this.url = url;
        this.method = method;
    }

}
