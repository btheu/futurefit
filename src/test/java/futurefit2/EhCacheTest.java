package futurefit2;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.ehcache.Cache;
import org.ehcache.Cache.Entry;
import org.ehcache.CacheManager;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.impl.serialization.CompactJavaSerializer;
import org.ehcache.impl.serialization.PlainJavaSerializer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import futurefit2.Futurefit2Test.GoogleApi;
import futurefit2.Futurefit2Test.Page;
import futurefit2.core.interceptor.DefaultCacheableInterceptor.Key;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class EhCacheTest {

    @Test
    @SneakyThrows
    public void testEhCacheDiskWithKeyValueObject() {
        ResourcePoolsBuilder disk = ResourcePoolsBuilder.newResourcePoolsBuilder()//
                .heap(1, EntryUnit.ENTRIES)//
                .offheap(1, MemoryUnit.MB) //
                .disk(2, MemoryUnit.MB, true);

        CacheConfigurationBuilder<Object, Object> newCacheConfigurationBuilder = //
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, disk) //
                        .withKeySerializer(new PlainJavaSerializer<Object>(Futurefit.class.getClassLoader())) //
                        .withValueSerializer(new PlainJavaSerializer<Object>(Futurefit.class.getClassLoader())); //

        CacheManagerBuilder<PersistentCacheManager> cacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder()//
                .with(CacheManagerBuilder.persistence("target/t1-2")) //
                .withCache("sample", newCacheConfigurationBuilder); //

        PersistentCacheManager cacheManager = cacheManagerBuilder.build(true);

        Cache<Object, Object> cache = cacheManager.getCache("sample", Object.class, Object.class);

        Key build = new Key(GoogleApi.class.getMethod("searchCached", String.class), new Object[] { "estivate" });

        Page value = new Page();
        value.setResultStatistics("test-stats-42");

        cache.forEach(new Consumer<Entry<?, ?>>() {
            @Override
            public void accept(Entry<?, ?> t) {
                log.info("hv {} => {}", t.getKey().toString(), t.getValue().toString());
            }
        });
        if (cache.containsKey(build)) {
            log.info("was already there ! {}", cache.get(build));
        }

        cache.put(build, value);

        assertTrue(cache.containsKey(build));

        cacheManager.close();

        cacheManager.init();

        cache = cacheManager.getCache("sample", Object.class, Object.class);

        assertTrue(cache.containsKey(build));
    }

    @Test
    @SneakyThrows
    public void testEhCacheDiskWithKeyObject() {
        ResourcePoolsBuilder disk = ResourcePoolsBuilder.newResourcePoolsBuilder()//
                .heap(1, EntryUnit.ENTRIES)//
                .offheap(1, MemoryUnit.MB) //
                .disk(2, MemoryUnit.MB, true);

        CacheConfigurationBuilder<Object, Object> newCacheConfigurationBuilder = //
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, disk) //
                        .withKeySerializer(new PlainJavaSerializer<Object>(Futurefit.class.getClassLoader())) //
                        .withValueSerializer(new PlainJavaSerializer<Object>(Futurefit.class.getClassLoader())); //

        CacheManagerBuilder<PersistentCacheManager> cacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder()//
                .with(CacheManagerBuilder.persistence("target/t1-1")) //
                .withCache("sample", newCacheConfigurationBuilder); //

        PersistentCacheManager cacheManager = cacheManagerBuilder.build(true);

        Cache<Object, Object> cache = cacheManager.getCache("sample", Object.class, Object.class);

        Key build = new Key(GoogleApi.class.getMethod("searchCached", String.class), new Object[] { "12", 12, "32" });

        if (cache.containsKey(build)) {
            log.info("was already there ! {}", cache.get(build));
        }

        cache.put(build, 42);

        assertTrue(cache.containsKey(build));

        cacheManager.close();

        cacheManager.init();

        cache = cacheManager.getCache("sample", Object.class, Object.class);

        assertTrue(cache.containsKey(build));
    }

    @Test
    public void testEhCacheDisk() {
        ResourcePoolsBuilder disk = ResourcePoolsBuilder.newResourcePoolsBuilder()//
                .heap(1, EntryUnit.ENTRIES)//
                .offheap(1, MemoryUnit.MB) //
                .disk(2, MemoryUnit.MB, true);

        CacheConfigurationBuilder<Object, Object> newCacheConfigurationBuilder = //
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, disk) //
                        .withKeySerializer(CompactJavaSerializer.asTypedSerializer()) //
                        .withValueSerializer(CompactJavaSerializer.asTypedSerializer()); //

        CacheManagerBuilder<PersistentCacheManager> cacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder()//
                .with(CacheManagerBuilder.persistence("target/t2")) //
                .withCache("sample", newCacheConfigurationBuilder); //

        PersistentCacheManager cacheManager = cacheManagerBuilder.build(true);

        Cache<Object, Object> cache = cacheManager.getCache("sample", Object.class, Object.class);

        if (cache.containsKey("key1")) {
            log.info("was already there ! {}", cache.get("key1"));
        }

        cache.put("key1", 42);

        assertTrue(cache.containsKey("key1"));

        cacheManager.close();

        cacheManager.init();

        cache = cacheManager.getCache("sample", Object.class, Object.class);

        assertTrue(cache.containsKey("key1"));
    }

    @Test
    public void testEhCacheMemory() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

        cacheManager.createCache("sample", CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(100)));

        Cache<Object, Object> cache = cacheManager.getCache("sample", Object.class, Object.class);

        cache.put("key1", 42);

        assertTrue(cache.containsKey("key1"));
    }

}
