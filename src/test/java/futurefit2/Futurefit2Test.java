package futurefit2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;

import org.junit.Assert;
import org.junit.Test;

import estivate.annotations.Select;
import estivate.annotations.Text;
import futurefit2.convertor.EstivateConverterFactory;
import futurefit2.core.InterceptorProxyInvocationHandler;
import futurefit2.core.RequestFacade;
import futurefit2.core.RequestInterceptor;
import junit.framework.TestCase;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * 
 * @author Benoit Theunissen
 *
 */
@Slf4j
public class Futurefit2Test {

    public static class FutureCall extends OkHttpClient implements okhttp3.Call {

        private okhttp3.Call newCall;

        public FutureCall(OkHttpClient client, Request request) {
            newCall = client.newCall(request);
        }

        @Override
        public Request request() {
            return newCall.request();
        }

        @Override
        public Response execute() throws IOException {

            newCall.execute();

            return newCall.execute();
        }

        @Override
        public void enqueue(Callback responseCallback) {
            newCall.enqueue(responseCallback);
        }

        @Override
        public void cancel() {
            newCall.cancel();
        }

        @Override
        public boolean isExecuted() {
            return newCall.isExecuted();
        }

        @Override
        public boolean isCanceled() {
            return newCall.isCanceled();
        }

        @Override
        public Timeout timeout() {
            return newCall.timeout();
        }

        @Override
        public okhttp3.Call clone() {
            return newCall.clone();
        }

    }

    @Test
    public void test() throws IOException {

        final Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                okhttp3.Call call = chain.call();
                if (call instanceof FutureCall) {
                    FutureCall futureCall = (FutureCall) call;

                    Request build = call.request().newBuilder().build();
                    // here injection
                }

                return chain.proceed(chain.request());
            }
        };

        final OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor(interceptor).build();

        Retrofit adapter = new Retrofit.Builder().baseUrl("https://www.google.com/")
                // .addCallAdapterFactory(UnboxCallAdapterFactory.create())
                .addConverterFactory(EstivateConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()).client(client).build();

        GoogleApi create = adapter.create(GoogleApi.class);

        create = createInterceptorProxy(GoogleApi.class, create);

        // create.searchDirect("retrofit");

        String stats = create.search("retrofit").execute().body().getResultStatistics();

        this.assertNotEmpty(stats);

        log.info("Statistics [{}]", stats);
    }

    @SuppressWarnings("unchecked")
    private static <T> T createInterceptorProxy(Class<T> targetInterface, T delegate) {
        return (T) Proxy.newProxyInstance(targetInterface.getClassLoader(), new Class<?>[] { targetInterface },
                new InterceptorProxyInvocationHandler<T>(delegate));
    }

    private void assertNotEmpty(String sentence) {
        TestCase.assertNotNull(sentence);
        Assert.assertNotEquals("", sentence.trim());
    }

    public static class GoogleInterceptor implements RequestInterceptor {
        @Override
        public void intercept(RequestFacade requestFacade, Annotation[] annotations, Object response) {

            log.info("intercepted");
        }
    }

    public static interface GoogleApi {

        @Estivate
        @Intercept(handler = GoogleInterceptor.class)
        @GET("/search?hl=en&safe=off")
        @Headers({ "User-Agent:Mozilla/5.0 Firefox/68.0" })
        public Call<Page> search(@Query("q") String query);

        @Estivate
        @GET("/search?hl=en&safe=off")
        @Headers({ "User-Agent:Mozilla/5.0 Firefox/68.0" })
        public Page searchDirect(@Query("q") String query);

    }

    @Data
    public static class Page {

        // get the div holding statistics
        @Select("#resultStats")
        @Text
        public String resultStatistics;

    }

}
