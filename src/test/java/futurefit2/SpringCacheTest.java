package futurefit2;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import com.github.benmanes.caffeine.cache.Caffeine;

import futurefit2.core.interceptor.HttpLoggingInterceptor.Level;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringCacheTest {

    @Test
    public void testSpringCaffeineCacheMemoryConfiguration() {

        Futurefit build = new Futurefit.Builder()
                .log(Level.BASIC)
                .baseUrl("https://www.google.fr")//
                .cacheManagerProvider(e -> {
                    Caffeine<Object, Object> caffeine = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS);

                    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
                    cacheManager.setCaffeine(caffeine);

                    return cacheManager;
                })
                .build();

    }

    @Test
    public void testSpringCaffeineCacheMemory() {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS);

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);

        Cache cache = cacheManager.getCache("sample");

        cache.put("key1", 42);

        assertNotNull(cache.get("key1", Integer.class));
    }

}
