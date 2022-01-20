package futurefit2;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import com.github.benmanes.caffeine.cache.Caffeine;

import futurefit2.Futurefit2Test.GoogleApi;
import futurefit2.core.interceptor.HttpLoggingInterceptor.Level;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class SpringCacheTest {

    @Test
    public void testSpringCaffeineCacheMemoryConfiguration() {

        final Futurefit build = new Futurefit.Builder().log(Level.BASIC)
                .baseUrl("https://www.google.fr")//
                .cacheManagerProviderSpring(definitions -> {
                    log.info("> Building own caffeine cache manager");
                    CaffeineCacheManager cacheManager = new CaffeineCacheManager();

                    definitions.getDefinitions().forEach(d -> {

                        com.github.benmanes.caffeine.cache.Cache<Object, Object> cache = Caffeine.newBuilder()
                                .expireAfterWrite(d.getTimeToLive().getSeconds(), TimeUnit.SECONDS)
                                .maximumSize(d.getHeapSize())
                                .build();

                        cacheManager.registerCustomCache(d.getName(), cache);

                    });
                    log.info("< Building own caffeine cache manager");
                    return cacheManager;
                })
                .build();

        final GoogleApi create = build.create(GoogleApi.class);

        for (int i = 0; i < 4; i++) {
            final String stats = create.searchCached("estivate").getResultStatistics();

            Futurefit2Test.assertNotEmpty(stats);

            log.info("Statistics [{}]", stats);
        }

    }

    @Test
    public void testSpringCaffeineCacheMemory() {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS);

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);

        org.springframework.cache.Cache cache = cacheManager.getCache("sample");

        cache.put("key1", 42);

        assertNotNull(cache.get("key1", Integer.class));
    }

}
