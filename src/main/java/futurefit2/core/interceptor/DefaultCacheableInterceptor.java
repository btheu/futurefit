package futurefit2.core.interceptor;

import java.lang.reflect.Method;

import org.ehcache.Cache;
import org.ehcache.CacheManager;

import futurefit2.Cacheable;
import futurefit2.utils.ReflectionUtil;
import lombok.Builder;
import lombok.Data;

public class DefaultCacheableInterceptor implements RequestInterceptor {

    private CacheManager cacheManager;

    public DefaultCacheableInterceptor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Object intercept(RequestInvocation invocation) {

        Method method = invocation.method();

        Object target = invocation.target();

        Object[] args = invocation.arguments();

        Cacheable findAnnotation = ReflectionUtil.findAnnotation(Cacheable.class, method.getAnnotations());
        if (findAnnotation != null) {
            Cache<Object, Object> cache = cacheManager.getCache(findAnnotation.cache(), Object.class, Object.class);

            Key key = Key.builder().method(method).args(args).build();

            Object result = cache.get(key);
            if (result == null) {
                result = invocation.invoke();

                cache.put(key, result);

            }
            return result;
        }

        return invocation.invoke();

    }

    @Data
    @Builder
    public static class Key {
        Method   method;
        Object[] args;
    }

}
