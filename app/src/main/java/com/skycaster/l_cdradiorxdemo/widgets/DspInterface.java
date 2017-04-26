package com.skycaster.l_cdradiorxdemo.widgets;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skycaster.l_cdradiorxdemo.R;

/**
 * Created by 廖华凯 on 2016/12/13.
 */
public class DspInterface extends LinearLayout {
    private UsbInterface usbInterface;
    private LinearLayout ll_title;
    private TextView tv_interface_name;

    public DspInterface(Context context, UsbInterface usbInterface) {
        this(context, null,usbInterface);

    }

    public DspInterface(Context context, AttributeSet attrs, UsbInterface usbInterface) {
        this(context, attrs, 0, usbInterface);
    }

    public DspInterface(Context context, AttributeSet attrs, int defStyleAttr, UsbInterface usbInterface) {
        super(context, attrs, defStyleAttr);
        this.usbInterface=usbInterface;
        setOrientation(VERTICAL);
        ll_title= (LinearLayout) View.inflate(context, R.layout.widget_dsp_interface,null);
        addView(ll_title);
        tv_interface_name= (TextView) ll_title.findViewById(R.id.widget_interface_name);
        if(Build.VERSION.SDK_INT>=21){
            tv_interface_name.setText(usbInterface.getName());
        }else {
            tv_interface_name.setText(usbInterface.getId()+"");
        }
        int count=usbInterface.getEndpointCount();
        for(int i=0;i<count;i++){
            UsbEndpoint endpoint = usbInterface.getEndpoint(i);
            View endPointView=View.inflate(context, R.layout.widget_dsp_end_point, null);
            TextView name= (TextView) endPointView.findViewById(R.id.widget_endpoint_name);
            TextView direction= (TextView) endPointView.findViewById(R.id.widget_endpoint_direction);
            name.setText(getEndPointType(endpoint.getType()));
            direction.setText(getDirection(endpoint.getDirection()));
            addView(endPointView);
        }
    }

    private String getEndPointType(int n){
        String type;
        switch (n){
            case UsbConstants.USB_ENDPOINT_XFER_BULK:
                type="USB_ENDPOINT_XFER_BULK";
                break;
            case UsbConstants.USB_ENDPOINT_XFER_CONTROL:
                type="USB_ENDPOINT_XFER_CONTROL";
                break;
            case UsbConstants.USB_ENDPOINT_XFER_INT:
                type="USB_ENDPOINT_XFER_INT";
                break;
            case UsbConstants.USB_ENDPOINT_XFER_ISOC:
                type="USB_ENDPOINT_XFER_ISOC";
                break;
            default:
                type="null";
                break;
        }
        return type;
    }

    private String getDirection(int n){
        String direction;
        switch (n){
            case UsbConstants.USB_DIR_IN:
                direction="USB_DIR_IN";
                break;
            case UsbConstants.USB_DIR_OUT:
                direction="USB_DIR_OUT";
                break;
            default:
                direction="null";
                break;
        }
        return direction;
    }


}
