package futurefit2.core.cache;

import java.util.function.Function;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;

import futurefit2.core.cache.CacheManagerInitializator.CacheDefinitions;

public class DefaultCacheManagerProvider implements Function<CacheDefinitions, CacheManager> {

    @Override
    public CacheManager apply(CacheDefinitions t) {
        CacheManagerBuilder<org.ehcache.CacheManager> builder = CacheManagerBuilder.newCacheManagerBuilder();

        t.getDefinitions().forEach(d -> {
            ResourcePoolsBuilder rpb = ResourcePoolsBuilder.newResourcePoolsBuilder();
            if (d.getHeapSize() != null) {
                rpb.heap(d.getHeapSize(), EntryUnit.ENTRIES);
            }

            CacheConfigurationBuilder<Object, Object> cc = CacheConfigurationBuilder
                    .newCacheConfigurationBuilder(Object.class, Object.class, rpb);

            cc.withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(d.getTimeToLive()));

            builder.withCache(d.getName(), cc);
        });

        return new EhCacheCacheManagerAdapter(builder.build());
    }

}
