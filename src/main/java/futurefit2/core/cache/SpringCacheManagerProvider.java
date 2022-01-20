package futurefit2.core.cache;

import java.util.function.Function;

import futurefit2.core.cache.CacheManagerInitializator.CacheDefinitions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringCacheManagerProvider implements Function<CacheDefinitions, CacheManager> {

    private @NonNull Function<CacheDefinitions, org.springframework.cache.CacheManager> provider;

    public SpringCacheManagerProvider(
            @NonNull Function<CacheDefinitions, org.springframework.cache.CacheManager> provider) {
        this.provider = provider;
    }

    @Override
    public CacheManager apply(CacheDefinitions t) {
        org.springframework.cache.CacheManager apply = provider.apply(t);

        t.getDefinitions().forEach(def -> {
            if (!apply.getCacheNames().contains(def.getName())) {
                log.warn("missing cache definition '{}'", def.getName());
            }
        });

        return new SpringCacheManagerAdapter(apply);
    }

}
