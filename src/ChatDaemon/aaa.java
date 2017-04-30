package ChatDaemon;

import java.util.concurrent.Semaphore;

/**
 * Created by refuhoo on 4/28/17.
 */
public class aaa {
    Semaphore sem = new Semaphore(0); // count -> 4 -> 3 -> 2 -> 1 -> 0


    class Thread1 extends Thread {
        public void run() {
            sleep(10);
            synchronized (lock) {
                putRequest(4);
            }
            sem.release(4);
        }
    }

    class Thread2 extends Thread {
        public void run() {
            sem.acquire(); // when count == 0, will block
            synchronized (lock) {
                getRequest();
            }
        }
    }

    class Thread3 extends Thread {
        public void run() {
            sem.acquire(); // when count == 0, will block
            synchronized (lock) {
                getRequest();
            }
        }
    }


    class BBB {
        int i;
        private Object lock;
        void increase() {
            synchronized (this) {
                i++;
            }
        }
        synchronized void decrease() {
            synchronized (this) {
                i--;
            }
        }

        void clear(int a) {
            i = 0;
        }
    }

    BBB b;
    class Thread4 extends Thread {
        public void run() {
            for(int i=0; i<100; i++) {
                b.increase();
            }
        }
    }

    class Thread5 extends Thread {
        public void run() {
            for(int i=0; i<100; i++) {
                b.increase();
            }
        }
    }


    public static void main() {
    }
}
