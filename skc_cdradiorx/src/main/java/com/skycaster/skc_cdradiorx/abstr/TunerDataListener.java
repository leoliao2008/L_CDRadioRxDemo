package com.skycaster.skc_cdradiorx.abstr;

/**
 * Created by 廖华凯 on 2017/2/7.
 */
public abstract class TunerDataListener {
    /**
     * 开始接收数据前的一些操作，可以在这里修改UI
     */
    public abstract void preTask();
    /**
     * 当接收到原始数据时启动,该操作处于子线程中
     * @param data 原始数据
     * @param frameId 原始数据对应的帧序号
     */
    public abstract void onReceiveData(byte[] data, int frameId);

    /**
     * 监测跳帧数量，该操作处于子线程中
     * @param missCount 累计跳帧数
     */
    protected void onPacketMiss(int missCount){}

    /**
     * 当停止接收原始数据时启动，该操作处于主线程中
     */
    public abstract void onTunerStop();
}
