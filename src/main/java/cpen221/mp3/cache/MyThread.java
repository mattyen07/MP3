package cpen221.mp3.cache;

import java.util.Map;
import java.util.Set;
import java.time.LocalDateTime;

public class MyThread<T extends Cacheable> implements Runnable {
    private final Map<T, TimePair> cacheMap;

    public MyThread(Map<T, TimePair> map) {
        this.cacheMap = map;

    }

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

