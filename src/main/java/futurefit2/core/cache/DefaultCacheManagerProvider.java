package futurefit2.core.cache;

import java.util.function.Function;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;

import futurefit2.core.cache.CacheManagerInitializator.CacheDefinitions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultCacheManagerProvider implements Function<CacheDefinitions, CacheManager> {

    @Override
    public CacheManager apply(CacheDefinitions t) {
        CacheManagerBuilder<org.ehcache.CacheManager> builder = CacheManagerBuilder.newCacheManagerBuilder();

        t.getDefinitions().forEach(d -> {
            log.debug("creating cache name={}", d.getName());

            ResourcePoolsBuilder rpb = ResourcePoolsBuilder.newResourcePoolsBuilder();
            rpb = rpb.offheap(10, MemoryUnit.MB);
            if (d.getHeapSize() == null) {
                rpb = rpb.heap(10_000, EntryUnit.ENTRIES);
            } else {
                rpb = rpb.heap(d.getHeapSize(), EntryUnit.ENTRIES);
            }

            CacheConfigurationBuilder<Object, Object> cc = CacheConfigurationBuilder
                    .newCacheConfigurationBuilder(Object.class, Object.class, rpb)
                    .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(d.getTimeToLive()));

            builder.withCache(d.getName(), cc);
        });

        org.ehcache.CacheManager cacheManager = builder.build();

        cacheManager.init();

        return new EhCacheCacheManagerAdapter(cacheManager);
    }

}
