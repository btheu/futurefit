package futurefit2;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ehcache.CacheManager;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.impl.serialization.PlainJavaSerializer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import estivate.annotations.Select;
import estivate.annotations.Text;
import futurefit2.core.RequestFacade;
import futurefit2.core.interceptor.HttpLoggingInterceptor.Level;
import futurefit2.core.interceptor.MethodInterceptor;
import futurefit2.core.interceptor.RequestInterceptor;
import futurefit2.utils.FuturefitException;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 
 * @author Benoit Theunissen
 *
 */
@Slf4j
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class Futurefit2Test {

    @Test
    public void testInterceptor() {
        Futurefit build = new Futurefit.Builder()//
                .log(Level.BASIC)//
                .baseUrl("https://www.google.fr")//
                .addInterceptor(new TestInterceptor()) //
                .build();

        GoogleApi create = build.create(GoogleApi.class);

        assertThrows(FuturefitException.class, () -> {
            create.failingCall("search");
        });
    }

    public class TestInterceptor implements RequestInterceptor {

        @Override
        public Object intercept(RequestInvocation invocation) {

            Object invoke = invocation.invoke();

            String name = invocation.method().getName();

            String url = invocation.method().getAnnotation(GET.class).value();

            String value = ((Path) invocation.method().getParameterAnnotations()[0][0]).value();

            String value2 = invocation.arguments()[0].toString();

            throw new InterceptorException(name + " (" + url + ")@" + value + "=" + value2);
        }

    }

    @Test
    public void testRateLimiter() throws IOException, InterruptedException {

        Futurefit build = new Futurefit.Builder().log(Level.BASIC).baseUrl("https://www.google.fr")//
                .withRateLimiter(1, 3, TimeUnit.SECONDS).build();

        GoogleApi create = build.create(GoogleApi.class);

        for (int i = 0; i < 4; i++) {
            String stats = create.searchDirect("estivate").getResultStatistics();

            assertNotEmpty(stats);

            log.info("Statistics [{}]", stats);
        }

    }

    @Test
    @SneakyThrows
    public void testCacheOnDisk() {
        ResourcePoolsBuilder diskResourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()//
                .heap(1, EntryUnit.ENTRIES)//
                .offheap(1, MemoryUnit.MB) //
                .disk(2, MemoryUnit.MB, true);

        CacheConfigurationBuilder<Object, Object> newCacheConfigurationBuilder = //
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, diskResourcePools) //
                        .withKeySerializer(new PlainJavaSerializer<Object>(Futurefit.class.getClassLoader())) //
                        .withValueSerializer(new PlainJavaSerializer<Object>(Futurefit.class.getClassLoader())); //

        CacheManagerBuilder<PersistentCacheManager> cacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder()//
                .with(CacheManagerBuilder.persistence("target/t3")) //
                .withCache("google", newCacheConfigurationBuilder); //

        final PersistentCacheManager cacheManager = cacheManagerBuilder.build(true);

        Futurefit build = new Futurefit.Builder().log(Level.BASIC).baseUrl("https://www.google.fr")//
                .cacheManager(cacheManager).build();

        GoogleApi create = build.create(GoogleApi.class);

        for (int i = 0; i < 10; i++) {
            create.searchCached("estivate");
            Thread.sleep(1 * 1000);
        }

        String stats = create.searchCached("estivate").getResultStatistics();

        assertNotEmpty(stats);

        log.info("Statistics [{}]", stats);
    }

    @Test
    @SneakyThrows
    public void testCacheOnMemory() {

        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();

        Futurefit build = new Futurefit.Builder().log(Level.BASIC).baseUrl("https://www.google.fr")//
                .cacheManager(cacheManager).build();

        GoogleApi create = build.create(GoogleApi.class);

        for (int i = 0; i < 10; i++) {
            create.searchCached("estivate");
            Thread.sleep(1 * 1000);
        }

        String stats = create.searchCached("estivate").getResultStatistics();

        log.info("Statistics [{}]", stats);

        assertNotEmpty(stats);
    }

    @Test
    @SneakyThrows
    public void testCacheWithGenericsReturn() {

        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();

        Futurefit build = new Futurefit.Builder().log(Level.BASIC).baseUrl("https://www.google.fr")//
                .cacheManager(cacheManager).build();

        GoogleApi create = build.create(GoogleApi.class);

        create.searchTitleCached("estivate", "key2");

        List<Title> searchTitleCached = create.searchTitleCached("estivate", "key2");

        log.info("resultat [{}]", searchTitleCached);
    }

    @Test
    public void test() throws IOException {

        Futurefit build = new Futurefit.Builder().log(Level.BASIC).baseUrl("https://www.google.fr").build();

        GoogleApi create = build.create(GoogleApi.class);

        String stats = create.search("estivate").execute().body().getResultStatistics();

        assertNotEmpty(stats);

        log.info("Statistics [{}]", stats);
    }

    @Test
    public void testDirect() throws IOException {

        Futurefit.Builder builder = new Futurefit.Builder();

        Futurefit build = builder.baseUrl("https://www.google.fr").build();

        GoogleApi create = build.create(GoogleApi.class);

        String stats = create.searchDirect("estivate").getResultStatistics();

        assertNotEmpty(stats);

        log.info("Statistics [{}]", stats);
    }

    @Test
    public void testResponse() throws IOException {
        Futurefit.Builder builder = new Futurefit.Builder();

        Futurefit build = builder.baseUrl("https://www.google.fr").build();

        GoogleApi create = build.create(GoogleApi.class);

        create.search("word").execute().body().getResultStatistics();

        ResponseBody response = create.searchResponse("estivate");

        log.info("Statistics [{}]", response);
    }

    public static void assertNotEmpty(String sentence) {
        assertNotNull(sentence);
        assertNotEquals("", sentence.trim());
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

        @Estivate
        @Cacheable(cache = "google", duration = "PT5S")
        @GET("/search?hl=en&safe=off")
        @Headers({ "User-Agent:Mozilla/5.0 Firefox/68.0" })
        public List<Title> searchTitleCached(@Query("q") String query, @Query("q2") String query2);

        @GET("/search?hl=en&safe=off")
        @Headers({ "User-Agent:Mozilla/5.0 Firefox/68.0" })
        public ResponseBody searchResponse(@Query("q") String query);

        @Estivate
        @GET("/artp/{path}?hl=en&safe=off")
        @Headers({ "User-Agent:Mozilla/5.0 Firefox/68.0" })
        public Page failingCall(@Path("path") String path);
    }

    @Data
    @SuppressWarnings("serial")
    public static class Page implements Serializable {
        // get the div holding statistics
        @Select("#result-stats")
        @Text
        public String resultStatistics;
    }

    @Data
    @Select("h3")
    @SuppressWarnings("serial")
    public static class Title implements Serializable {

        @Text
        String title;

    }

    @SuppressWarnings("serial")
    public class InterceptorException extends RuntimeException {
        public InterceptorException(String message) {
            super(message);
        }
    }
}
