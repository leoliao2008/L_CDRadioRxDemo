package com.skycaster.skc_cdradiorx.factory;


import com.skycaster.skc_cdradiorx.beans.DSPParam;
import com.skycaster.skc_cdradiorx.beans.LvParameter;
import com.skycaster.skc_cdradiorx.beans.RequestBean;
import com.skycaster.skc_cdradiorx.utils.LogUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 这个类生成手机端发送给DSP端的请求
 * @author 廖华凯
 *
 */
public class RequestFactory {



    private RequestFactory(){}
	
	/**
	 * 向DSP发送升级的请求，DSP将返回一个Ack给移动设备,如果Ack状态码为1，
	 * 则可以通过sendUpgradePackage方法从移动设备发送升级文件给DSP
	 * @param src 升级文件
	 * @return 返回相应RequestBean对象，可以通过RequestBean.toByte()方法把该请求
     * 转成数组发送给DSP
	 */
    public static synchronized RequestBean requestUpgrade(File src) {
		//读取升级文件，生成验证码
		byte[] temp=new byte[1024];
		int len;
        byte verifyCode=0;
		try {
            BufferedInputStream bis=new BufferedInputStream(new FileInputStream(src));
			while ((len=bis.read(temp))!=-1){
				for(int i=0;i<len;i++){
					verifyCode = (byte) ((verifyCode^temp[i]));
				}

            }
            bis.close();
		} catch (IOException e) {
            showLog("获取升级文件失败");
			e.printStackTrace();
		}
		RequestBean requestBean=new RequestBean(0x0001);//0x0001代表升级
		byte[] valid=new byte[]{verifyCode};
    	try {
			requestBean.setData(valid);
		} catch (RequestBean.DataOverSizeException e) {
			e.printStackTrace();
		}
		return requestBean;
    }

    
    /**
     * 启动设备
     * @return 返回相应RequestBean对象，可以通过RequestBean.toByte()方法把该请求
     * 转成数组发送给DSP
     */
    public static synchronized RequestBean requestStartDevice() {
		return new RequestBean(0x0003);//0x0003代表启动设备
    }
    
    
    /**
     * 停止设备
     * @return 返回相应RequestBean对象，可以通过RequestBean.toByte()方法把该请求
     * 转成数组发送给DSP
     */
    public static  synchronized RequestBean requestStopDevice() {
		return new RequestBean(0x0004);//0x0004代表停止设备
    }
    
    /**
     * 查询设备
     * @return 返回相应RequestBean对象，可以通过RequestBean.toByte()方法把该请求
     * 转成数组发送给DSP
     */
    public static synchronized RequestBean requestVersionInfo() {
		return new RequestBean(0x0005);//0x0005代表查询DSP版本号
    }
    
    
    /**
     * 读取设备工作状态
     * @return 返回相应RequestBean对象，可以通过RequestBean.toByte()方法把该请求
     * 转成数组发送给DSP
     */
    public static synchronized RequestBean requestDeviceStatus() {
		return new RequestBean(0x0006);//0x0006代表读取设备工作状态
    }
    
    
   /**
    * 设置DSP基础参数（时偏、频偏、频点等）,不含滤波参数
    * @param config 封装好参数对象
    * @return 返回相应RequestBean对象，可以通过RequestBean.toByte()方法把该请求
    * 转成数组发送给DSP
    */
    public static synchronized RequestBean requestSetParameter(DSPParam config){
    	RequestBean requestBean=new RequestBean(0x0007);//0x0007代表配置参数
    	requestBean.setDspConfig(config);
    	return requestBean;
    }
    
    
    /**
     * 设置DSP滤波参数（高通滤波(HPF)、选择性带通滤波(SBPF)...等）
     * @param lvParameter 封装好的滤波参数对象
     * @return 返回相应RequestBean对象，可以通过RequestBean.toByte()方法把该请求
     * 转成数组发送给DSP
     */
    public static synchronized RequestBean requestSetLvParameter(LvParameter lvParameter){
    	RequestBean requestBean=new RequestBean(0x0008);//0x0008代表滤波参数
    	requestBean.setLvParameter(lvParameter);
    	return requestBean;
    }
    
    /**
     * 启动原始数据传输
     * @return 返回相应RequestBean对象，可以通过RequestBean.toByte()方法把该请求
     * 转成数组发送给DSP
     */
    public static  synchronized RequestBean requestStartTunerTransfer() {
		return new RequestBean(0x0009);//0x0009代表从移动设备端启动原始数据传输到DSP上
    }
    
    /**
     * 停止原始数据传输
     * @return 返回相应RequestBean对象，可以通过RequestBean.toByte()方法把该请求
     * 转成数组发送给DSP
     */
    public static synchronized RequestBean requestStopTunerTransfer() {
		return new RequestBean(0x000A);//0x000A代表停止数据传输
    }
    
    
    /**
     * 启动基带数据传输
     * @return 返回相应RequestBean对象，可以通过RequestBean.toByte()方法把该请求
     * 转成数组发送给DSP
     */
    public static synchronized RequestBean requestStartBusinessTransfer() {
		return new RequestBean(0x000C);//0x000C代表启动基带数据传输
    }
    
    
    /**
     * 停止基带数据传输
     * @return 返回相应RequestBean对象，可以通过RequestBean.toByte()方法把该请求
     * 转成数组发送给DSP
     */
    public static synchronized RequestBean requestStopsBusinessTransfer() {
		return new RequestBean(0x000D);//0x000D代表停止基带数据传输
    }

    private static void showLog(String msg){
        LogUtils.showLog("RequestFactory", msg);
    }

}
