package futurefit2.core;

import java.util.function.Function;

import org.springframework.cache.CacheManager;

import lombok.Builder;

public class CacheInitializator2 {

    @Builder
    public static class CacheDefinition {

    }

    @Builder
    public static class CacheDefinitions {

    }

    public static void init(Class<?> apiClass, Function<CacheDefinitions, CacheManager> provider) {
        // TODO BTHEU call the provider with the filled CacheDefinitions and set the
        // CacheManager in return
    }

}
