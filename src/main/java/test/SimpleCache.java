package test;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by sunny on 2017/1/7.
 */
public final class SimpleCache<K, V> {

    private Lock lock = new ReentrantLock();

    private int maxCapacity = 0;

    private Map<K, V> eden;

    private Map<K, V> perm;

    public SimpleCache(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.eden = new ConcurrentHashMap<K, V>(maxCapacity);
        this.perm = new WeakHashMap<K, V>(maxCapacity);
    }

    public V get(K k) {
        V v = this.eden.get(k);
        if (v == null) {
            lock.lock();
            try {
                v = this.perm.get(k);
            } finally {
                lock.unlock();
            }
            if (v != null) {
                this.eden.put(k, v);
            }
        }
        return v;
    }

    public void put(K k, V v) {
        if (this.eden.size() >= maxCapacity) {
            lock.lock();
            try {
                this.perm.putAll(this.eden);
            } finally {
                lock.unlock();
            }
            this.eden.clear();
        }
        this.eden.put(k, v);
    }
}
