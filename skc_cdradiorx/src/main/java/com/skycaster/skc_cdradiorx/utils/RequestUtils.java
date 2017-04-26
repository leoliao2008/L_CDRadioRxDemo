package com.skycaster.skc_cdradiorx.utils;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbRequest;

import com.skycaster.skc_cdradiorx.Constant;
import com.skycaster.skc_cdradiorx.bases.CDRadioApplication;
import com.skycaster.skc_cdradiorx.beans.AckBean;
import com.skycaster.skc_cdradiorx.beans.DSP;
import com.skycaster.skc_cdradiorx.beans.RequestBean;
import com.skycaster.skc_cdradiorx.intface.RequestCallBack;
import com.skycaster.skc_cdradiorx.manager.SingleThreadExecutor;

import java.nio.ByteBuffer;

/**
 * Created by 廖华凯 on 2016/11/26.
 */
public class RequestUtils {


    /**
     * 异步发送usb请求,主体部分在子线程运行，当前线程不会阻滞
     * @param requestBean 封装好在请求对象
     * @param callBack 回调，可以获得dsp返回的回覆对象
     */
    public static synchronized void sendRequest(final RequestBean requestBean, final RequestCallBack callBack){
        SingleThreadExecutor.enqueue(new Runnable() {
            @Override
            public void run() {
                AckBean ackBean = null;
                DSP dsp = CDRadioApplication.getDsp();
                if (dsp != null) {
                    UsbDeviceConnection connection = dsp.getConnection();
                    UsbRequest request = new UsbRequest();
                    request.initialize(connection, dsp.getEptIn());
                    ByteBuffer byteBuffer = ByteBuffer.allocate(Constant.DSP_DATA_LENGTH);
                    request.setClientData(requestBean.getCommandCode());
                    request.queue(byteBuffer, Constant.DSP_DATA_LENGTH);
                    int result = connection.bulkTransfer(dsp.getEptOut(), requestBean.toBytes(), Constant.DSP_DATA_LENGTH, 1000);
                    UsbRequest requestWait = connection.requestWait();
                    if (result > 0) {
                        if (requestWait.getClientData() != null && requestWait.getClientData() == request.getClientData()) {
                            ackBean = new AckBean(byteBuffer.array());
                        } else {
//                            showLog("请求对不上应答");
                        }
                    } else {
//                        showLog("请求发送失败，dsp接收不了请求");
                    }
                    request.close();
                } else {
//                    showLog("DSP为null");
                }

                if (ackBean == null) {
                    ackBean = new AckBean();
                }
                if (ackBean.getAckStatusCode() == 7) {
//                    showLog("设备认证错误,请支持正版！");
                    ackBean = new AckBean();
                    CDRadioApplication.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(Constant.WARNING);
                        }
                    });
                }
                final AckBean finalAckBean = ackBean;
                CDRadioApplication.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onGetAckBean(finalAckBean);
                    }
                });
            }
        });
    }


    private static byte[] temp=new byte[Constant.DSP_DATA_LENGTH];
    /**
     * 同步发送usb请求，当前线程阻滞
     * @return true表示成功、false表示失败
     */
    public synchronized static boolean sendRequest(RequestBean requestBean){
        boolean isSuccess=false;
        DSP dsp = CDRadioApplication.getDsp();
        if(dsp ==null){
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("DSP未连接");
                }
            });
            return false;
        }
        UsbDeviceConnection connection= dsp.getConnection();
        int result = connection.bulkTransfer(dsp.getEptOut(), requestBean.toBytes(), Constant.DSP_DATA_LENGTH, 1000);
//        showLog("请求被接受的长度："+result);
        if(result==Constant.DSP_DATA_LENGTH){
            result=connection.bulkTransfer(dsp.getEptIn(),temp,Constant.DSP_DATA_LENGTH,1000);
//            showLog("请求被接受后返回的应答的长度："+result);
            if(result==Constant.DSP_DATA_LENGTH){
//                showLog("该应答的状态码："+(temp[11]<<8|temp[10]&0xff));
                if((temp[11]<<8|temp[10]&0xff)==1){
                    isSuccess=true;
                }else if((temp[11]<<8|temp[10]&0xff)==7){
//                    showLog("设备认证错误,请支持正版！");
                    CDRadioApplication.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(Constant.WARNING);
                        }
                    });
                }
            }
        }
        return isSuccess;
    }


    public synchronized static boolean noAckRequest(RequestBean requestBean){
        boolean isSuccess=false;
        DSP dsp = CDRadioApplication.getDsp();
        if(dsp ==null){
            showLog("device = null");
            return false;
        }
        UsbDeviceConnection connection= dsp.getConnection();
        int result=connection.bulkTransfer(dsp.getEptOut(),requestBean.toBytes(),Constant.DSP_DATA_LENGTH,1000);
        if(result==Constant.DSP_DATA_LENGTH){
            isSuccess=true;
        }
        return isSuccess;
    }


    private static void showLog(String message){
        LogUtils.showLog("RequestUtils:",message);
    }
}
