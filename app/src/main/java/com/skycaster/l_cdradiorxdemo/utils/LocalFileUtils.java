package com.skycaster.l_cdradiorxdemo.utils;

import android.content.Context;
import android.os.Environment;

import com.skycaster.l_cdradiorxdemo.R;
import com.skycaster.skc_cdradiorx.bases.CDRadioApplication;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 廖华凯 on 2016/11/21.
 */
public class LocalFileUtils {
    private static File dir;

    /**
     * 测试用
     * @return 获得DSP升级文件
     */
    public static File getUpgradeSourceFile(){
        prepareFile("Upgrade");
        byte[] buffer=new byte[1024*1024];
        int len;
        try {
            InputStream inputStream = CDRadioApplication.getGlobalContext().getResources().openRawResource(R.raw.upgrade_file);
            while ((len=inputStream.read(buffer))!=-1){
                writeNewFile(buffer,0,len);
            }
            stopWritingNewFile();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile;
    }

    public static boolean deleteAllFiles(){
        File dir;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            dir=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), CDRadioApplication.getGlobalContext().getPackageName());
        }else {
            dir=new File(CDRadioApplication.getGlobalContext().getDir("RunningRecord", Context.MODE_WORLD_READABLE), CDRadioApplication.getGlobalContext().getPackageName());
        }
        if(dir.exists()){
            showLog("删除目录被找到");
            deleteFilesUnder(dir);
        }else{
            showLog("删除目录找不到");
        }
        return dir.delete();
    }

    private static void deleteFilesUnder(File dir){
        for(File file:dir.listFiles()){
            if(file.isDirectory()){
                deleteFilesUnder(file);
            }
            file.delete();
        }
    }

    private static void showLog(String msg){
        com.skycaster.l_cdradiorxdemo.utils.LogUtils.showLog("LocalFileUtils", msg);
    }


    private static File newFile;
    private static BufferedOutputStream newBufferedOutPutStream;

    /**
     * 创建新文件
     * @param fileName 文件名，注意不要带后缀，如.bin/.txt
     * @return 最终生成的文件名，名字上含生成日期
     */
    public synchronized static String prepareFile(String fileName){
        Date date=new Date(System.currentTimeMillis());
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddkkmmss");
        String newFileName = fileName + sdf.format(date) + ".bin";
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            dir =new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), CDRadioApplication.getGlobalContext().getPackageName());
        }else {
            dir =new File(CDRadioApplication.getGlobalContext().getDir("RunningRecord", Context.MODE_WORLD_READABLE), CDRadioApplication.getGlobalContext().getPackageName());
        }
        if(!dir.exists()){
            boolean isSuccess = dir.mkdirs();
            if(isSuccess){
                LogUtils.showLog("prepareFile :", "success");
            }else {
                LogUtils.showLog("prepareFile :", "fail");
            }
        }
        newFile=new File(dir, newFileName);
        if(newFile.exists()){
            newFile.delete();
        }
        try {
            boolean isSuccess = newFile.createNewFile();
            if(isSuccess){
                LogUtils.showLog("prepareFile:", "success");
            }else {
                LogUtils.showLog("prepareFile:", "fail");

            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.showLog("prepareFile error:", e.getMessage());
        }
        try {
            newBufferedOutPutStream=new BufferedOutputStream(new FileOutputStream(newFile,true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtils.showLog("cannot find band file to create stream :", e.getMessage());
        }
        return newFileName;
    }

    public static void writeNewFile(final byte[]data,int indexStart,int length){
        try {
            newBufferedOutPutStream.write(data, indexStart, length);
            newBufferedOutPutStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.showLog("write local file() error:", e.getMessage());
        }
    }

    public static void stopWritingNewFile(){
        if(newBufferedOutPutStream!=null){
            try {
                newBufferedOutPutStream.flush();
                newBufferedOutPutStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static BufferedOutputStream bandFileOutPutStream;
    public static synchronized String prepareBandDataFile(){
        Date date=new Date(System.currentTimeMillis());
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddkkmmss");
        String newFileName = "band_data" + sdf.format(date) + ".bin";
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            dir =new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), CDRadioApplication.getGlobalContext().getPackageName());
        }else {
            dir =new File(CDRadioApplication.getGlobalContext().getDir("RunningRecord", Context.MODE_WORLD_READABLE), CDRadioApplication.getGlobalContext().getPackageName());
        }
        if(!dir.exists()){
            boolean isSuccess = dir.mkdirs();
            if(isSuccess){
                LogUtils.showLog("prepareFile :", "success");
            }else {
                LogUtils.showLog("prepareFile :", "fail");
            }
        }
        File bandFile = new File(dir, newFileName);
        if(bandFile.exists()){
            bandFile.delete();
        }
        try {
            boolean isSuccess = bandFile.createNewFile();
            if(isSuccess){
                LogUtils.showLog("prepareFile:", "success");
            }else {
                LogUtils.showLog("prepareFile:", "fail");

            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.showLog("prepareFile error:", e.getMessage());
        }
        try {
            bandFileOutPutStream=new BufferedOutputStream(new FileOutputStream(bandFile,true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtils.showLog("cannot find band file to create stream :", e.getMessage());
        }
        return newFileName;
    }

    public static void writeBandFile(final byte[]data,int indexStart,int length){
        try {
            bandFileOutPutStream.write(data, indexStart, length);
            bandFileOutPutStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.showLog("write local file() error:", e.getMessage());
        }
    }

    public static void stopWritingBandFile(){
        if(bandFileOutPutStream!=null){
            try {
                bandFileOutPutStream.flush();
                bandFileOutPutStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
