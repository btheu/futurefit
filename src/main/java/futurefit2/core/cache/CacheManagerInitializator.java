package futurefit2.core.cache;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import futurefit2.Cacheable;
import futurefit2.core.cache.CacheManagerInitializator.CacheDefinition.CacheDefinitionBuilder;
import futurefit2.core.cache.CacheManagerInitializator.CacheDefinitions.CacheDefinitionsBuilder;
import futurefit2.utils.ReflectionUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

public class CacheManagerInitializator {

    @Getter
    @Builder
    public static class CacheDefinition {
        @NonNull
        private String name;
        @NonNull
        private Duration timeToLive;
        private Long heapSize;
    }

    @Getter
    @Builder
    public static class CacheDefinitions {
        @NonNull
        private Class<?> apiClass;
        @Singular
        private List<CacheDefinition> definitions;

    }

    public static CacheDefinitions definitions(Class<?> apiClass) {
        CacheDefinitionsBuilder builder = CacheDefinitions.builder();
        builder.apiClass(apiClass);

        for (Method method : apiClass.getDeclaredMethods()) {
            Cacheable findAnnotation = ReflectionUtil.findAnnotation(Cacheable.class, method.getAnnotations());
            if (findAnnotation != null) {
                CacheDefinitionBuilder cbuilder = CacheDefinition.builder();

                final Duration duration = Duration.parse(findAnnotation.duration());
                final String name = findAnnotation.cache();
                final long heap = findAnnotation.heap();

                cbuilder.name(name);
                cbuilder.timeToLive(duration);
                cbuilder.heapSize(heap);

                builder.definition(cbuilder.build());
            }
        }

        return builder.build();
    }

    public static CacheManager init(Class<?> apiClass, Function<CacheDefinitions, CacheManager> cacheProvider) {
        final CacheDefinitions definitions = definitions(apiClass);

        // call the provider with the filled CacheDefinitions
        final CacheManager cacheManager = cacheProvider.apply(definitions);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (cacheManager) {
                cacheManager.close();
            }
        }));

        return cacheManager;
    }

}
