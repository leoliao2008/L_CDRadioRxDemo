package com.skycaster.l_cdradiorxdemo.utils;

import android.widget.Toast;

import com.skycaster.skc_cdradiorx.bases.CDRadioApplication;


/**
 * Toast封装版
 * Created by 廖华凯 on 2016/11/17.
 */
public class ToastUtil {
    private static Toast mToast;
    /**
     * 利用封装好的Toast显示信息
     * @param message 信息内容
     */
    public static void showToast(String message){
//        Toast.makeText(CDRadioApplication.getGlobalContext(),message,Toast.LENGTH_SHORT).show();
        if(mToast==null){
            mToast=Toast.makeText(CDRadioApplication.getGlobalContext(),message,Toast.LENGTH_SHORT);
        }else {
            mToast.setText(message);
        }
        LogUtils.historyLog.add(message);
        mToast.show();
    }
}
