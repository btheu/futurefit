package futurefit2.core.interceptor;

import java.io.IOException;

import lombok.Getter;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 
 * @author Benoit Theunissen
 *
 */
public class UserAgentInterceptor implements Interceptor {

    @Getter
    private String userAgent;

    private UserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();

        request = request.newBuilder()//
                .header("User-Agent", userAgent)//
                .build();

        return chain.proceed(request);
    }

    public static UserAgentInterceptor build(String agentName) {
        return new UserAgentInterceptor(agentName);
    }

}
