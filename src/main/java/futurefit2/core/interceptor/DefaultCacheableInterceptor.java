package futurefit2.core.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import futurefit2.Cacheable;
import futurefit2.core.cache.CacheManager;
import futurefit2.core.cache.CacheManager.Cache;
import futurefit2.utils.ReflectionUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
            String cacheName = findAnnotation.cache();
            Cache<Object, Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);

            Key key = new Key(invocation.baseUrl(), method, args);

            boolean hasKey;
            try {
                hasKey = cache.hasKey(key);
            } catch (Throwable e) {
                if (e.getClass().getCanonicalName().contains("SerializationException")) {
                    log.error("deserialization failed for {}, removing it", key);
                } else {
                    log.error("hasKey failed for {}, removing it", key);
                    log.error(e.getMessage(), e);
                }
                log.debug(e.getMessage(), e);
                cache.remove(key);
                hasKey = false;
            }

            Object result;
            if (hasKey) {
                result = cache.get(key);
            } else {
                result = invocation.invoke();

                if (result == null) {
                    log.error("Cache value is null for cache key: {}", key);
                } else {
                    cache.put(key, result);

                    if (cache.hasNoKey(key)) {
                        log.error("something wrong happen with cache '{}' on method '{}'", //
                                cacheName, method.toGenericString());
                    }
                }
            }
            return result;
        }

        return invocation.invoke();
    }

    @Data
    @SuppressWarnings("serial")
    public static class Key implements Serializable {
        private final List<Object> signature = new ArrayList<>();

        public Key(final String baseUrl, final Method method, final Object[] args) {
            final Object[] arguments = (args == null ? new Object[0] : args);
            signature.add(baseUrl);
            signature.add("%FUTUREFIT_SEP%");
            signature.add(method.getName());
            signature.add("%FUTUREFIT_SEP%");
            signature.add(method.toGenericString());
            signature.add("%FUTUREFIT_SEP%");
            signature.add(method.getReturnType().toGenericString());
            signature.add("%FUTUREFIT_SEP%");
            for (Class<?> paramType : method.getParameterTypes()) {
                signature.add(paramType.toGenericString());
                signature.add("%FUTUREFIT_PARAM_SEP%");
            }
            signature.add("%FUTUREFIT_SEP%");
            for (Object arg : arguments) {
                signature.add(arg);
                signature.add("%FUTUREFIT_ARG_SEP%");
            }
        }

        public Key(Object[] args) {
            for (Object arg : args) {
                signature.add(arg);
            }
        }

    }

}
