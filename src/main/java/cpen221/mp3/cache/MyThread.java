package cpen221.mp3.cache;


import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class MyThread<T extends Cacheable> extends Thread{
    private final int timeoutRun;
    private final Map<T, Long> cacheMap;

    public MyThread(int timeout, Map<T, Long> map) {
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
                    if (System.currentTimeMillis() - this.cacheMap.get(object) > (this.timeoutRun*1000)) {
                        this.cacheMap.remove(object);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

