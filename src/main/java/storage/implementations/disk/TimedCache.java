package storage.implementations.disk;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class TimedCache<T extends AutoCloseable> {

    Map<String, CacheRecord<T>> cache;

    public TimedCache() {
        this.cache = new ConcurrentHashMap<>();
        //Timer timer = new Timer();
        //timer.scheduleAtFixedRate(new Eviction<>(cache), 0L, 30000L);
    }

    public void prolongate(String key, long delta) {
        CacheRecord<T> record = cache.get(key);

        if (record == null) {
            return;
        }

        record.expTime += delta;
    }

    public T get(String key) {
        CacheRecord<T> record = cache.get(key);

        if (record == null) {
            return null;
        }

        return record.object;
    }

    public void delete(String key) {
        CacheRecord<T> record = cache.get(key);

        if (record != null) { //todo кто-то может уже получил его из кеша и  пользуется, надо учесть
            cache.remove(key);
            try {
                record.object.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void add(String key, T value, long expTime) {
        cache.put(key, new CacheRecord<>(value, expTime));
    }

    private static class CacheRecord<R extends AutoCloseable> {
        final R object;
        volatile long expTime;

        public CacheRecord(R object, long expTime) {
            this.object = object;
            this.expTime = expTime;
        }
    }

    public static class Eviction<J extends AutoCloseable> extends TimerTask {
        Map<String, CacheRecord<J>> cache;

        public Eviction(Map<String, CacheRecord<J>> cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            for (Map.Entry<String, CacheRecord<J>> entry : cache.entrySet()) {
                if (entry.getValue().expTime <= time) {
                    System.out.println("Remove " + entry.getValue());
                    cache.remove(entry.getKey());
                }
            }
        }
    }


}
