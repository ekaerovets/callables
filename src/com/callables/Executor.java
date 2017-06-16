package com.callables;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author karyakin dmitry
 *         date 16.06.17.
 */
public class Executor<T> {

    public Executor() {
        new Thread(new WorkerThread(taskQueue, lock)).start();
    }

    private final Object lock = new Object();

    private Queue<Task> taskQueue = new PriorityBlockingQueue<>();

    // to make sure that two tasks with the same fireTime will be fired in the same order, in which they were enqueued
    private AtomicLong extraCounter = new AtomicLong(0);

    public void addTask(LocalDateTime fireTime, Callable<T> callable) {
        Task<T> task = new Task<>(fireTime, extraCounter.incrementAndGet(), callable);
        taskQueue.add(task);

        // in case the new task is the first in the queue, we should reset timeout of the worker thread
        if (taskQueue.peek() == task) {
            synchronized (lock) {
                lock.notify();
            }
        }
    }

}
