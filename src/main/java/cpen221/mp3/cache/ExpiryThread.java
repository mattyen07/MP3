package cpen221.mp3.cache;

import java.util.Map;
import java.util.Set;
import java.time.LocalDateTime;

public class ExpiryThread<T extends Cacheable> implements Runnable {
    private final Map<T, TimePair> cacheMap;

    /*
    RI: cacheMap objects with accurate TimePairs stored in cacheObjects.
        this.cacheMap is not null.
     */

    /*
    AF:
     */

    /**
     * Initializes new instance of ExpiryThread with a map of the objects in the cache.
     * @param cacheObjects is not null and its keys are the objects in the cache and the values are the TimePair's
     *            corresponding to the objects.
     */
    public ExpiryThread(Map<T, TimePair> cacheObjects) {
        this.cacheMap = cacheObjects;
    }

    /**
     * Runs ExpiryThread. This constantly updates the cache by removing stale (expired) objects.
     * @modifies cacheMap therefore modifying the cache by removing stale objects.
     */
    public void run() {
        while (true) {
            Set<T> copyMap = this.cacheMap.keySet();

            for (T object : copyMap) {
                if (LocalDateTime.now().isAfter(this.cacheMap.get(object).getExpiryTime())) {
                    this.cacheMap.remove(object);
                }
            }

        }
    }

}

