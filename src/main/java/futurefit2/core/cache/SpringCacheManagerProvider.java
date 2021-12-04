package futurefit2.core.cache;

import java.util.function.Function;

import futurefit2.core.cache.CacheManagerInitializator.CacheDefinitions;
import lombok.NonNull;

public class SpringCacheManagerProvider implements Function<CacheDefinitions, CacheManager> {

    private @NonNull Function<CacheDefinitions, org.springframework.cache.CacheManager> provider;

    public SpringCacheManagerProvider(
            @NonNull Function<CacheDefinitions, org.springframework.cache.CacheManager> provider) {
        this.provider = provider;
    }

    @Override
    public CacheManager apply(CacheDefinitions t) {
        return new SpringCacheManagerAdapter(provider.apply(t));
    }

}
