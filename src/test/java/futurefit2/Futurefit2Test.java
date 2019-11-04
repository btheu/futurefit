package futurefit2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.junit.Assert;
import org.junit.Test;

import estivate.annotations.Select;
import estivate.annotations.Text;
import futurefit2.core.RequestFacade;
import futurefit2.core.interceptor.MethodInterceptor;
import junit.framework.TestCase;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Call;
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

    @Test
    public void testRateLimiter() throws IOException, InterruptedException {

        Futurefit2 build = new Futurefit2.Builder().log(Level.BASIC).baseUrl("https://www.google.com/")//
                .withRateLimiter(2, 3, TimeUnit.SECONDS).build();

        GoogleApi create = build.create(GoogleApi.class);

        for (int i = 0; i < 10; i++) {
            String stats = create.searchCached("estivate").getResultStatistics();

            this.assertNotEmpty(stats);

            log.info("Statistics [{}]", stats);
        }

    }

    @Test
    public void testCache() throws IOException, InterruptedException {

        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();

        Futurefit2 build = new Futurefit2.Builder().log(Level.BASIC).baseUrl("https://www.google.com/")//
                .cacheManager(cacheManager).build();

        GoogleApi create = build.create(GoogleApi.class);

        for (int i = 0; i < 10; i++) {
            create.searchCached("estivate");
            Thread.sleep(1 * 1000);
        }

        String stats = create.searchCached("estivate").getResultStatistics();

        this.assertNotEmpty(stats);

        log.info("Statistics [{}]", stats);
    }

    @Test
    public void test() throws IOException {

        Futurefit2 build = new Futurefit2.Builder().log(Level.BASIC).baseUrl("https://www.google.com/").build();

        GoogleApi create = build.create(GoogleApi.class);

        String stats = create.search("estivate").execute().body().getResultStatistics();

        this.assertNotEmpty(stats);

        log.info("Statistics [{}]", stats);
    }

    @Test
    public void testDirect() throws IOException {

        Futurefit2.Builder builder = new Futurefit2.Builder();

        Futurefit2 build = builder.baseUrl("https://www.google.com/").build();

        GoogleApi create = build.create(GoogleApi.class);

        String stats = create.searchDirect("estivate").getResultStatistics();

        this.assertNotEmpty(stats);

        log.info("Statistics [{}]", stats);
    }

    @Test
    public void testResponse() throws IOException {
        Futurefit2.Builder builder = new Futurefit2.Builder();

        Futurefit2 build = builder.baseUrl("https://www.google.com/").build();

        GoogleApi create = build.create(GoogleApi.class);

        create.search("word").execute().body().getResultStatistics();

        ResponseBody response = create.searchResponse("estivate");

        log.info("Statistics [{}]", response);
    }

    private void assertNotEmpty(String sentence) {
        TestCase.assertNotNull(sentence);
        Assert.assertNotEquals("", sentence.trim());
    }

    public static class GoogleInterceptor implements MethodInterceptor {
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

        @Estivate
        @Cacheable(cache = "google", duration = "PT5S")
        @GET("/search?hl=en&safe=off")
        @Headers({ "User-Agent:Mozilla/5.0 Firefox/68.0" })
        public Page searchCached(@Query("q") String query);

        @Estivate
        @Cacheable(cache = "google", duration = "PT5S")
        @GET("/search?hl=en&safe=off")
        @Headers({ "User-Agent:Mozilla/5.0 Firefox/68.0" })
        public Page searchCached(@Query("q") String query, @Query("q2") String query2);

        @GET("/search?hl=en&safe=off")
        @Headers({ "User-Agent:Mozilla/5.0 Firefox/68.0" })
        public ResponseBody searchResponse(@Query("q") String query);

    }

    @Data
    public static class Page {
        // get the div holding statistics
        @Select("#resultStats")
        @Text
        public String resultStatistics;
    }

}
