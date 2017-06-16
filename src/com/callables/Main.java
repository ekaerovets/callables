package com.callables;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * @author karyakin dmitry
 *         date 16.06.17.
 */
public class Main {

    public static void main(String[] args) {
        new Main().executeTasks();
    }

    private Executor<Void> executor = new Executor<>();
    private static LocalDateTime now = LocalDateTime.now();
    static long testStartTimeNanos = System.nanoTime();


    private void executeTasks() {

        // the simplest case - add a few tasks in the beginning
        addTask(800, "800 / 1");
        addTask(440, "440 / 1");
        addTask(312, "312 / 1");
        addTask(2000, "2000 / 1");
        addTask(1800, "1800 / 1");

        // add a few tasks while there are some tasks in the queue
        sleepUninterruptedly(1500);
        addTask(1550, "1550 / 2");
        addTask(1900, "1900 / 2");
        addTask(2100, "2100 / 2");

        sleepUninterruptedly(1000);

        // now add a few tasks with the same time in parallel
        // tasks must be called in the order they were added with some rare exceptions
        final CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    latch.await();
                } catch (InterruptedException ignore) {
                }
                for (int i1 = 0; i1 < 5; i1++) {
                    addTask(3000, "[added at " + System.nanoTime() + "] / 3000/ 3");
                }
            }).start();
        }
        latch.countDown();

        // run a few long-running tasks and a few normal tasks, scheduled right after
        // the latter should be fired only when all the long-running tasks are complete
        addTask(3510, "Short task 3510");
        addTask(3508, "Short task 3508 (1)");
        for (int i = 0; i < 10; i++) {
            addLongRunningTask(3500, "Long task 3500 / " + i);
        }

        addTask(3508, "Short task 3508 (2)");
        addTask(3503, "Short task 3503");
        addTask(3513, "Short task 3513");
        addTask(5000, "Short task 5000");

    }

    private void addTask(long offset, String message) {
        executor.addTask(fromOffset(offset), new SimpleCallable<Void>(message));
    }

    private void addLongRunningTask(long offset, String message) {
        executor.addTask(fromOffset(offset), new LongRunningCallable<>(message));
    }

    private LocalDateTime fromOffset(long offset) {
        return now.plus(offset, ChronoUnit.MILLIS);
    }

    private static void sleepUninterruptedly(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ignore) {

        }
    }

    private static final class SimpleCallable<T> implements Callable<Void> {

        private String message;

        SimpleCallable(String message) {
            this.message = message;
        }

        @Override
        public Void call() throws Exception {
            System.out.println("Task " + message + " called in " + ((System.nanoTime() - testStartTimeNanos) / 1000000.0) + "ms since test start");
            return null;
        }
    }

    private static final class LongRunningCallable<T> implements Callable<Void> {

        private String message;

        LongRunningCallable(String message) {
            this.message = message;
        }

        private long calcFibonacci(int index) {
            if (index < 3) {
                return 1;
            } else {
                return calcFibonacci(index - 2) + calcFibonacci(index - 1);
            }
        }

        @Override
        public Void call() throws Exception {
            System.out.println("Task " + message + " called in " + ((System.nanoTime() - testStartTimeNanos) / 1000000.0) + "ms since test start");
            // calculate Fibonacci number using the naive approach
            // takes about 25 ms on my machine
            calcFibonacci(35);
            return null;
        }
    }

}
