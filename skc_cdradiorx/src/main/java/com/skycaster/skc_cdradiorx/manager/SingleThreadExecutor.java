package com.skycaster.skc_cdradiorx.manager;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * 封装好的线程管理者，保证每次最多只执行一个线程任务，后面如果还有其他任务，将会排队执行。
 * Created by 廖华凯 on 2016/12/7.
 */
public class SingleThreadExecutor {
    private SingleThreadExecutor() {
    }

    private static final java.util.concurrent.ThreadPoolExecutor threadPoolExecutor=new java.util.concurrent.ThreadPoolExecutor(1,1,0, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>());

    public static void enqueue(Runnable runnable){
        threadPoolExecutor.execute(runnable);
    }

    public static void cancelAll() {
        threadPoolExecutor.shutdownNow();
    }
}
