package com.clevertap.android.sdk.extras;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CleverTapExecutors {
    private static final int CPU_COUNT = Math.max(Runtime.getRuntime().availableProcessors(), 1);

    private static ExecutorService io;

    private static AtomicInteger threadCount = new AtomicInteger(1);

    /**
     * used for local io, database, and other works without too much time.
     */
    public static ExecutorService io() {
        if (io == null) {
            int max = Math.min(4, CPU_COUNT * 2 + 1);
            io = new ThreadPoolExecutor(max, max,
                    10L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("CleverTap-Executors-Thread-" + threadCount.getAndIncrement());
                    return thread;
                }
            });
            ((ThreadPoolExecutor) io).allowCoreThreadTimeOut(true);
        }

        return io;
    }
}
