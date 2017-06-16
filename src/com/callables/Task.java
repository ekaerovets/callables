package com.callables;

import java.time.*;
import java.util.concurrent.Callable;

/**
 * @author karyakin dmitry
 *         date 16.06.17.
 */
public class Task<T> implements Comparable<Task> {

    private long timeNanos;
    private long extra;
    private Callable<T> callable;

    Task(LocalDateTime fireTime, long extra, Callable<T> callable) {

        Instant instant = fireTime.atZone(ZoneId.systemDefault()).toInstant();
        this.timeNanos = instant.getEpochSecond() * 1_000_000_000 + instant.getNano();

        this.extra = extra;
        this.callable = callable;
    }

    public long getTimeNanos() {
        return timeNanos;
    }

    @Override
    public int compareTo(Task o) {
        int res = Long.compare(this.timeNanos, o.timeNanos);
        return res != 0 ? res : Long.compare(this.extra, o.extra);
    }

    public T fire() throws Exception {
        return callable.call();
    }

}
