package com.skycaster.skc_cdradiorx.beans;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

/**
 * 代表dsp设备的类
 * Created by Administrator on 2016/11/20.
 */

public class DSP {
    private UsbManager usbManager;//负责链接dsp的usbManager
    private UsbInterface usbInterface;//dsp的interface
    private UsbDeviceConnection connection;//dsp的链接
    private UsbDevice usbDevice;//安卓封装好的usb设备对象，也可以理解为本dsp，但可以获取一些dsp硬件上的信息
    private UsbEndpoint eptIn;//dsp的数据输入端
    private UsbEndpoint eptOut;//dsp的数据输出端
    private boolean isReadyToCommu;//dsp是否已经可以通信

    public boolean isReadyToCommu() {
        return isReadyToCommu;
    }

    public void setIsReadyToCommu(boolean isReadyToCommu) {
        this.isReadyToCommu = isReadyToCommu;
    }

    public UsbManager getUsbManager() {
        return usbManager;
    }

    public void setUsbManager(UsbManager usbManager) {
        this.usbManager = usbManager;
    }

    public UsbInterface getUsbInterface() {
        return usbInterface;
    }

    public void setUsbInterface(UsbInterface usbInterface) {
        this.usbInterface = usbInterface;
    }

    public UsbDeviceConnection getConnection() {
        return connection;
    }

    public void setConnection(UsbDeviceConnection connection) {
        this.connection = connection;
    }

    public UsbEndpoint getEptIn() {
        return eptIn;
    }

    public void setEptIn(UsbEndpoint eptIn) {
        this.eptIn = eptIn;
    }

    public UsbEndpoint getEptOut() {
        return eptOut;
    }

    public void setEptOut(UsbEndpoint eptOut) {
        this.eptOut = eptOut;
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }

    public void setUsbDevice(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

    /**
     * 包含DSP工作模式（原始数据传输、业务数据传输）
     */
    public enum OperatingMode{
        TunerMode,//原始数据传输模式
        BusinessMode//业务数据传输模式
    }
}
