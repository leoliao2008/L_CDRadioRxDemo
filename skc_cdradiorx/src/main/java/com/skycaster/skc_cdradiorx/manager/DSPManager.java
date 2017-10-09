package com.skycaster.skc_cdradiorx.manager;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;

import com.skycaster.jniutils.JniHelper;
import com.skycaster.skc_cdradiorx.Constant;
import com.skycaster.skc_cdradiorx.abstr.BusinessDataListener;
import com.skycaster.skc_cdradiorx.bases.CDRadioApplication;
import com.skycaster.skc_cdradiorx.beans.DSP;
import com.skycaster.skc_cdradiorx.beans.DSPParam;
import com.skycaster.skc_cdradiorx.beans.DataQuality;
import com.skycaster.skc_cdradiorx.beans.LvParameter;
import com.skycaster.skc_cdradiorx.beans.RequestBean;
import com.skycaster.skc_cdradiorx.factory.RequestFactory;
import com.skycaster.skc_cdradiorx.intface.RequestCallBack;
import com.skycaster.skc_cdradiorx.service.BusinessService;
import com.skycaster.skc_cdradiorx.service.TunerService;
import com.skycaster.skc_cdradiorx.utils.LogUtils;
import com.skycaster.skc_cdradiorx.utils.RequestUtils;
import com.skycaster.skc_cdradiorx.utils.ToastUtil;
import com.skycaster.skc_cdradiorx.utils.UsbUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * 这是一个DSP设备管理类，封装了调用DSP功能的各项方法。
 * Created by 廖华凯 on 2016/11/24.
 */
public class DSPManager {

    private static DSPManager mDSPManager;
    private static byte[] ackHolder =new byte[Constant.DSP_DATA_LENGTH];
    private JniHelper jniHelper= JniHelper.getJniHelper();
    private ServiceConnection bizServiceConnection;
    private ServiceConnection tunerServiceConnection;

    private DSPManager() {}

    /**
     * 检测USB端口上的设备，如果是DSP则尝试初始化连接。
     * @return true表示连接成功，false表示连接失败。
     */
    public synchronized boolean connectDSP(){
        return UsbUtil.detectUSB();
    }

    /**
     * 重新初始化与DSP的连接。
     * @return true表示连接成功，false表示连接失败。
     */
    public synchronized boolean resetConnection(){
        return UsbUtil.resetConnection();
    }


    /**
     * 利用单例模式初始化并获取设备管理者对象
     * @return 一个唯一的设备管理者对象
     */
    public synchronized static DSPManager getDSPManager(){
        if(mDSPManager ==null){
            mDSPManager =new DSPManager();
        }
        return mDSPManager;
    }


    private File upgradeFile;//升级文档
    private RequestBean upgradeRequest;//升级请求
    private UpgradeListener upgradeListener;
    private boolean isPostDelete;

