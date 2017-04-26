package com.skycaster.l_cdradiorxdemo.utils;

import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2016/12/6.
 */
public class CacheUtil {
    private static ArrayList<byte[]>list=new ArrayList<>();
    private static boolean isFull;
    public synchronized static void putToCache(byte[]value,CacheFullListener listener){
        if(!isFull){
            if(list.size()<=2553){
                list.add(value);
            }else {
                isFull=true;
                listener.onCacheFull();
            }
        }
    }

//    public synchronized static void writeToTunerFile(){
//        for(byte[] b:list){
//            LocalFileUtils.writeTunerFile(b);
//        }
//        LocalFileUtils.stopWriting();
//    }

    public interface CacheFullListener{
        void onCacheFull();
    }
}
