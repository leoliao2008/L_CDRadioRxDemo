package com.skycaster.l_cdradiorxdemo.activities;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skycaster.l_cdradiorxdemo.R;
import com.skycaster.l_cdradiorxdemo.widgets.DspInterface;
import com.skycaster.skc_cdradiorx.bases.CDRadioApplication;
import com.skycaster.skc_cdradiorx.beans.DSP;

/**
 * Created by 廖华凯 on 2016/12/13.
 */
public class DSPInfo extends FragmentActivity {
    private Button btn_back;
    private LinearLayout ll_rootView;
    private DSP dsp;
    private UsbDevice usbDevice;
    private TextView tv_productId;
    private TextView tv_productName;
    private TextView tv_vendorId;
    private TextView tv_vendorName;
    private TextView tv_deviceId;
    private TextView tv_deviceName;
    private TextView tv_serialNum;
    private TextView tv_versionInfo;

    public static void startActivity(Activity context){
        context.startActivity(new Intent(context, DSPInfo.class));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_usb_info);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        btn_back= (Button) findViewById(R.id.widget_action_bar_btn_back);
        ll_rootView= (LinearLayout) findViewById(R.id.widget_ll_usb_info_root_view);
        tv_productId= (TextView) findViewById(R.id.widget_usb_info_tv_product_id);
        tv_productName= (TextView) findViewById(R.id.widget_usb_info_tv_product_name);
        tv_vendorId= (TextView) findViewById(R.id.widget_usb_info_tv_vendor_id);
        tv_vendorName= (TextView) findViewById(R.id.widget_usb_info_tv_vendor_name);
        tv_deviceId= (TextView) findViewById(R.id.widget_usb_info_tv_device_id);
        tv_deviceName= (TextView) findViewById(R.id.widget_usb_info_tv_device_name);
        tv_serialNum= (TextView) findViewById(R.id.widget_usb_info_tv_serial_num);
        tv_versionInfo= (TextView) findViewById(R.id.widget_usb_info_tv_version_info);
    }

    private void initData() {
        dsp= CDRadioApplication.getDsp();
        if(dsp!=null&&dsp.isReadyToCommu()){
            usbDevice = dsp.getUsbDevice();
            tv_productId.setText(usbDevice.getProductId()+"");
            tv_vendorId.setText(usbDevice.getVendorId()+"");
            tv_deviceId.setText(usbDevice.getDeviceId()+"");
            tv_deviceName.setText(usbDevice.getDeviceName());
            if(Build.VERSION.SDK_INT>=21){
                tv_productName.setText(usbDevice.getProductName());
                tv_vendorName.setText(usbDevice.getManufacturerName());
                tv_serialNum.setText(usbDevice.getSerialNumber());
            }else {
                tv_productName.setText("不支持低级安卓系统");
                tv_vendorName.setText("不支持低级安卓系统");
                tv_serialNum.setText("不支持低级安卓系统");
            }
            if(Build.VERSION.SDK_INT>=23){
                tv_versionInfo.setText(usbDevice.getVersion());
            }else {
                tv_versionInfo.setText("不支持低级安卓系统");
            }

            int count=usbDevice.getInterfaceCount();
            for(int i=0;i<count;i++){
                UsbInterface usbInterface = usbDevice.getInterface(i);
                ll_rootView.addView(new DspInterface(DSPInfo.this,usbInterface));
            }

        }
    }

    private void initListener() {
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
}
