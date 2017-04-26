package com.skycaster.skc_cdradiorx.beans;

/**
 * 代表滤波参数的类
 * @author 廖华凯
 *
 */
public class LvParameter {
	private short configCode;//参数代号
	private byte [] data;//参数内容

    /**
     *
     * @param type HPF高通参数、SBPF带通参数
     * @param data so返回的滤波参数字节组
     */
	public LvParameter(LvParamType type,byte[] data) {
		if(type== LvParamType.HPF){
			this.configCode=1;
		}else if(type== LvParamType.SBPF){
			this.configCode=2;
		}
		this.data=data;
	}


	/**
	 * 把对象转化成一个4096byte的数组
	 * @return 目标数组
	 */
	public byte[] toBytes(){
		byte[] bytes=new byte[4096];
        bytes[0]= (byte) configCode;//参数代码，2个字节
        bytes[1]= (byte) (configCode>>8);
		//把参数内容复制到数组后面
        System.arraycopy(data,0,bytes,2,data.length);
		return bytes;
	}

	public enum LvParamType{
		HPF,SBPF;//高通参数、带通参数
	}


}
