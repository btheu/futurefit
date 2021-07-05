package futurefit2.core.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.ehcache.Cache;
import org.ehcache.CacheManager;

import futurefit2.Cacheable;
import futurefit2.utils.ReflectionUtil;
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

            Key key = new Key(method, args);

            Object result = cache.get(key);
            if (result == null) {
                result = invocation.invoke();

                if (result == null) {
                    throw new NullPointerException("Cache value is null for cache key: " + key);
                }

                cache.put(key, result);

            }
            return result;
        }

        return invocation.invoke();
    }

    @Data
    @SuppressWarnings("serial")
    public static class Key implements Serializable {
        private final List<Object> signature = new ArrayList<>();

        public Key(Method method, Object[] args) {
            signature.add(method.getName());
            signature.add("%FUTUREFIT_SEP%");
            signature.add(method.getReturnType().getName());
            signature.add("%FUTUREFIT_SEP%");
            for (Class<?> paramType : method.getParameterTypes()) {
                signature.add(paramType.getName());
            }
            signature.add("%FUTUREFIT_SEP%");
            for (Object arg : args) {
                signature.add(arg);
            }
        }

        public Key(Object[] args) {
            for (Object arg : args) {
                signature.add(arg);
            }
        }

    }

}
