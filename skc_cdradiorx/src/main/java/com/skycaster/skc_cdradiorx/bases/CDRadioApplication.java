package com.skycaster.skc_cdradiorx.bases;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.skycaster.skc_cdradiorx.beans.DSP;


/**
 * 针对DSP App开发专用的Application，务必在manifest中取代开发系统默认的Application。
 *
 * Created by 廖华凯 on 2016/11/17.
 */
public class CDRadioApplication extends Application {
    private static Context mContext;//全局context,方便各方调用
    private static DSP dsp;//全局DSP对象，方便各方调用
    private static Handler handler=new Handler();
    private static boolean isUpgradeMode;//判断下一次连接DSP时是否要升级
    private static boolean isReceivingData;

    public static boolean isUpgradeMode() {
        return isUpgradeMode;
    }

    public static void setIsUpgradeMode(boolean isUpgradeMode) {
        CDRadioApplication.isUpgradeMode = isUpgradeMode;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext=getBaseContext();
    }

    public static boolean isReceivingData() {
        return isReceivingData;
    }

    public static void setIsReceivingData(boolean isReceivingData) {
        CDRadioApplication.isReceivingData = isReceivingData;
    }

    /**
     * @return 获取全局context
     */
    public static Context getGlobalContext(){
        return mContext;
    }

    /**
     * 更新全局DSP对象，此对象包含了DSP多种参数
     * @param dsp dsp对象
     */
    public static void setDsp(DSP dsp){
        CDRadioApplication.dsp = dsp;}

    /**
     * 获取全局DSP对象
     * @return dsp对象
     */
    public static DSP getDsp() {
        return dsp;
    }


    /**
     * 相当于一个全局的Handler的post(),可以在任意子线程中调用此方法修改UI
     * @param runnable
     */
    public static void post(Runnable runnable){
        handler.post(runnable);
    }
}
