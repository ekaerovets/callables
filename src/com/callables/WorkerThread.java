package com.callables;

import java.time.Clock;
import java.time.Instant;
import java.util.Queue;

/**
 * @author karyakin dmitry
 *         date 16.06.17.
 */
public class WorkerThread implements Runnable {

    private Queue<Task> queue;

    private final Object lock;

    public WorkerThread(Queue<Task> queue, Object lock) {
        this.queue = queue;
        this.lock = lock;
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {

            long now = getNow();
            long nextTaskIn = Long.MAX_VALUE;

            while (true) {
                Task t = queue.peek();
                if (t == null) {
                    break;
                }
                if (t.getTimeNanos() > now) {
                    nextTaskIn = t.getTimeNanos() - now;
                    break;
                }
                Task poll = queue.poll();
                try {
                    poll.fire();
                } catch (Exception ignore) {
                    // some logging
                }
                now = getNow();
            }

            long sleepMs = nextTaskIn / 1_000_000 + 1;

            try {
                synchronized (lock) {
                    lock.wait(sleepMs);
                }
            } catch (InterruptedException ignore) {

            }


        }
    }

    private long getNow() {
        Instant nowInstant = Clock.systemDefaultZone().instant();
        return nowInstant.getEpochSecond() * 1_000_000_000 + nowInstant.getNano();
    }
}
