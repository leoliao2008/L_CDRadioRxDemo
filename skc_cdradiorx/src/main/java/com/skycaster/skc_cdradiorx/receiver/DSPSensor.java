package com.skycaster.skc_cdradiorx.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.skycaster.skc_cdradiorx.Constant;
import com.skycaster.skc_cdradiorx.bases.CDRadioApplication;
import com.skycaster.skc_cdradiorx.manager.DSPManager;
import com.skycaster.skc_cdradiorx.utils.LogUtils;
import com.skycaster.skc_cdradiorx.utils.ToastUtil;
import com.skycaster.skc_cdradiorx.utils.UsbUtil;


/**
 * 当DSP被安装或移除时，此广播接收器将执行相应逻辑
 * Created by Administrator on 2016/11/19.
 */

public class DSPSensor extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if(device!=null&&device.getVendorId()== Constant.VENDOR_ID&&device.getProductId()==Constant.PRODUCT_ID){
            switch (action){
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    if(CDRadioApplication.getDsp()==null){
                        LogUtils.showLog("device sensor", "检测设备");
                        ToastUtil.showToast("检测到设备");
                        if(UsbUtil.detectUSB()){
                            if(CDRadioApplication.isUpgradeMode()){
                                showLog("进入升级模式");
                                DSPManager.getDSPManager().sendUpgradePackage();
                            }
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    UsbUtil.closeConnection();
                    LogUtils.showLog("device sensor", "设备已被移除");
                    ToastUtil.showToast("设备已被移除");
                    break;
                default:
                    break;
            }
        }
    }

    private void showLog(String msg){
        LogUtils.showLog("device Sensor",msg);
    }
}
