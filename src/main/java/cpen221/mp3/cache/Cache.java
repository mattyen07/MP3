package cpen221.mp3.cache;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache<T extends Cacheable> {

    /*
    RI: capacity is not null and is the largest size of the CacheMap
        timeout is not null and is the largest time an item can exist in the cache.
        cacheMap contains all objects in the cache as keys and the TimePair is the value.
            for each TimePair value, .getExpiryTime() returns the time at which the cached item expires
            and .getLastAccess() returns the most recent time the item was accessed.
     */

    /*
    AF(cache) = items stored in cache such that
        cacheMap contains all items of the cache.
        expired items are removed from the cache.
     */

    /* the default cache size is 32 objects */
    public static final int DSIZE = 32;

    /* the default timeout value is 3600s */
    public static final int DTIMEOUT = 3600;

    private final int capacity;
    private Map<T, TimePair> cacheMap;
    private final int timeout;

    /**
     * Create a cache with a fixed capacity and a timeout value.
     * Objects in the cache that have not been refreshed within the timeout period
     * are removed from the cache.
     *
     * @param capacity >=1 the number of objects the cache can hold.
     * @param timeout >=0  the duration in seconds an object should be in the cache before it times out.
     */
    public Cache(int capacity, int timeout) {

        this.capacity = capacity;
        this.cacheMap = new ConcurrentHashMap<>();
        this.timeout = timeout;

        /* starts the auto clear expiry thread */
        Runnable expiry = new ExpiryThread<>(this.cacheMap);
        Thread expiryThread = new Thread(expiry);
        expiryThread.start();
    }



    /**
     * Create a cache with default capacity and timeout values.
     */
    public Cache() {
        this(DSIZE, DTIMEOUT);
    }

    /**
     * Add a value to the cache.
     * If the cache is full then remove the least recently accessed object to
     * make room for the new object.
     * @param t object to be placed in cache
     * @return true if t is successfully placed in cache
     */
    public boolean put(T t) {
        if (this.cacheMap.size() < this.capacity && !this.cacheMap.containsKey(t)) {
            TimePair add = new TimePair(LocalDateTime.now(), LocalDateTime.now().plusSeconds(this.timeout));
            this.cacheMap.put(t, add);
            return true;
        }
        if (this.cacheMap.size() == this.capacity && !this.cacheMap.containsKey(t)) {
            LocalDateTime time = LocalDateTime.now();
            LocalDateTime furthestAccessTime = LocalDateTime.now();
            T removeObject = t;

            for (T key: this.cacheMap.keySet()) {
                if (this.cacheMap.get(key).getLastAccess().isBefore(furthestAccessTime)) {
                    removeObject = key;
                    furthestAccessTime = this.cacheMap.get(key).getLastAccess();
                }
            }

            this.cacheMap.remove(removeObject);
            this.cacheMap.put(t, new TimePair(time, LocalDateTime.now().plusSeconds(this.timeout)));
            return true;
        }

        return false;
    }

    /**
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the cache
     * @throws NotFoundException if object id can't be found in the cache
     */
    public T get(String id) throws NotFoundException {
        for (T object : this.cacheMap.keySet()) {
            if (object.id().equals(id)) {
                LocalDateTime expiry = this.cacheMap.get(object).getExpiryTime();
                this.cacheMap.replace(object, new TimePair(LocalDateTime.now(), expiry));
                return object;
            }
        }

        throw new NotFoundException();
    }

    /**
     * Update the last refresh time for the object with the provided id.
     * This method is used to mark an object as "not stale" so that its timeout
     * is delayed.
     *
     * @param id the identifier of the object to "touch"
     * @return true if object is successfully touched and false otherwise.
     */
    public boolean touch(String id) {
        for (T object : this.cacheMap.keySet()) {
            if (object.id().equals(id)) {
                LocalDateTime update = this.cacheMap.get(object).getLastAccess();
                TimePair replacePair = new TimePair(update, LocalDateTime.now().plusSeconds(this.timeout));
                this.cacheMap.replace(object, replacePair);
                return true;
            }
        }

        return false;
    }

    /**
     * Update an object in the cache.
     * This method updates an object and acts like a "touch" to renew the
     * object in the cache.
     *
     * @param t the object to update
     * @return true if successful and false otherwise
     */
    public boolean update(T t) {
        if (this.cacheMap.containsKey(t)) {
            LocalDateTime update = this.cacheMap.get(t).getLastAccess();
            TimePair replacePair = new TimePair(update, LocalDateTime.now().plusSeconds(this.timeout));
            this.cacheMap.replace(t, replacePair);
            return true;
        }

        return false;
    }

}
