package com.skycaster.skc_cdradiorx.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.skycaster.skc_cdradiorx.Constant;
import com.skycaster.skc_cdradiorx.abstr.TunerDataListener;
import com.skycaster.skc_cdradiorx.bases.CDRadioApplication;
import com.skycaster.skc_cdradiorx.beans.DSP;
import com.skycaster.skc_cdradiorx.factory.RequestFactory;
import com.skycaster.skc_cdradiorx.manager.DSPManager;
import com.skycaster.skc_cdradiorx.utils.RequestUtils;
import com.skycaster.skc_cdradiorx.utils.ToastUtil;

/**
 * Created by 廖华凯 on 2017/2/7.
 */
public class TunerService extends Service {
    private ServiceStopHelper stopHelper;
    private DSP dsp;
    private byte[] data=new byte[Constant.DSP_DATA_LENGTH];
    private int frameId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TunerServiceBinder();
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        if(stopHelper==null){
            stopHelper=new ServiceStopHelper();
        }
        super.unbindService(conn);
    }

    public class TunerServiceBinder extends Binder {
        public TunerService getService(){
            return TunerService.this;
        }
    }

    public void commenceTunerTransfer(TunerDataListener listener){
        dsp=CDRadioApplication.getDsp();
        if(dsp!=null){
            if(!CDRadioApplication.isReceivingData()){
                boolean isSuccess = RequestUtils.sendRequest(RequestFactory.requestStartTunerTransfer());
                CDRadioApplication.setIsReceivingData(isSuccess);
                if(isSuccess){
                    preTask(listener);
                    onTask(listener);
                }else {
                    postTask(listener);
                }
            }else {
                showToast("业务数据正在运行，请先停止数据传输。");
                postTask(listener);
            }
        }else {
            showToast("DSP连接异常。");
            postTask(listener);
        }

    }

    private boolean preTask(TunerDataListener listener){
        listener.preTask();
        startForeground(456, new Notification());
        stopHelper=null;
        return false;
    }

    private boolean onTask(final TunerDataListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                UsbDeviceConnection connection=dsp.getConnection();
                UsbEndpoint eptIn=dsp.getEptIn();
                int result;
                while (CDRadioApplication.isReceivingData()){
                    result=connection.bulkTransfer(eptIn, data, Constant.DSP_DATA_LENGTH, 1000);
                    if(result<0){
                        //返回数据长度为-1，表示连接中断，此时退出
                        CDRadioApplication.setIsReceivingData(false);
                    }else if((data[2] | data[3] << 8)==0x000A&&(data[10]| data[11]<<8&0xff)==1){
                        //接收到正常退出基带数据的命令，此时退出
                        CDRadioApplication.setIsReceivingData(false);
                    }else if(stopHelper!=null){
                        //退出命令已经发出，但DSP由于各种原因未能回应，此时将重新发送退出命令
                        stopHelper.stopService();
                    }else {
                        frameId =((data[7]<<8| data[6]&0xff))&0x0ffff;//解析数据，获取数据帧序号
                        frameId =(frameId ==0?65536: frameId);//如果帧序号跳到0，则表示上一次帧序号已经达到了双字节byte数组达到的最大值65535，本次应为65536.
                        listener.onReceiveData(data.clone(),frameId);
                    }
                }
                CDRadioApplication.post(new Runnable() {
                    @Override
                    public void run() {
                        postTask(listener);
                    }
                });
            }
        }).start();
        return false;
    }

    private boolean postTask(TunerDataListener listener){
        stopHelper=null;
        listener.onTunerStop();
        stopForeground(true);
        stopSelf();
        return false;
    }

    private void showToast(final String msg){
        CDRadioApplication.post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showToast(msg);
            }
        });
    }

    private class ServiceStopHelper{
        private void stopService(){
            CDRadioApplication.setIsReceivingData(DSPManager.getDSPManager().apiStopDAQ());
        }
    }
}
