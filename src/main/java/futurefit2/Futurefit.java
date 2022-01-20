package futurefit2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.google.common.util.concurrent.RateLimiter;

import futurefit2.convertor.BuiltInConverterFactory;
import futurefit2.core.ProxyRequestFacade;
import futurefit2.core.RequestFacadeCallback;
import futurefit2.core.UnboxCallAdapter;
import futurefit2.core.cache.CacheManager;
import futurefit2.core.cache.CacheManagerInitializator;
import futurefit2.core.cache.CacheManagerInitializator.CacheDefinitions;
import futurefit2.core.cache.DefaultCacheManagerProvider;
import futurefit2.core.cache.EhCacheCacheManagerProvider;
import futurefit2.core.cache.SpringCacheManagerProvider;
import futurefit2.core.interceptor.HttpLoggingInterceptor;
import futurefit2.core.interceptor.HttpLoggingInterceptor.Level;
import futurefit2.core.interceptor.InterceptorProxyInvocationHandler;
import futurefit2.core.interceptor.RequestInterceptor;
import futurefit2.core.interceptor.UserAgentInterceptor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * 
 * @author Benoit Theunissen
 *
 */
@Slf4j
public class Futurefit {

    private final String baseUrl;

    private final Retrofit.Builder retrofitBuilder;

    private final RequestUpdateInterceptor requestUpdateInterceptor;

    private final RateLimiter rateLimiter;

    private final List<RequestInterceptor> interceptors;

    private final Function<CacheDefinitions, CacheManager> cacheProvider;
    
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
        protected final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

        protected CookieJar cookieJar = null;
        protected RateLimiter rateLimiter = null;

        protected Function<CacheDefinitions, futurefit2.core.cache.CacheManager> cacheProvider;

        protected String baseUrl = null;
        protected String userAgent = null;

        protected List<RequestInterceptor> interceptors = new ArrayList<>();

        public Builder() {
            this.retrofitBuilder.addConverterFactory(ScalarsConverterFactory.create());
            this.retrofitBuilder.addConverterFactory(BuiltInConverterFactory.create());
            this.retrofitBuilder.addCallAdapterFactory(callAdapterFactory);
        }

        public Retrofit.Builder retrofit() {
            return retrofitBuilder;
        }

        public Builder client(@NonNull OkHttpClient client) {
            clientBuilder = client.newBuilder();
            return this;
        }

        public Builder baseUrl(@NonNull String baseUrl) {
            this.baseUrl = baseUrl;
            this.retrofitBuilder.baseUrl(baseUrl);
            return this;
        }

        public Builder baseUrl(@NonNull URL baseUrl) {
            this.baseUrl = baseUrl.toString();
            this.retrofitBuilder.baseUrl(baseUrl);
            return this;
        }

        public Builder baseUrl(@NonNull HttpUrl baseUrl) {
            this.baseUrl = baseUrl.toString();
            this.retrofitBuilder.baseUrl(baseUrl);
            return this;
        }

        public Builder log(@NonNull Level level) {
            loggingInterceptor.setLevel(level);
            return this;
        }

        public Builder userAgent(@NonNull String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder cookieJar(@NonNull CookieJar cookieJar) {
            this.cookieJar = cookieJar;
            return this;
        }

        /**
         * @deprecated use cacheManagerProviderEhCache()
         */
        @Deprecated
        public Builder cacheManager(@NonNull org.ehcache.CacheManager cacheManager) {
            this.cacheProvider = new EhCacheCacheManagerProvider(cacheManager);
            return this;
        }

        public Builder cacheManagerProviderEhCache(
                @NonNull Function<CacheDefinitions, org.ehcache.CacheManager> provider) {
            this.cacheProvider = new EhCacheCacheManagerProvider(provider);
            return this;
        }

        public Builder cacheManagerProviderSpring(
                @NonNull Function<CacheDefinitions, org.springframework.cache.CacheManager> provider) {
            this.cacheProvider = new SpringCacheManagerProvider(provider);
            return this;
        }

        public Builder addInterceptor(@NonNull RequestInterceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public Builder withRateLimiter(@NonNull RateLimiter rateLimiter) {
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
        public Builder withRateLimiter(double permitsPerSecond, @NonNull Duration warmupPeriod) {
            this.rateLimiter = RateLimiter.create(permitsPerSecond, warmupPeriod);
            return this;
        }

        /**
         * @see RateLimiter#create(double, long, TimeUnit)
         */
        public Builder withRateLimiter(double permitsPerSecond, long warmupPeriod, @NonNull TimeUnit unit) {
            this.rateLimiter = RateLimiter.create(permitsPerSecond, warmupPeriod, unit);
            return this;
        }

        public Futurefit build() {
            if (userAgent != null) {
                clientBuilder //
                        .addNetworkInterceptor(UserAgentInterceptor.build(userAgent));
            }
            clientBuilder //
                    .addNetworkInterceptor(requestUpdateInterceptor) //
                    .addNetworkInterceptor(loggingInterceptor);

            if (cacheProvider == null) {
                cacheProvider = new DefaultCacheManagerProvider();
            }
            if (rateLimiter == null) {
                rateLimiter = RateLimiter.create(10);
            }
            if (cookieJar == null) {
                // default cookieJar
                CookieManager cookieManager = new CookieManager();
                cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                cookieJar = new JavaNetCookieJar(cookieManager);
            }
            clientBuilder.cookieJar(cookieJar);

            this.retrofitBuilder.client(clientBuilder.build());
            return new Futurefit(this.retrofitBuilder, requestUpdateInterceptor, cacheProvider, rateLimiter, baseUrl,
                    interceptors);
        }

    }

    private Futurefit(retrofit2.Retrofit.Builder retrofitBuilder, RequestUpdateInterceptor requestUpdateInterceptor,
            Function<CacheDefinitions, CacheManager> cacheProvider, RateLimiter rateLimiter, String baseUrl,
            List<RequestInterceptor> interceptors) {
        this.baseUrl = baseUrl;
        this.retrofitBuilder = retrofitBuilder;
        this.requestUpdateInterceptor = requestUpdateInterceptor;
        this.rateLimiter = rateLimiter;
        this.interceptors = interceptors;
        this.cacheProvider = cacheProvider;
    }

    public <T> T create(Class<T> apiClass) {

        T retrofitAdapter = this.retrofitBuilder.build().create(apiClass);

        // init cache
        CacheManager cacheManager = CacheManagerInitializator.init(apiClass, this.cacheProvider);

        return createInterceptorProxy(apiClass, retrofitAdapter, cacheManager, new RequestFacadeCallback() {
            @Override
            public void apply(ProxyRequestFacade requestFacade) {
                requestUpdateInterceptor.setRequestUpdates(requestFacade);
            }
        });

    }

    @SuppressWarnings("unchecked")
    private <T> T createInterceptorProxy(Class<T> targetInterface, T delegate, CacheManager cacheManager,
            RequestFacadeCallback callback) {
        return (T) Proxy.newProxyInstance(targetInterface.getClassLoader(), new Class<?>[] { targetInterface },
                new InterceptorProxyInvocationHandler<T>(delegate, callback, cacheManager, this.rateLimiter,
                        this.baseUrl, this.interceptors));
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
