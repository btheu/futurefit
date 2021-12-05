package futurefit2.core.cache;

import org.ehcache.Status;

import lombok.NonNull;

public class EhCacheCacheManagerAdapter implements CacheManager {

    private org.ehcache.CacheManager cacheManager;

    public EhCacheCacheManagerAdapter(@NonNull org.ehcache.CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public <K, V> Cache<K, V> getCache(@NonNull String cacheName, @NonNull Class<K> keyType,
            @NonNull Class<V> valueType) {
        return wrap(cacheManager.getCache(cacheName, keyType, valueType));
    }

    private <K, V> Cache<K, V> wrap(@NonNull org.ehcache.Cache<K, V> cache) {
        return new Cache<K, V>() {
            @Override
            public V get(K key) {
                return cache.get(key);
            }

            @Override
            public void put(K key, V value) {
                cache.put(key, value);
            }

            @Override
            public boolean hasKey(K key) {
                return cache.containsKey(key);
            }
        };
    }

    @Override
    public void close() {
        if (cacheManager.getStatus() == Status.AVAILABLE) {
            cacheManager.close();
        }
    }

}
