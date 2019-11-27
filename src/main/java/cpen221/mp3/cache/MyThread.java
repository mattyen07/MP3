package cpen221.mp3.cache;


import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

public class MyThread<T extends Cacheable> implements Runnable{
    private final int timeoutRun;
    private final Map<T, LocalDateTime> cacheMap;

    public MyThread(int timeout, Map<T, LocalDateTime> map) {
        this.timeoutRun = timeout;
        this.cacheMap = map;

    }

    public void run() {
        try {
            while (true) {
                List<T> copyMap = new ArrayList<>();
                for (T key : this.cacheMap.keySet()) {
                    if (!copyMap.contains(key)) {
                        copyMap.add(key);
                    }
                }

                for (T object : copyMap) {
                    if (LocalDateTime.now().minusSeconds(timeoutRun).isAfter(this.cacheMap.get(object))) {
                        this.cacheMap.remove(object);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

