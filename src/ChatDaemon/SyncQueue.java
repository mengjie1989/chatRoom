package ChatDaemon;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by refuhoo on 4/23/17.
 */
public class SyncQueue<E> {

    Queue<E> requestQueue = new LinkedList<E>();
    Object mLock = new Object();

    public int getSize() {
        synchronized (mLock) {
            return requestQueue.size();
        }
    }

    public void add(E e) {
        synchronized (mLock) {
            requestQueue.add(e);
        }
    }

    public E poll() {
        synchronized (mLock) {
            return requestQueue.poll();
        }
    }
}
