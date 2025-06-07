package com.wyt;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private final ExecutorService executorService;

    public ThreadPool(int maxPoolSize, int queueSize) {
        // corePoolSize表示线程池预设线程数；拒绝策略表示超过缓存队列时直接丢弃
        this.executorService = new ThreadPoolExecutor(Integer.min(10, maxPoolSize / 2), maxPoolSize, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueSize), new ThreadPoolExecutor.DiscardPolicy());
    }

    public void execute(Runnable task) {
        this.executorService.execute(task);
    }

    public void shutdown() {
        this.executorService.shutdown();
    }
}
