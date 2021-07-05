package futurefit2.core;

import java.lang.reflect.Method;
import java.time.Duration;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.Status;
import org.ehcache.config.CacheRuntimeConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import futurefit2.Cacheable;
import futurefit2.utils.ReflectionUtil;

public class CacheInitializator {

    public static void init(Class<?> apiClass, CacheManager cacheManager) {

        if (cacheManager.getStatus() != Status.AVAILABLE) {
            cacheManager.init();
        }

        for (Method method : apiClass.getDeclaredMethods()) {
            Cacheable findAnnotation = ReflectionUtil.findAnnotation(Cacheable.class, method.getAnnotations());
            if (findAnnotation != null) {
                String cacheName = findAnnotation.cache();
                Cache<Object, Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);
                if (cache == null) {
                    cacheManager.createCache(cacheName, //
                            CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(findAnnotation.heap()))
                                    .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.parse(findAnnotation.duration()))));
                } else {
                    // override TTL
                    cacheManager.removeCache(cacheName);
                    cacheManager.createCache(cacheName, //
                            CacheConfigurationBuilder//
                                    .newCacheConfigurationBuilder(cache.getRuntimeConfiguration())
                                    .withExpiry(ExpiryPolicyBuilder
                                            .timeToLiveExpiration(Duration.parse(findAnnotation.duration()))));
                }
            }
        }
    }

}
