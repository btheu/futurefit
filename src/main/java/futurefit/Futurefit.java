package futurefit;

import java.lang.reflect.Proxy;
import java.util.concurrent.Executor;

import futurefit.core.AuthenticationExtractor;
import futurefit.core.AuthenticationInvocationHandler;
import futurefit.core.AuthentificationRequestFacade;
import futurefit.core.AuthentificationRequestInterceptor;
import futurefit.utils.ComposedRequestInterceptor;
import futurefit.utils.VoidRequestInterceptor;
import retrofit.Endpoint;
import retrofit.ErrorHandler;
import retrofit.Profiler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RestAdapter.Log;
import retrofit.RestAdapter.LogLevel;
import retrofit.client.Client;
import retrofit.client.Client.Provider;
import retrofit.converter.Converter;

/**
 * 
 * @author Benoit Theunissen
 *
 */
@Deprecated
public class Futurefit {

    private final RestAdapter.Builder restBuilder;
    private RequestInterceptor        interceptor;

    public static class Builder {

        protected final RestAdapter.Builder restBuilder;

        protected RequestInterceptor interceptor = new VoidRequestInterceptor();

        public Builder() {
            this.restBuilder = new RestAdapter.Builder();
        }

        public Builder setClient(Client client) {
            this.restBuilder.setClient(client);
            return this;
        };

        public Builder setClient(Provider provider) {
            this.restBuilder.setClient(provider);
            return this;
        };

        public Builder setConverter(Converter converter) {
            this.restBuilder.setConverter(converter);
            return this;
        };

        public Builder setEndpoint(String endPoint) {
            this.restBuilder.setEndpoint(endPoint);
            return this;
        };

        public Builder setEndpoint(Endpoint endPoint) {
            this.restBuilder.setEndpoint(endPoint);
            return this;
        };

        public Builder setErrorHandler(ErrorHandler errorHandler) {
            this.restBuilder.setErrorHandler(errorHandler);
            return this;
        };

        public Builder setExecutors(Executor e1, Executor e2) {
            this.restBuilder.setExecutors(e1, e2);
            return this;
        };

        public Builder setLog(Log log) {
            this.restBuilder.setLog(log);
            return this;
        };

        public Builder setLogLevel(LogLevel level) {
            this.restBuilder.setLogLevel(level);
            return this;
        };

        public Builder setProfiler(Profiler<?> profiler) {
            this.restBuilder.setProfiler(profiler);
            return this;
        };

        public Builder setRequestInterceptor(RequestInterceptor requestInterceptor) {
            interceptor = requestInterceptor;
            return this;
        };

        public Futurefit build() {
            return new Futurefit(this.restBuilder, this.interceptor);
        }

    }

    private Futurefit(RestAdapter.Builder restBuilder, RequestInterceptor interceptor) {
        this.restBuilder = restBuilder;
        this.interceptor = interceptor;
    }

    public <T> T create(Class<T> apiClass) {

        final AuthentificationRequestInterceptor tokenInterceptor = new AuthentificationRequestInterceptor() {

            @Override
            public void intercept(RequestFacade request) {

                if (this.authentificationRequestFacade != null) {
                    switch (this.authentificationRequestFacade.getType()) {
                    case ENCODED_PATH_PARAM:
                        request.addEncodedPathParam(this.authentificationRequestFacade.getName(),
                                this.authentificationRequestFacade.getValue());
                        break;
                    case ENCODED_QUERY_PARAM:
                        request.addEncodedQueryParam(this.authentificationRequestFacade.getName(),
                                this.authentificationRequestFacade.getValue());
                        break;
                    case HEADER:
                        request.addHeader(this.authentificationRequestFacade.getName(),
                                this.authentificationRequestFacade.getValue());
                        break;
                    case PATH_PARAM:
                        request.addPathParam(this.authentificationRequestFacade.getName(),
                                this.authentificationRequestFacade.getValue());
                        break;
                    case QUERY_PARAM:
                        request.addQueryParam(this.authentificationRequestFacade.getName(),
                                this.authentificationRequestFacade.getValue());
                        break;
                    default:
                        throw new RuntimeException(
                                "Type not implemented: " + this.authentificationRequestFacade.getType().name());
                    }
                }
            }
        };

        final ComposedRequestInterceptor interceptors = new ComposedRequestInterceptor();
        interceptors.add(tokenInterceptor);
        interceptors.add(interceptor);

        T retrofitAdapter = this.restBuilder.setRequestInterceptor(interceptors).build().create(apiClass);

        return createAuthProxy(apiClass, retrofitAdapter, new AuthenticationExtractor() {

            @Override
            public void extracted(AuthentificationRequestFacade authentificationRequestFacade) {
                tokenInterceptor.setAuthentificationRequestFacade(authentificationRequestFacade);
            }
        });

    }

    @SuppressWarnings("unchecked")
    private static <T> T createAuthProxy(Class<T> targetInterface, T delegate, AuthenticationExtractor extractor) {

        return (T) Proxy.newProxyInstance(targetInterface.getClassLoader(), new Class<?>[] { targetInterface },
                new AuthenticationInvocationHandler<T>(delegate, extractor));
    }

}
