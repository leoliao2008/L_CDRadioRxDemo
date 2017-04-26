package com.skycaster.skc_cdradiorx.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.os.Binder;
import android.os.IBinder;

import com.skycaster.jniutils.JniHelper;
import com.skycaster.skc_cdradiorx.Constant;
import com.skycaster.skc_cdradiorx.abstr.BusinessDataListener;
import com.skycaster.skc_cdradiorx.bases.CDRadioApplication;
import com.skycaster.skc_cdradiorx.beans.DSP;
import com.skycaster.skc_cdradiorx.manager.DSPManager;
import com.skycaster.skc_cdradiorx.utils.LogUtils;
import com.skycaster.skc_cdradiorx.utils.ToastUtil;

import java.util.Arrays;


/**
 * Created by 廖华凯 on 2016/11/29.
 */
public class BusinessService extends Service {

    private DSP dsp;
    private UsbDeviceConnection connection;
    private UsbEndpoint eptIn;
    private byte[] ackHolder;
    private byte[] coreData=new byte[Constant.DSP_CORE_LENGTH];
    private int len;
    private int currentFrameId;
    private int expectedFrameId;
    private int missCount;
    private byte[] bizData;
    private byte formCode;
    private JniHelper jniHelper= JniHelper.getJniHelper();
    private DSPManager dspManager=DSPManager.getDSPManager();
    private int[] offSets=new int[2];
    private int effectiveLen;
    private int maxLen;



    @Override
    public IBinder onBind(Intent intent) {
        showLog("on bind");
        maxLen=intent.getIntExtra("com.skycaster.skc_cdradiorx.MaxLen",81);
        bizData=new byte[maxLen];
        formCode=intent.getByteExtra("com.skycaster.skc_cdradiorx.FormCode", (byte) 33);
        return new ServiceBinder();
    }




    public class ServiceBinder extends Binder{
        public BusinessService getService(){
            return BusinessService.this;
        }
    }


//    private long[] times=new long[2];

    public void commenceReceiving(final BusinessDataListener listener) {
        dsp= CDRadioApplication.getDsp();
        if(dsp!=null){
            CDRadioApplication.setIsReceivingData(true);
            connection=dsp.getConnection();
            eptIn=dsp.getEptIn();
            ackHolder =new byte[Constant.DSP_DATA_LENGTH];
            expectedFrameId=1;
            stopEnsurer=null;
            listener.preTask();
            jniHelper.cdradio_rx_start();
//            times[0]=0;
            startForeground(123,new Notification());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (CDRadioApplication.isReceivingData()){
                        len =connection.bulkTransfer(eptIn, ackHolder,Constant.DSP_DATA_LENGTH,1000);
                        if(len <0){
                            break;
                        }
                        if((ackHolder[2] | ackHolder[3]<< 8)==0x000D&&(ackHolder[10]| ackHolder[11]<<8&0xff)==1){
                            //如果当前应答命令码为0x000D且状态码为1，说明DSP收到了停止业务数据的命令，且正常执行了该命令，该结束当前循环了。
                            break;
                        }
                        if(stopEnsurer!=null){
                            //双重保险，保证在dsp接收停止命令失败的情况下能重新接收停止命令
                            stopEnsurer.stopNow();
//                            showLog("业务数据接收停止命令失败，重新发送停止命令...");
                            continue;
                        }
                        currentFrameId =((ackHolder[7]<<8| ackHolder[6]&0xff))&0x0ffff;//解析数据，获取数据帧序号
                        currentFrameId =(currentFrameId ==0?65536: currentFrameId);//如果帧序号跳到0，则表示上一次帧序号已经达到了双字节byte数组达到的最大值65535，本次应为65536.
                        //判断是否跳帧，如果跳帧:
                        if (expectedFrameId != currentFrameId){
                            missCount+=currentFrameId-expectedFrameId;
                            listener.onPacketMiss(missCount);
                            expectedFrameId=currentFrameId;
                        }
                        updateFrameId();
//                        showLog("当前序号：" + (((ackHolder[7] << 8 | ackHolder[6] & 0xff)) & 0x0ffff));
                        listener.onGetBandData(ackHolder, currentFrameId);
                        System.arraycopy(ackHolder, 10, coreData, 0, 4096);
                        jniHelper.cdradio_rx_write_baseband(coreData, 1024);
                        if (jniHelper.cdradio_rx_get_offset(offSets)){
//                            showLog("cdradio_rx_get_offset--------success");
                            dspManager.setDSPParams(offSets[0], offSets[1]);
//                            showLog("------------------reset freqs, freqOffset: " + offSets[0] + ",timeOffset: " + offSets[1]);
                        }
                        effectiveLen = jniHelper.cdradio_rx_read_services_bytes(bizData, maxLen, formCode);
                        if(effectiveLen >0){
                            listener.onGetBizData(Arrays.copyOf(bizData, effectiveLen));
//                            showLog("cdradio_rx_read_service_frame--------success");
                        }
                    }
                    CDRadioApplication.setIsReceivingData(false);
                    stopForeground(true);
                    stopEnsurer=null;
                    post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onServiceStop();
                        }
                    });
                }
            }).start();
        }else {
            post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("DSP not connected");
                }
            });
            CDRadioApplication.setIsReceivingData(false);
            listener.onServiceStop();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(stopEnsurer==null){
            stopEnsurer=new BusinessDataStopEnsurer() {
                @Override
                public void stopNow() {
                    DSPManager.getDSPManager().apiStopService();
                }
            };
        }
        jniHelper.cdradio_rx_stop();
        stopSelf();
        return super.onUnbind(intent);
    }

    private interface BusinessDataStopEnsurer {
        void stopNow();
    }
    private BusinessDataStopEnsurer stopEnsurer;

    private void showLog(String msg){
        LogUtils.showLog("BusinessService", msg);
    }

    /**
     * 更新下一帧的正确帧序号
     */
    private synchronized void updateFrameId(){
        if(currentFrameId!=65536){
            expectedFrameId++;
        }else {
            //如果当前帧序号是65536，则下一帧从1重新开始
            expectedFrameId=1;
        }
    }

    private void post(Runnable runnable){
        CDRadioApplication.post(runnable);
    }
}
