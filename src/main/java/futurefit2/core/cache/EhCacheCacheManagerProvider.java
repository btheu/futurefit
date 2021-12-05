package futurefit2.core.cache;

import java.util.function.Function;

import futurefit2.core.cache.CacheManagerInitializator.CacheDefinitions;
import lombok.NonNull;

public class EhCacheCacheManagerProvider implements Function<CacheDefinitions, CacheManager> {

    private org.ehcache.CacheManager cacheManager;
    private Function<CacheDefinitions, org.ehcache.CacheManager> provider;

    public EhCacheCacheManagerProvider(@NonNull org.ehcache.CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public EhCacheCacheManagerProvider(@NonNull Function<CacheDefinitions, org.ehcache.CacheManager> provider) {
        this.provider = provider;
    }

    @Override
    public CacheManager apply(@NonNull CacheDefinitions t) {
        if (cacheManager == null) {
            return new EhCacheCacheManagerAdapter(this.provider.apply(t));
        } else {
            cacheManager.init();
            return new EhCacheCacheManagerAdapter(cacheManager);
        }
    }

}
