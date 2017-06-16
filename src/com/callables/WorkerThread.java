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

            while (true) {
                Task t = queue.peek();
                if (t == null) {
                    break;
                }
                if (t.getTimeNanos() > now) {
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

            try {
                synchronized (lock) {
                    // If worker is notified while not waiting, the notification will be lost.
                    // Another check within synchronized block fixes the problem.
                    Task nextTask = queue.peek();
                    long nextTaskIn = nextTask == null ? Long.MAX_VALUE : nextTask.getTimeNanos() - now;
                    long sleepMs = nextTaskIn / 1_000_000 + 1;
                    if (sleepMs > 0) {
                        lock.wait(sleepMs);
                    }
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