    /**
     * 向DSP发出升级DSP系统的请求，注意：如果返回成功，DSP需要断电重启并与移动设备重新连接，连接后DSP将进入boot模式，
     * 在boot模式下会自动从移动设备获取升级资源。移动设备这段时间不能关闭或退出本程序，如果关闭或退出了，需要重新发送
     * 本请求，重走升级流程。
     * @param scr 升级文件。
     * @param upgradeListener 升级回调，可以监听升级进度。
     * @param isPostDelete 升级成功后是否删除升级文件。
     * @return true表示请求成功，false表示请求失败。
     */
    public boolean prepareDSPUpgrade(File scr,UpgradeListener upgradeListener,boolean isPostDelete){
        if(CDRadioApplication.isReceivingData()){
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("正在传输数据，请先关闭数据传输");
                }
            });
            return false;
        }
        this.isPostDelete=isPostDelete;
        this.upgradeFile=scr;
        this.upgradeListener=upgradeListener;
        this.upgradeRequest = RequestFactory.requestUpgrade(scr);
        boolean isSuccess= RequestUtils.sendRequest(upgradeRequest);
        if(isSuccess){
            CDRadioApplication.setIsUpgradeMode(true);
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("请重新连接DSP");
                }
            });
        }else {
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("升级请求失败，请重新尝试");
                }
            });
        }
        return isSuccess;
    }

    private boolean prepareDSPUpgrade(){
        return RequestUtils.sendRequest(upgradeRequest);
    }

    /**
     * 向DSP发送升级数据。本方法一般不建议开发者调用，仅用于封装本SDK其他程序。
     */
    public synchronized void sendUpgradePackage() {
        if(upgradeListener==null){
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("升级回调为null");
                }
            });
            return;
        }
        if(prepareDSPUpgrade()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    CDRadioApplication.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast("开始发送升级文件，请不要关闭程序或执行其他操作。");
                        }
                    });
                    RequestBean requestBean=new RequestBean(0x0002);//0x0002代表发送升级文件数据
                    byte[] validData=new byte[4096];//初始化有效数据
                    int validLen=-1;//初始化有效数据长度
                    long fileSize=upgradeFile.length();
                    final int frameCount= (int) (fileSize%4096==0?fileSize/4096:(fileSize/4096+1));//根据升级文件大小计算升级包个数
                    requestBean.setTotalFrame(frameCount);//初始化总帧数
                    short frameId=0;//初始化当前帧序号
                    BufferedInputStream bis=null;
                    try {
                        bis=new BufferedInputStream(new FileInputStream(upgradeFile));
                        while ((validLen=bis.read(validData))!=-1){
                            frameId++;
                            requestBean.setFrameId(frameId);
                            requestBean.setData(Arrays.copyOf(validData, validLen));
                            if (!RequestUtils.sendRequest(requestBean)){
                                showLog("第" + frameId + "帧发送不成功");
                                CDRadioApplication.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        upgradeListener.onFinish(false);
                                        ToastUtil.showToast("升级文件传输失败，请不要关掉本程序，并重新连接DSP");
                                    }
                                });
                                bis.close();
                                return;
                            }
                            final short currentId = frameId;
                            CDRadioApplication.post(new Runnable() {
                                @Override
                                public void run() {
                                    upgradeListener.onProgress(frameCount, currentId);
                                }
                            });
                        }
                        CDRadioApplication.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast("升级成功！");
                                upgradeListener.onFinish(true);
                            }
                        });
                        CDRadioApplication.setIsUpgradeMode(false);
                        if(isPostDelete){
                            if(upgradeFile.delete()){
                                showLog("已经删除升级文件");
                            }else {
                                showLog("删除升级文件失败");
                            }
                            isPostDelete=false;//返回初始值
                        }
                        upgradeFile=null;
                        upgradeRequest =null;

                    } catch (Exception e) {
                        showLog(e.getMessage());
                        upgradeListener.onFinish(false);
                    }finally {
                        if(bis!=null){
                            try {
                                bis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            bis=null;
                        }
                    }
                }
            }).start();
        }else {
            showLog("启动升级请求失败");
            upgradeListener.onFinish(false);
        }

    }


    /**
     * DSP系统升级回调，可以监听升级进度及结果
     * @author 廖华凯
     *
     */
    public interface UpgradeListener {
        /**
         * 此方法返回升级进度
         * @param totalFrame 升级文件总帧数
         * @param currentFrame 当前传输的帧序号，帧序号从1开始。
         */
        void onProgress(int totalFrame, int currentFrame);

        /**
         * 返回最终升级结果
         * @param isSuccess true表示升级成功，false表示升级失败
         */
        void onFinish(boolean isSuccess);
    }


    /**
     * 检测USB端口，如果未初始化DSP连接则首先尝试初始化DSP连接；然后在连接正常的前提下给DSP设置频点、滤波参数。
     * @param freq 频点,默认98 MHz
     * @param toneLeft 左频，默认36；左频、右频将决定滤波参数的值。
     * @param toneRight 右频，默认45；左频、右频将决定滤波参数的值。
     * @return true表示成功，false表示失败
     * @throws FreqOutOfRangeException 频点的取值范围必须在64（含）-108（含）内，否则会弹出FreqOutOfRangeException
     */
    public  boolean apiOpenCDRadio(double freq, int toneLeft, int toneRight) throws FreqOutOfRangeException {
        if(CDRadioApplication.isReceivingData()){
            showLog("正在传输数据，请先关闭数据传输");
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("正在传输数据，请先关闭数据传输");
                }
            });
            return false;
        }

        boolean isSuccess=true;
        //寻找并连接设备
        if(CDRadioApplication.getDsp()==null||!CDRadioApplication.getDsp().isReadyToCommu()){
            isSuccess= connectDSP();
        }
        if(isSuccess){
            showLog("设备连接成功");
            byte[] gaotong=new byte[140];
            byte[] daitong=new byte[180];
            jniHelper.cdradio_rx_cfg_spectrum(toneLeft,toneRight);
            jniHelper.cdradio_rx_get_fir_coef(gaotong, daitong);
            LvParameter hpf=new LvParameter(LvParameter.LvParamType.HPF,gaotong);
            LvParameter sbpf=new LvParameter(LvParameter.LvParamType.SBPF,daitong);
            //初始化设备参数
            isSuccess = initDevice(freq, hpf, sbpf);
            if(isSuccess){
                showLog("设备参数设置成功");
            }else {
                showLog("设备参数设置失败");
            }
        }else {
            showLog("设备连接失败，请重新检测设备");
        }
        return isSuccess;
    }




    /**
     * 获取设备当前DSP版本信息
     * @param callBack 接口回调，可以从该回调的onGetAckBean(AckBean ackBean)方法中获取应答对象ackBean，通过
     *                 应答对象ackBean的getVersionInfo()方法获得DSP版本信息。
     */
    public void getDSPVersionInfo(RequestCallBack callBack){
        if(CDRadioApplication.isReceivingData()){
            showLog("正在传输数据，请先关闭数据传输");
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("正在传输数据，请先关闭数据传输");
                }
            });
            return;
        }
        RequestUtils.sendRequest(RequestFactory.requestVersionInfo(), callBack);
    }


    /**
     * 获取DSP当前工作状态
     * @param callBack 接口回调，可以从该回调的onGetAckBean(AckBean ackBean)方法中获取应答对象ackBean，通过
     *                 应答对象ackBean的getAckStatusDescription()方法获得DSP当前工作状态。
     */
    public  void getDeviceStatusInfo(RequestCallBack callBack){
        if(CDRadioApplication.isReceivingData()){
            showLog("正在传输数据，请先关闭数据传输");
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("正在传输数据，请先关闭数据传输");
                }
            });
            return;
        }
        RequestUtils.sendRequest(RequestFactory.requestDeviceStatus(), callBack);
    }


    /**
     * 设置DSP工作参数及工作模式
     * @param freq 频点
     * @param HPF 高通参数
     * @param SBPF 带通参数
     * @return true表示成功，false表示失败
     */
    private  boolean initDevice(double freq,LvParameter HPF,LvParameter SBPF) throws FreqOutOfRangeException {
        //更新频道参数
        boolean configResult = setDSPParams(freq);
        //更新滤波参数--高通滤波
        boolean HPFResult=RequestUtils.sendRequest(RequestFactory.requestSetLvParameter(HPF));
        //更新滤波参数--带通滤波
        boolean SBPFResult=RequestUtils.sendRequest(RequestFactory.requestSetLvParameter(SBPF));
        return configResult&&HPFResult&&SBPFResult;
    }


    private int missCount;
    private int currentFrameId;
    private int expectedFrameId;

    /**
     * 通过生成前台服务获取业务数据
     * @param maxLen 业务数据的最大有效长度，当超出此长度时，后续业务数据将通过下一个数据包接收。
     * @param formCode 操作代码，暂定为33。
     * @param listener 接口回调，在这里处理获得的业务数据。注意，本回调大部分方法在子线程中执行，请参考回调注释。
     * @return true为启动成功、false为失败
     */
    public boolean apiGetService(final Activity context, int maxLen, final byte formCode, final BusinessDataListener listener){
        if(CDRadioApplication.isReceivingData()){
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("已在传输数据，请先停止数据传输。");
                }
            });
            return false;
        }
        boolean isSuccess = RequestUtils.sendRequest(RequestFactory.requestStartBusinessTransfer());
        CDRadioApplication.setIsReceivingData(isSuccess);
        if(isSuccess){
            bizServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    ((BusinessService.ServiceBinder)service).getService().commenceReceiving(listener);
//                    ((BusinessService.ServiceBinder)service).getService().startForeground(123,new Notification());
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
            Intent intent=new Intent(context, BusinessService.class);
            intent.putExtra("com.skycaster.skc_cdradiorx.MaxLen",maxLen);
            intent.putExtra("com.skycaster.skc_cdradiorx.FormCode", formCode);
            CDRadioApplication.getGlobalContext().bindService(intent, bizServiceConnection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
        }else {
            listener.onServiceStop();
        }
        return isSuccess;
    }

    /**
     * 停止业务数据传输
     * @return 返回true代表操作成功，false代表失败
     */
    public  synchronized boolean apiStopService(){
        if(!CDRadioApplication.isReceivingData()){
            showLog("数据传输在本方法执行前已经被停止了，不必重复停止。");
            return true;
        }
        if(bizServiceConnection !=null){
            CDRadioApplication.getGlobalContext().unbindService(bizServiceConnection);
            bizServiceConnection =null;
        }
        boolean isSuccess = RequestUtils.noAckRequest(RequestFactory.requestStopsBusinessTransfer());
        showLog("stop biz result:" + isSuccess);
        return isSuccess;
    }


    /**
     * 重启DSP设备；一般来说当DSP链接到移动设备时，会自动启动DSP，不需要发送一个特定的启动请求;
     * 此方法是当用户发送了停止DSP的请求后，再度启动DSP时用到。
     * @return 返回true代表操作成功，false代表失败
     */
    public boolean startDevice(){
        if(CDRadioApplication.isReceivingData()){
            showLog("正在传输数据，请先关闭数据传输");
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("正在传输数据，请先关闭数据传输");
                }
            });
            return false;
        }
        return RequestUtils.sendRequest(RequestFactory.requestStartDevice());
    }


    /**
     * 启动前台服务，开始接收原始数据
     * @param context 当前活动
     * @param listener 回调，定义接收原始数据时各阶段相应的逻辑
     * @return 返回true表示成功启动服务，false则失败。
     */
    public boolean apiOpenDAQ(Activity context, final com.skycaster.skc_cdradiorx.abstr.TunerDataListener listener){
        tunerServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ((TunerService.TunerServiceBinder)service).getService().commenceTunerTransfer(listener);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        boolean isSuccess = CDRadioApplication.getGlobalContext().bindService(
                new Intent(context, TunerService.class),
                tunerServiceConnection,
                Service.BIND_AUTO_CREATE | Service.BIND_IMPORTANT);
        showLog("start daq service: "+isSuccess);
        return isSuccess;
    }


    /**
     * 向DSP发起原始数据请求并接收原始数据
     * @param listener 回调；注意部分回调将在子线程中执行，请参考回调注释。
     * @return 返回true代表请求成功，false代表请求失败
     */
    public boolean apiOpenDAQ(final TunerDataListener listener){
        if(CDRadioApplication.isReceivingData()){
            ToastUtil.showToast("已在传输数据，请先停止数据传输。");
            return false;
        }
        boolean isSuccess=startDevice();
        if(isSuccess){
            daqStopListener=null;
            isSuccess=RequestUtils.sendRequest(RequestFactory.requestStartTunerTransfer());
            showLog("start daq result:"+isSuccess);
            if(isSuccess){
                jniHelper.cdradio_rx_start();
                CDRadioApplication.setIsReceivingData(true);
                listener.preTask();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DSP dsp= CDRadioApplication.getDsp();
                        UsbDeviceConnection connection=dsp.getConnection();
                        UsbEndpoint eptIn=dsp.getEptIn();
                        missCount=0;
                        expectedFrameId=1;
                        while (CDRadioApplication.isReceivingData()){
                            int bulkTransfer = connection.bulkTransfer(eptIn, ackHolder,  Constant.DSP_DATA_LENGTH, 1000);
                            showLog("bulkTransfer: "+bulkTransfer);
                            if(bulkTransfer<0){
                                //如果dsp通信中断了，在这里重新链接，并退出循环。
                                CDRadioApplication.setIsReceivingData(false);
                                break;
                            }
                            if((ackHolder[2] | ackHolder[3] << 8)==0x000A&&(ackHolder[10]| ackHolder[11]<<8&0xff)==1){
                                CDRadioApplication.setIsReceivingData(false);//如果当前应答命令码为0x000A且状态码为1，说明DSP收到了停止原始数据的命令，且正常执行了该命令，该结束当前循环了。
                                break;
                            }
                            if(daqStopListener!=null&& CDRadioApplication.isReceivingData()){
                                showLog("fail to receive stop daq command,resend stop command...");
                                //双重保险，如果dsp读取不到停止命令，这里重复发送，直到读到为止。
                                daqStopListener.isStopSuccess(false);
                                continue;
                            }
                            if (CDRadioApplication.isReceivingData()) {
                                currentFrameId =((ackHolder[7]<<8| ackHolder[6]&0xff))&0x0ffff;//解析数据，获取数据帧序号
                                currentFrameId =(currentFrameId ==0?65536: currentFrameId);//如果帧序号跳到0，则表示上一次帧序号已经达到了双字节byte数组达到的最大值65535，本次应为65536.
                                listener.onReceiveData(ackHolder.clone(),currentFrameId);
                                //判断是否跳帧，如果跳帧:
                                if (expectedFrameId != currentFrameId){
                                    missCount+=currentFrameId-expectedFrameId;
                                    listener.onPacketMiss(missCount);
                                    expectedFrameId=currentFrameId;
                                }
                                updateFrameId();
                                showLog("当前序号：" + (((ackHolder[7] << 8 | ackHolder[6] & 0xff)) & 0x0ffff));
                            }
                        }
                        jniHelper.cdradio_rx_stop();
                        daqStopListener=null;
                        listener.onTunerStop();
                    }
                }).start();
            }
        }else {
            showLog("启动原始数据传输时启动设备失败");
            listener.onTunerStop();
        }
        return isSuccess;
    }

    /**
     *DSP接收原始数据时的回调
     */
    public interface TunerDataListener{
        /**
         * 开始接收数据前的一些操作，可以在这里修改UI
         */
        void preTask();
        /**
         * 当接收到原始数据时启动,该操作处于子线程中
         * @param data 原始数据
         * @param frameId 原始数据对应的帧序号
         */
        void onReceiveData(byte[] data, int frameId);

        /**
         * 监测跳帧数量，该操作处于子线程中
         * @param missCount 累计跳帧数
         */
        void onPacketMiss(int missCount);

        /**
         * 当停止接收原始数据时启动，该操作处于子线程中
         */
        void onTunerStop();
    }

