package com.skycaster.skc_cdradiorx.beans;

/**
 * 此对象封装了一系列代表dsp数据传输质量的参数
 * Created by 廖华凯 on 2016/12/14.
 */
public class DataQuality {
    /**
     * 当前so库的状态
     */
    private byte[] cppLibStatus=new byte[100];
    /**
     * 信噪比，比值越高，编译质量越好
     */
    private double[] snr=new double[1];
    /**
     * 数组，index0 表示某段时间成功编译的数量，index1 表示某段时间编译失败的数量
     */
    private int[] counts=new int[2];

    /**
     * 偏移量参数
     */
    private int[] offSets=new int[2];
  /**
     * 初始化此对象
     */
    public DataQuality() {
    }


    public int[] getOffSets() {
        return offSets;
    }

    public void setOffSets(int[] offSets) {
        System.arraycopy(offSets,0,this.offSets,0,2);
    }

    public void setCppLibStatus(byte[] cppLibStatus) {
        System.arraycopy(cppLibStatus,0,this.cppLibStatus,0,100);
    }

    public void setSnr(double[] snr) {
        System.arraycopy(snr,0,this.snr,0,1);
    }

    public void setCounts(int[] counts) {
       System.arraycopy(counts,0,this.counts,0,2);
    }

    /**
     * @return 返回so库状态码
     */
    public String getStatus() {
        return new String(cppLibStatus).trim();
    }


    /**
     *
     * @return 返回当前信噪率
     */
    public double getSnrRead() {
        return snr[0];
    }

    /**
     *
     * @return 返回成功编译数量
     */
    public int getSuccessCount(){
        return counts[0];
    }

    /**
     *
     * @return 返回失败编译数量
     */
    public int getFailCount(){
        return counts[1];
    }


}
