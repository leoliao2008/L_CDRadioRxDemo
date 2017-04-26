package com.skycaster.skc_cdradiorx.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.skycaster.skc_cdradiorx.Constant;
import com.skycaster.skc_cdradiorx.utils.ToastUtil;
import com.skycaster.skc_cdradiorx.utils.UsbUtil;


/**
 * 监听用户DSP链接授权情况，执行相应逻辑
 * Created by Administrator on 2016/11/19.
 */

public class DSPPermissionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action!=null&& Constant.ACTION_DSP_PERMISSION.equals(action)){
            synchronized (this){
                if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)){
                    UsbDevice device=intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if(device!=null){
                        UsbUtil.detectUSB();
                    }
                }else {
                    ToastUtil.showToast("您拒绝了DSP与本机的链接权限");
                }
            }
        }

    }
}
