package com.skycaster.skc_cdradiorx.abstr;

/**
 * Created by 廖华凯 on 2017/1/4.
 */
/**
 * 接口回调，当接收到业务数据时启动，注意：部分方法在子线程中实现，不可直接操作UI。
 */
public abstract class BusinessDataListener {
    /**
     * 接收任务开始前的一些初始化工作将在这里执行，该操作发生在主线程，可以在直接更新UI。
     */
    public abstract void preTask();

    /**
     * 当接收到基带数据时启动,该操作发生在子线程；注意不要在方法中执行耗时的操作，如果比较耗时，建议在线程池中执行。
     * @param bandData 基带数据，大小4106bytes
     * @param frameId 当前基带数据对应的帧序号
     */
    public void onGetBandData(final byte[] bandData, final int frameId){}
    /**
     * 每次接收到业务数据时被调用，该方法在子线程中被执行；
     * 注意不要在这里执行太耗时的操作，如果比较耗时，建议在线程池中执行。
     * @param data 本次业务数据，以字节数组的形式返回
     */
    public abstract void onGetBizData(final byte[] data);

    /**
     * 当侦测到基带数据跳帧时被调用，该操作发生在子线程。
     * 注意不要在方法中执行耗时的操作，如果比较耗时，建议在线程池中执行。
     * @param missCount 表示累计跳了多少帧
     */
    public void onPacketMiss(final int missCount){}

    /**
     * 当业务数据停止发送时被调用，可以在这里执行业务数据停止后的逻辑。该操作发生在主线程，可以在直接更新UI。
     */
    public abstract void onServiceStop();
}
