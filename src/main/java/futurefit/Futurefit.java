package futurefit;

import java.lang.reflect.Proxy;
import java.util.concurrent.Executor;

import futurefit.core.AuthenticationExtractor;
import futurefit.core.AuthenticationInvocationHandler;
import futurefit.core.AuthentificationRequestFacade;
import futurefit.core.AuthentificationRequestInterceptor;
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
public class Futurefit {

    private final retrofit.RestAdapter.Builder builder;

    public static class Builder {

        private final RestAdapter.Builder builder;

        public Builder() {
            this.builder = new RestAdapter.Builder();
        }

        public Builder setClient(Client client) {
            this.builder.setClient(client);
            return this;
        };

        public Builder setClient(Provider provider) {
            this.builder.setClient(provider);
            return this;
        };

        public Builder setConverter(Converter converter) {
            this.builder.setConverter(converter);
            return this;
        };

        public Builder setEndpoint(String endPoint) {
            this.builder.setEndpoint(endPoint);
            return this;
        };

        public Builder setEndpoint(Endpoint endPoint) {
            this.builder.setEndpoint(endPoint);
            return this;
        };

        public Builder setErrorHandler(ErrorHandler errorHandler) {
            this.builder.setErrorHandler(errorHandler);
            return this;
        };

        public Builder setExecutors(Executor e1, Executor e2) {
            this.builder.setExecutors(e1, e2);
            return this;
        };

        public Builder setLog(Log log) {
            this.builder.setLog(log);
            return this;
        };

        public Builder setLogLevel(LogLevel level) {
            this.builder.setLogLevel(level);
            return this;
        };

        public Builder setProfiler(Profiler<?> profiler) {
            this.builder.setProfiler(profiler);
            return this;
        };

        public Builder setRequestInterceptor(RequestInterceptor requestInterceptor) {
            this.builder.setRequestInterceptor(requestInterceptor);
            return this;
        };

        public Futurefit build() {
            return new Futurefit(this.builder);
        }

    }

    private Futurefit(retrofit.RestAdapter.Builder builder) {
        this.builder = builder;
    }

    public <T> T create(Class<T> class1) {

        final AuthentificationRequestInterceptor tokenInterceptor = new AuthentificationRequestInterceptor() {

            public void intercept(RequestFacade request) {

                if (this.authentificationRequestFacade != null) {
                    switch (this.authentificationRequestFacade.getType()) {
                    case ENCODED:
                        request.addHeader(this.authentificationRequestFacade.getName(),
                                this.authentificationRequestFacade.getValue());
                        break;
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

        T retrofitAdapter = this.builder.setRequestInterceptor(tokenInterceptor).build().create(class1);

        return createAuthProxy(class1, retrofitAdapter, new AuthenticationExtractor() {

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
