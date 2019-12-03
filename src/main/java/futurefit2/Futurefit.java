package futurefit2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;

import com.google.common.util.concurrent.RateLimiter;

import futurefit2.convertor.BuiltInConverterFactory;
import futurefit2.core.CacheInitializator;
import futurefit2.core.ProxyRequestFacade;
import futurefit2.core.RequestFacadeCallback;
import futurefit2.core.UnboxCallAdapter;
import futurefit2.core.interceptor.HttpLoggingInterceptor;
import futurefit2.core.interceptor.HttpLoggingInterceptor.Level;
import futurefit2.core.interceptor.InterceptorProxyInvocationHandler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

/**
 * 
 * @author Benoit Theunissen
 *
 */
@Slf4j
public class Futurefit {

    private final Retrofit.Builder retrofitBuilder;

    private final RequestUpdateInterceptor requestUpdateInterceptor;

    private final CacheManager cacheManager;
    private final RateLimiter  rateLimiter;

    private static CallAdapter.Factory callAdapterFactory = new CallAdapter.Factory() {
        @Override
        public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
            if (startWithAny(returnType.getTypeName(), Call.class.getCanonicalName())) {
                // skip to default call adapter
                return null;
            }
            log.debug("unbox call adapter for {}", returnType);
            return new UnboxCallAdapter<>(returnType);
        }

        private boolean startWithAny(String typeName, String... string2) {
            for (String string : string2) {
                if (typeName.startsWith(string)) {
                    return true;
                }
            }
            return false;
        }
    };

    public static class Builder {

        protected OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        protected final Retrofit.Builder retrofitBuilder = new Retrofit.Builder();

        protected final RequestUpdateInterceptor requestUpdateInterceptor = new RequestUpdateInterceptor();
        protected final HttpLoggingInterceptor   loggingInterceptor       = new HttpLoggingInterceptor();

        protected CacheManager cacheManager = null;
        protected RateLimiter  rateLimiter  = null;

        public Builder() {
            this.retrofitBuilder.addConverterFactory(BuiltInConverterFactory.create());
            this.retrofitBuilder.addCallAdapterFactory(callAdapterFactory);
        }

        public Retrofit.Builder retrofit() {
            return retrofitBuilder;
        }

        public Builder client(OkHttpClient client) {
            clientBuilder = client.newBuilder();
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.retrofitBuilder.baseUrl(baseUrl);
            return this;
        }

        public Builder baseUrl(URL baseUrl) {
            this.retrofitBuilder.baseUrl(baseUrl);
            return this;
        }

        public Builder baseUrl(HttpUrl baseUrl) {
            this.retrofitBuilder.baseUrl(baseUrl);
            return this;
        }

        public Builder log(Level level) {
            loggingInterceptor.setLevel(level);
            return this;
        }

        public Builder cacheManager(CacheManager cacheManager) {
            this.cacheManager = cacheManager;
            return this;
        }

        public Builder withRateLimiter(RateLimiter rateLimiter) {
            this.rateLimiter = rateLimiter;
            return this;
        }

        /**
         * @see RateLimiter#create(double)
         */
        public Builder withRateLimiter(double permitsPerSecond) {
            this.rateLimiter = RateLimiter.create(permitsPerSecond);
            return this;
        }

        /**
         * @see RateLimiter#create(double, Duration)
         */
        public Builder withRateLimiter(double permitsPerSecond, Duration warmupPeriod) {
            this.rateLimiter = RateLimiter.create(permitsPerSecond, warmupPeriod);
            return this;
        }

        /**
         * @see RateLimiter#create(double, long, TimeUnit)
         */
        public Builder withRateLimiter(double permitsPerSecond, long warmupPeriod, TimeUnit unit) {
            this.rateLimiter = RateLimiter.create(permitsPerSecond, warmupPeriod, unit);
            return this;
        }

        public Futurefit build() {
            clientBuilder //
                    .addNetworkInterceptor(requestUpdateInterceptor) //
                    .addNetworkInterceptor(loggingInterceptor);

            if (cacheManager == null) {
                cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
            }
            if (rateLimiter == null) {
                rateLimiter = RateLimiter.create(10);
            }
            this.retrofitBuilder.client(clientBuilder.build());
            return new Futurefit(this.retrofitBuilder, requestUpdateInterceptor, cacheManager, rateLimiter);
        }

    }

    private Futurefit(retrofit2.Retrofit.Builder retrofitBuilder, RequestUpdateInterceptor requestUpdateInterceptor,
            CacheManager cacheManager, RateLimiter rateLimiter) {
        this.retrofitBuilder = retrofitBuilder;
        this.requestUpdateInterceptor = requestUpdateInterceptor;
        this.cacheManager = cacheManager;
        this.rateLimiter = rateLimiter;
    }

    public <T> T create(Class<T> apiClass) {

        T retrofitAdapter = this.retrofitBuilder.build().create(apiClass);

        CacheInitializator.init(apiClass, cacheManager);

        return createInterceptorProxy(apiClass, retrofitAdapter, new RequestFacadeCallback() {
            @Override
            public void apply(ProxyRequestFacade requestFacade) {
                requestUpdateInterceptor.setRequestUpdates(requestFacade);
            }
        });

    }

    @SuppressWarnings("unchecked")
    private <T> T createInterceptorProxy(Class<T> targetInterface, T delegate, RequestFacadeCallback callback) {
        return (T) Proxy.newProxyInstance(targetInterface.getClassLoader(), new Class<?>[] { targetInterface },
                new InterceptorProxyInvocationHandler<T>(delegate, callback, this.cacheManager, this.rateLimiter));
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
