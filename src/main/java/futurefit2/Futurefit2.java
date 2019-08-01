package futurefit2;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URL;

import futurefit2.convertor.EstivateConverterFactory;
import futurefit2.core.InterceptorProxyInvocationHandler;
import futurefit2.core.ProxyRequestFacade;
import futurefit2.core.RequestFacadeCallback;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;

/**
 * 
 * @author Benoit Theunissen
 *
 */
public class Futurefit2 {

    private final Retrofit.Builder retrofitBuilder;

    private final RequestUpdateInterceptor requestUpdateInterceptor;

    public static class Builder {

        protected final Retrofit.Builder         retrofitBuilder;
        protected final RequestUpdateInterceptor requestUpdateInterceptor = new RequestUpdateInterceptor();

        protected boolean client = false;

        public Builder() {
            this.retrofitBuilder = new Retrofit.Builder();
            this.retrofitBuilder.addConverterFactory(EstivateConverterFactory.create());
            this.retrofitBuilder
                    .client(new OkHttpClient.Builder().addNetworkInterceptor(requestUpdateInterceptor).build());
        }

        public Retrofit.Builder retrofit() {
            return retrofitBuilder;
        };

        public Builder client(OkHttpClient client) {
            OkHttpClient newClient = client.newBuilder().addNetworkInterceptor(requestUpdateInterceptor).build();
            this.retrofitBuilder.client(newClient);
            this.client = true;
            return this;
        };

        public Builder baseUrl(String baseUrl) {
            this.retrofitBuilder.baseUrl(baseUrl);
            return this;
        };

        public Builder baseUrl(URL baseUrl) {
            this.retrofitBuilder.baseUrl(baseUrl);
            return this;
        };

        public Builder baseUrl(HttpUrl baseUrl) {
            this.retrofitBuilder.baseUrl(baseUrl);
            return this;
        };

        public Futurefit2 build() {
            return new Futurefit2(this.retrofitBuilder, requestUpdateInterceptor);
        }

    }

    private Futurefit2(retrofit2.Retrofit.Builder retrofitBuilder, RequestUpdateInterceptor requestUpdateInterceptor) {
        this.retrofitBuilder = retrofitBuilder;
        this.requestUpdateInterceptor = requestUpdateInterceptor;
    }

    public <T> T create(Class<T> apiClass) {

        T retrofitAdapter = this.retrofitBuilder.build().create(apiClass);

        return createInterceptorProxy(apiClass, retrofitAdapter, new RequestFacadeCallback() {
            @Override
            public void apply(ProxyRequestFacade requestFacade) {
                requestUpdateInterceptor.setRequestUpdates(requestFacade);
            }
        });

    }

    @SuppressWarnings("unchecked")
    private static <T> T createInterceptorProxy(Class<T> targetInterface, T delegate, RequestFacadeCallback callback) {
        return (T) Proxy.newProxyInstance(targetInterface.getClassLoader(), new Class<?>[] { targetInterface },
                new InterceptorProxyInvocationHandler<T>(delegate, callback));
    }

    public static class RequestUpdateInterceptor implements Interceptor {

        private ProxyRequestFacade requestFacade;

        @Override
        public Response intercept(Chain chain) throws IOException {

            if (requestFacade == null) {
                return chain.proceed(chain.request());
            }

            okhttp3.HttpUrl.Builder newUrl = chain.request().url().newBuilder();

            okhttp3.Request.Builder newRequest = chain.request().newBuilder();

            switch (this.requestFacade.getType()) {
            case NONE:
                break;
            case HEADER:
                newRequest.header(this.requestFacade.getName(), this.requestFacade.getValue());
                break;
            case QUERY_PARAM:
                newUrl.addQueryParameter(this.requestFacade.getName(), this.requestFacade.getValue());
                break;
            case ENCODED_QUERY_PARAM:
                newUrl.addEncodedQueryParameter(this.requestFacade.getName(), this.requestFacade.getValue());
                break;
            case PATH_PARAM:
            case ENCODED_PATH_PARAM:
            default:
                throw new RuntimeException("Type not implemented: " + this.requestFacade.getType().name());
            }

            newRequest.url(newUrl.build());

            return chain.proceed(newRequest.build());
        }

        public void setRequestUpdates(ProxyRequestFacade requestFacade) {
            this.requestFacade = requestFacade;
        }

    }
}
