package futurefit2.core.cache;

public interface CacheManager {

    public static interface Cache<K, V> {

        V get(K key);

        void put(K key, V value);

        void remove(K key);

        boolean hasKey(K key);

        default boolean hasNoKey(K key) {
            return !hasKey(key);
        }

    }

    <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType);

    void close();

}