//    /**
//     * 停止原始数据传输
//     * @return 返回true代表操作成功，false代表失败
//     */
//    public synchronized boolean apiStopDAQ(){
//        if(!CDRadioApplication.isReceivingData()){
//            showLog("数据传输在本方法执行前已经被停止了，不必重复停止。");
//            return true;
//        }
//        boolean isSuccess = RequestUtils.noAckRequest(RequestFactory.requestStopTunerTransfer());
//        showLog("stop daq result:"+isSuccess);
//        if(isSuccess&&daqStopListener==null){
//            daqStopListener=new DaqStopListener() {
//                @Override
//                public void isStopSuccess(boolean success) {
//                    if(!success){
//                        apiStopDAQ();
//                    }
//                }
//            };
//        }
//        return isSuccess;
//    }

    /**
     * 停止原始数据传输
     * @return 返回true代表操作成功，false代表失败
     */
    public synchronized boolean apiStopDAQ(){
        if(tunerServiceConnection!=null){
            CDRadioApplication.getGlobalContext().unbindService(tunerServiceConnection);
        }
        boolean isSuccess = RequestUtils.noAckRequest(RequestFactory.requestStopTunerTransfer());
        if(isSuccess){
            tunerServiceConnection=null;
        }
        return isSuccess;
    }

    private int offsets[]=new int[2];

    /**
     *设置DSP频偏和时偏参数
     * @param freqOffset 频偏
     * @param timeOffset 时偏
     * @return 返回true代表操作成功，false代表失败
     */
    public synchronized boolean setDSPParams(int freqOffset,int timeOffset) {
        offsets[0]=freqOffset;
        offsets[1]=timeOffset;
        return RequestUtils.noAckRequest(RequestFactory.requestSetParameter(new DSPParam(freqOffset, timeOffset)));
    }

    /**
     *设置DSP基础参数，其中频偏、时偏为系统默认值
     * @param freq 频点
     * @return 返回true代表操作成功，false代表失败
     */
    private boolean setDSPParams(double freq) throws FreqOutOfRangeException {
        if(CDRadioApplication.isReceivingData()){
            showLog("正在传输数据，请先关闭数据传输");
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("正在传输数据，请先关闭数据传输");
                }
            });
            return false;
        }
        if(freq>108||freq<64){
            throw new FreqOutOfRangeException("频点的取值范围必须在64（含）-108（含）内");
        }
        return RequestUtils.noAckRequest(RequestFactory.requestSetParameter(new DSPParam(freq)));
    }


    /**
     * 终止DSP的运行
     * @return 返回true代表操作成功，false代表失败
     */
    public synchronized boolean apiStopDevice(){
        if(CDRadioApplication.isReceivingData()){
            showLog("正在传输数据，请先关闭数据传输");
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("正在传输数据，请先关闭数据传输");
                }
            });
            return false;
        }
        return RequestUtils.sendRequest(RequestFactory.requestStopDevice());
    }

    private boolean isMonitoringQuality;
    private byte[] cppLibStatus;
    private double[] snr;
    private int[] counts;



    /**
     * 启动DSP数据传输质量检测，在so库未启动的情况下也可以正常运行,必须在主线程使用此方法。此方法只用于实验室测试，
     * 一般用户不建议调用。
     * @param timeMillis 间隔多少毫秒获取一次统计数据
     * @param monitor 回调
     */
    public synchronized boolean checkDataQuality(final long timeMillis, final DataQualityMonitor monitor){
        if(!CDRadioApplication.isReceivingData()){
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("数据传输未启动");
                }
            });
            return false;
        }
        if(isMonitoringQuality){
            showLog("Data quality is monitoring...");
            return false;
        }
        isMonitoringQuality=true;
        new AsyncTask<Void,DataQuality,Void>(){

            @Override
            protected void onPreExecute() {
                cppLibStatus = new byte[100];
                snr = new double[1];
                counts = new int[2];
            }

            @Override
            protected Void doInBackground(Void... params) {
                while (isMonitoringQuality){
                    jniHelper.cdradio_rx_get_state(cppLibStatus, snr, counts);
                    DataQuality temp=new DataQuality();
                    temp.setCppLibStatus(cppLibStatus);
                    temp.setSnr(snr);
                    temp.setCounts(counts);
                    temp.setOffSets(offsets);
                    publishProgress(temp);
                    SystemClock.sleep(timeMillis);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(DataQuality... values) {
                monitor.onCallBack(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                cppLibStatus=null;
                snr=null;
                counts=null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return true;
    }

    /**
     * 回调，返回DSP数据传播质量对象
     */
    public interface DataQualityMonitor{
        void onCallBack(DataQuality dataQuality);
    }

    /**
     * 停止DSP数据传输质量检测
     */
    public void stopCheckDataQuality(){
        isMonitoringQuality=false;
    }



    private static void showLog(String message){
        LogUtils.showLog("DSPManager:", message);
    }


    private synchronized void updateFrameId(){
        if(currentFrameId!=65536){
            expectedFrameId++;
        }else {
            //如果当前帧序号是65536，则下一帧从1重新开始
            expectedFrameId=1;
        }
    }

    private interface DaqStopListener{
        void isStopSuccess(boolean success);
    }

    private DaqStopListener daqStopListener;

    /**
     * 当设置DSP频点超出范围（64（含）-108（含））时会弹出此错误。
     */
    public class FreqOutOfRangeException extends Exception{
        public FreqOutOfRangeException(String detailMessage) {
            super(detailMessage);
            CDRadioApplication.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast("频点的取值范围必须在64（含）-108（含）内");
                }
            });
        }
    }


}
