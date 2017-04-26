package com.skycaster.skc_cdradiorx.bases;

import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.skycaster.skc_cdradiorx.Constant;
import com.skycaster.skc_cdradiorx.receiver.DSPPermissionReceiver;
import com.skycaster.skc_cdradiorx.receiver.DSPSensor;
import com.skycaster.skc_cdradiorx.utils.LogUtils;
import com.skycaster.skc_cdradiorx.utils.ToastUtil;
import com.skycaster.skc_cdradiorx.utils.UsbUtil;


/**
 * 如果要使用DSP功能，又不是很熟悉本sdk的开发者，建议继承此activity。
 *
 * Created by 廖华凯 on 2016/11/17.
 *
*/
public abstract class CDRadioActivity extends FragmentActivity {
    private DSPSensor mDSPSensor;//监听dsp插入、移除
    private DSPPermissionReceiver mPermissionReceiver;//监听dsp连接权限授予结果
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        if(intent!=null){
            UsbDevice usbDevice=intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if(usbDevice!=null){
                if(UsbUtil.detectUSB()){
                    ToastUtil.showToast("设备连接成功");
                }else {
                    ToastUtil.showToast("设备连接失败");
                }
            }
        }
        registerReceiver();//注册广播接收者
    }


    /**
     * 注册广播接收者，监听DSP插入、移除、链接权限等动作
     */
    private void registerReceiver(){
        //监听dsp插入、移除
        mDSPSensor =new DSPSensor();
        IntentFilter filter=new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mDSPSensor, filter);

        //监听dsp连接权限授予结果
        mPermissionReceiver=new DSPPermissionReceiver();
        IntentFilter filter1=new IntentFilter(Constant.ACTION_DSP_PERMISSION);
        registerReceiver(mPermissionReceiver, filter1);

    }

    /**
     * 取消注册广播接收者
     */
    private void unregisterReceiver(){
        unregisterReceiver(mDSPSensor);
        unregisterReceiver(mPermissionReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    protected void showLog(String s){
        LogUtils.showLog(this, s);
    }




}
