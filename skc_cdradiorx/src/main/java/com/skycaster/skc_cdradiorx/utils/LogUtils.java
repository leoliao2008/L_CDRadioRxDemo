package com.skycaster.skc_cdradiorx.utils;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2016/11/25.
 */
public class LogUtils {
    private static boolean deActivateLog;
    public static ArrayList<String> historyLog=new ArrayList<>();

    /**
     *封装好的显示系统log的类
     * @param context 当前activity
     * @param message 需要显示的信息
     */
    public static void showLog(Activity context,String message){
        if(!deActivateLog){
            Log.e(context.getLocalClassName(),message);
            historyLog.add(message);
        }
    }

    public static void showLog(String tag,String message){
        if(!deActivateLog){
            Log.e(tag, message);
            historyLog.add(message);
        }
    }
}
