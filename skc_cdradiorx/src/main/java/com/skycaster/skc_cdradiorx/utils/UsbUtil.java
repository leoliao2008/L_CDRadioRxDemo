package com.skycaster.skc_cdradiorx.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.skycaster.skc_cdradiorx.Constant;
import com.skycaster.skc_cdradiorx.bases.CDRadioApplication;
import com.skycaster.skc_cdradiorx.beans.DSP;
import com.skycaster.skc_cdradiorx.factory.RequestFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.content.Context.USB_SERVICE;

/**
 * Created by Administrator on 2016/11/20.
 */

public class UsbUtil {

    private static final String DSP_CONNECTION_CHANGES ="com.skycaster.action.DSP_CONNECTION_CHANGES";
    private static final String IS_DSP_CONNECTED="IS_DSP_CONNECTED";


    /**
     * 链接dsp
     * @return
     */
    public synchronized static boolean detectUSB(){
        boolean isSuccess = false;
        if(CDRadioApplication.getDsp()==null||!CDRadioApplication.getDsp().isReadyToCommu()){
            closeConnection();
            UsbManager usbManager = (UsbManager) CDRadioApplication.getGlobalContext().getSystemService(USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            if(!deviceList.isEmpty()){
                Iterator<Map.Entry<String, UsbDevice>> iterator = deviceList.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String, UsbDevice> deviceEntry = iterator.next();
                    UsbDevice device = deviceEntry.getValue();
                    if(device.getProductId()== Constant.PRODUCT_ID&&device.getVendorId()==Constant.VENDOR_ID){
                        LogUtils.showLog(UsbUtil.class.getSimpleName(), "device be found:"+device.getProductId());
                        DSP dsp = new DSP();
                        dsp.setUsbManager(usbManager);
                        dsp.setUsbDevice(device);
                        isSuccess = initConnection(dsp, CDRadioApplication.getGlobalContext());
                        break;
                    }
                }
            }else {
                LogUtils.showLog(UsbUtil.class.getSimpleName(),"usb device list size is zero... ");
            }
        }else if (CDRadioApplication.getDsp()!=null&& CDRadioApplication.getDsp().isReadyToCommu()){
            isSuccess=true;
        }
        if(isSuccess){
            //如果DSP上一次关闭时没有正常关闭，如没有来得及申请停止数据传输就断掉了，在这里补充发送停止命令，这样dsp就能重新接收其他命令了。
            stopAllKindOfTransfer();
        }
        Intent intent=new Intent(DSP_CONNECTION_CHANGES);
        intent.putExtra(IS_DSP_CONNECTED,isSuccess);
        CDRadioApplication.getGlobalContext().sendBroadcast(intent);
        return isSuccess;
    }

    /**
     * 此方法只用于本SDK内部程序调用，不建议开发者使用。
     * @return
     */
    public static synchronized boolean stopAllKindOfTransfer(){
        boolean a = RequestUtils.sendRequest(RequestFactory.requestStopsBusinessTransfer());
        boolean b = RequestUtils.sendRequest(RequestFactory.requestStopTunerTransfer());
        return a && b;
    }

    /**
     * 关闭DSP各项连接
     */
    public synchronized static void closeConnection(){
        DSP dsp = CDRadioApplication.getDsp();
        if(dsp !=null){
            UsbDeviceConnection connection= dsp.getConnection();
            if(connection!=null){
                UsbInterface usbInterface= dsp.getUsbInterface();
                if(usbInterface!=null){
                    connection.releaseInterface(usbInterface);
                }
                connection.close();
                CDRadioApplication.setDsp(null);
            }
        }
    }

    /**
     * 重新链接DSP
     * @return
     */
    public synchronized static boolean resetConnection(){
        closeConnection();
        return detectUSB();
    }

    private synchronized static boolean initConnection(DSP dsp,Context context){
        boolean isSuccess=false;
        UsbManager usbManager = dsp.getUsbManager();
        if(usbManager.hasPermission(dsp.getUsbDevice())){
            findInterface(dsp);
            assignEndPoints(dsp);
            if(dsp.getEptIn()!=null&& dsp.getEptOut()!=null){
                UsbDeviceConnection usbConnection = usbManager.openDevice(dsp.getUsbDevice());
                if(usbConnection!=null){
                    dsp.setConnection(usbConnection);
                    isSuccess=usbConnection.claimInterface(dsp.getUsbInterface(),true);
                    dsp.setIsReadyToCommu(isSuccess);
                    if(isSuccess){
                        LogUtils.showLog("initConnection","DSP通信准备完毕");
                        CDRadioApplication.setDsp(dsp);
                    }else {
                        LogUtils.showLog("initConnection", "找到了通信端口，但链接权限受限");
                    }
                }else {
                    LogUtils.showLog("initConnection", "找到了通信端口，但无法打开设备");
                }
            }else {
                LogUtils.showLog("initConnection", "无法找到通讯端口，通信失败");
            }
        }else {
            requestPermission(dsp,context);
        }
        return isSuccess;
    }

    private synchronized static void requestPermission(final DSP dsp, final Context context){
        PendingIntent pi=PendingIntent.getBroadcast(context, 0, new Intent(Constant.ACTION_DSP_PERMISSION), 0);
        dsp.getUsbManager().requestPermission(dsp.getUsbDevice(), pi);
    }

    private synchronized static void findInterface(DSP dsp){
        if(dsp.getUsbDevice()!=null){
            int count = dsp.getUsbDevice().getInterfaceCount();
            for(int i=0;i<count;i++){
                UsbInterface itf = dsp.getUsbDevice().getInterface(i);
                if(UsbConstants.USB_CLASS_VENDOR_SPEC==itf.getInterfaceClass())
                {
                    dsp.setUsbInterface(itf);
                    LogUtils.showLog(UsbUtil.class.getSimpleName(), "找到了interface");
                    break;
                }
            }
        }
    }

    /**
     * 根据DSP的interface寻找发送端口及接受端口
     */
    private synchronized static void assignEndPoints(DSP dsp){
        UsbInterface usbInterface = dsp.getUsbInterface();
        if(usbInterface!=null){
            int endpointCount = usbInterface.getEndpointCount();
            for(int n=0;n<endpointCount;n++){
                UsbEndpoint endpoint = usbInterface.getEndpoint(n);
                if(endpoint.getType()==UsbConstants.USB_ENDPOINT_XFER_BULK){
                    if(endpoint.getDirection()==UsbConstants.USB_DIR_OUT){
                        dsp.setEptOut(endpoint);
                        LogUtils.showLog("initConnection", "找到了eptOut");
                    }else {
                        dsp.setEptIn(endpoint);
                        LogUtils.showLog("initConnection", "找到了eptIn");
                    }
                }
            }
        }else {
            LogUtils.showLog("initConnection", "找不到interface");
        }
    }

}
