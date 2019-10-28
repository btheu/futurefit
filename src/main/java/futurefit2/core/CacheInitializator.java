package futurefit2.core;

import java.lang.reflect.Method;
import java.time.Duration;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import futurefit2.Cacheable;
import futurefit2.utils.ReflectionUtil;

public class CacheInitializator {

    public static void init(Class<?> apiClass, CacheManager cacheManager) {

        cacheManager.init();

        for (Method method : apiClass.getDeclaredMethods()) {
            Cacheable findAnnotation = ReflectionUtil.findAnnotation(Cacheable.class, method.getAnnotations());
            if (findAnnotation != null) {
                Cache<Object, Object> cache = cacheManager.getCache(findAnnotation.cache(), Object.class, Object.class);
                if (cache == null) {
                    cacheManager.createCache(findAnnotation.cache(), //
                            CacheConfigurationBuilder
                                    .newCacheConfigurationBuilder(Object.class, Object.class,
                                            ResourcePoolsBuilder.heap(findAnnotation.heap()))
                                    .withExpiry(ExpiryPolicyBuilder
                                            .timeToLiveExpiration(Duration.parse(findAnnotation.duration()))));
                }
            }
        }
    }

}
