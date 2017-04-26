package com.skycaster.skc_cdradiorx.beans;


/**
 * DSP基本参数类，同时包含时偏、频偏、频点三个参数。根据协议，设置参数时，不可分开设置此三个参数，
 * 要同时设置此三个参数。
 * @author 廖华凯
 */
public class DSPParam {
	private int freqOffset;//频偏
	private int timeOffset;//时偏
	private int freq;//频点

	/**
	 * 生成一个包含时偏、频偏、频点的参数类,频点为系统默认值
	 * @param freqOffset 频偏
	 * @param timeOffset 时偏
	 */
	public DSPParam(int freqOffset, int timeOffset) {
		super();
		this.freqOffset=freqOffset;
		this.timeOffset=timeOffset;
		this.freq= Integer.MAX_VALUE;
	}

    /**
     * 生成一个包含时偏、频偏、频点的参数类，其中时偏、频偏为系统默认值。
     * @param freq 频点
     */
	public DSPParam(double freq) {
		super();
		this.freqOffset=Integer.MAX_VALUE;
		this.timeOffset=Integer.MAX_VALUE;
		this.freq= (int) (freq*100);
	}

	/**
	 * 把该对象转成一个12 bytes大小的数组
	 * @return 该对象转成的数组
	 */
	public byte[] toBytes() {
		byte[] bytes=new byte[12];
		//设置频偏 设置时偏 设置频点
        bytes[0]= (byte) freqOffset;
        bytes[1]= (byte) (freqOffset>>8);
        bytes[2]= (byte) (freqOffset>>16);
        bytes[3]= (byte) (freqOffset>>24);
        bytes[4]= (byte) timeOffset;
        bytes[5]= (byte) (timeOffset>>8);
        bytes[6]= (byte) (timeOffset>>16);
        bytes[7]= (byte) (timeOffset>>24);
        bytes[8]= (byte) freq;
        bytes[9]= (byte) (freq>>8);
        bytes[10]= (byte) (freq>>16);
        bytes[11]= (byte) (freq>>24);
		return bytes;
	}


	

}
