package futurefit2.core.cache;

public class SpringCacheManagerAdapter implements CacheManager {

    private org.springframework.cache.CacheManager cacheManager;

    public SpringCacheManagerAdapter(org.springframework.cache.CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        return adapt(cacheManager.getCache(cacheName));
    }

    private <K, V> Cache<K, V> adapt(org.springframework.cache.Cache cache) {
        return new Cache<K, V>() {
            @Override
            @SuppressWarnings("unchecked")
            public V get(K key) {
                return (V) cache.get(key).get();
            }

            @Override
            public void put(K key, V value) {
                cache.put(key, value);
            }

            @Override
            public boolean hasKey(K key) {
                return cache.get(key) != null;
            }

        };
    }

    @Override
    public void close() {
        // nothing
    }

}
