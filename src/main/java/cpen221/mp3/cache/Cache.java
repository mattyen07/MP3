package cpen221.mp3.cache;


import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class Cache<T extends Cacheable> {

    /* the default cache size is 32 objects */
    public static final int DSIZE = 32;

    /* the default timeout value is 3600s */
    public static final int DTIMEOUT = 3600;

    private final int capacity;
    private final int timeout;
    private Map<T, Long> cacheMap;

    /* TODO: Implement this datatype */

    /**
     * Create a cache with a fixed capacity and a timeout value.
     * Objects in the cache that have not been refreshed within the timeout period
     * are removed from the cache.
     *
     * @param capacity the number of objects the cache can hold
     * @param timeout  the duration an object should be in the cache before it times out
     */
    public Cache(int capacity, int timeout) {

        this.capacity = capacity;
        this.cacheMap = new HashMap<>();
        this.timeout = timeout;

        Thread expiryThread = new MyThread<>(timeout, this.cacheMap);
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
     */
    public boolean put(T t) {
        if (this.cacheMap.size() < this.capacity && !this.cacheMap.containsKey(t)) {
            this.cacheMap.put(t, System.currentTimeMillis());
            return true;
        }
        if (this.cacheMap.size() == this.capacity && !this.cacheMap.containsKey(t) ) {
            long time = System.currentTimeMillis();
            long maxTime = 0;
            T removeObject = t;

            for (T key: this.cacheMap.keySet()) {
                if (time - this.cacheMap.get(key) > maxTime) {
                    removeObject = key;
                    maxTime = time - this.cacheMap.get(key);
                }
            }

            this.cacheMap.remove(removeObject);
            this.cacheMap.put(t, time);
            return true;
        }

        return false;
    }

    /**
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the cache
     */
    public T get(String id) {

        /* Do not return null. Throw a suitable checked exception when an object
            is not in the cache. */
        try {
            for (T object : this.cacheMap.keySet()) {
                if (object.id().equals(id)) {
                    return object;
                }
            }
            throw new NotFoundException("ID not found");
        } catch(NotFoundException e){

            return null; //need to change this
        }
    }

    /**
     * Update the last refresh time for the object with the provided id.
     * This method is used to mark an object as "not stale" so that its timeout
     * is delayed.
     *
     * @param id the identifier of the object to "touch"
     * @return true if successful and false otherwise
     */
    public boolean touch(String id) {
        for (T object : this.cacheMap.keySet()) {
            if (object.id().equals(id)) {
                this.cacheMap.replace(object, System.currentTimeMillis());
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
            this.cacheMap.replace(t, System.currentTimeMillis());
            return true;
        }
        return false;
    }

}
