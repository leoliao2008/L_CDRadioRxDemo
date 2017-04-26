package com.skycaster.l_cdradiorxdemo.activities;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.skycaster.l_cdradiorxdemo.R;
import com.skycaster.l_cdradiorxdemo.adapters.TunerDataAdapter;
import com.skycaster.l_cdradiorxdemo.beans.TunerData;
import com.skycaster.l_cdradiorxdemo.utils.CacheUtil;
import com.skycaster.l_cdradiorxdemo.utils.LogUtils;
import com.skycaster.l_cdradiorxdemo.utils.ToastUtil;
import com.skycaster.skc_cdradiorx.abstr.BaseActivity;
import com.skycaster.skc_cdradiorx.bases.CDRadioApplication;
import com.skycaster.skc_cdradiorx.beans.DSP;
import com.skycaster.skc_cdradiorx.manager.DSPManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {
    private DSP dsp;//DSP对象，包含一系列dsp属性
    private UsbDevice usbDevice;//DSP设备
    private TextView tv_vendorId;//厂家id
    private TextView tv_productId;//设备id
    private Button btn_detectDevice;//检测设备
    private Button btn_upgradeDevice;//升级设备
    private UsbInterface usbInterface;//DSP接口
    private boolean isConnected;//移动设备是否已经可以和DSP通信
    private TextView tv_upgradeFeedback;//DSP升级返回码
    private Button btn_sendUpgradePackage;//开始发送升级包
    private LinearLayout ll_upgradePkFb;//DSP接受升级包后的返回码放到这里显示
    private Button btn_activate_DSP;//启动设备
    private TextView tv_activate_feedback;//启动设备后DSP返回的数据
    private EditText edt_inputPinpian;//输入频偏的设定
    private Button btn_setPinpian;//设置频偏的按钮
    private TextView tv_pinpianResult;//显示频偏设置结果
    private EditText edt_inputShipian;//输入时偏的设定
    private Button btn_setShipian;//设置时偏的按钮
    private TextView tv_shipianResult;//显示时偏设置结果
    private EditText edt_inputPindian;//输入频点的设定
    private Button btn_setPindian;//设置频点的按钮
    private TextView tv_pindianResult;//显示频点的设置结果
    private Button btn_deactivateDSP;//停止设备
    private TextView tv_deactivateDSP_feedback;//显示停止设备的返回结果
    private Button btn_getDSPInfo;//查询设备版本
    private TextView tv_getDSPInfoFeedback;//返回查询数据
    private Button btn_getDSPStatus;//查询设备当前运行状态
    private TextView tv_getDSPStatusFb;//返回查询数据
    private Button btn_setGaotong;//设置高通参数
    private TextView tv_showGaotong;//返回设置结果
    private Button btn_setDaitong;//设置带通参数
    private TextView tv_showDaitong;//返回设置结果
    private Button btn_startBrandTransfer;//启动原始数据传输
    private TextView tv_brandTransferFb;//原始数据操作状态
    private Button btn_toBznBage;//点击登陆到业务数据页面
    private DSPManager dspManager;

    private ArrayList<TunerData> list=new ArrayList<>();
    private TunerDataAdapter adpt;
    private ListView listView;
    private Button btn_stopTranfer;
    private TextView tv_stopTranferFb;
    private byte[] temp=new byte[4106];//数据容器
    private int expectedFrameId=1;//本来应该这是个帧id
    private int actualFrameId;//实际上读到的是这个帧id
    private int missCount;//丢包数
    private TextView tv_missCount;
    private CacheUtil.CacheFullListener cacheFullListener=new CacheUtil.CacheFullListener() {
        @Override
        public void onCacheFull() {
            ToastUtil.showToast("可以停掉原始数据传输了");
        }
    };
    private Button btn_checkFrameId;
    private TextView tv_currentId;
    private TextView tv_nextId;
    private TextView tv_matchResult;
    private Handler handler=new Handler();
    private int len=-1;
    private int loop=0;
    private int previousFrameId;
    private int currentFrameId;
    private Button btn_totalMemo;
    private TextView tv_totalMemo;
    private Button btn_freeMemo;
    private TextView tv_freeMemo;
    private Button btn_maxMemo;
    private TextView tv_maxMemo;
    private Button btn_writeData;

    private boolean isRunning;

    ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(1,1,0, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>());
//    private LinkedHashMap<Integer,byte[]>daqData=new LinkedHashMap<>();
    private LinkedList<byte[]> daqData=new LinkedList<>();
//    private LinkedHashMap<Integer,byte[]>bizData=new LinkedHashMap<>();
    private ArrayList<byte[]> bizData =new ArrayList<>();

    private Button btn_startService;
    private TextView tv_startServcieFb;
    private Button btn_stopService;
    private TextView tv_stopServiceFb;
    private Button btn_writeService;
    private Button btn_testService;
    private TextView tv_bizDataCount;
    private int serviceMissCount;
    private Button btn_setParams;
    private Button btn_clearCache;
    private byte[] serviceData=new byte[64];
    private Button btn_dspDetail;
    private Button btn_madStart;
    private TextView tv_madStartFb;
    private LinkedList<byte[]>bandDataPool=new LinkedList<>();
    private ListView bandListView;
    private ArrayList<TunerData> bandList=new ArrayList<>();
    private TunerDataAdapter bandAdpt;
    private TextView tv_bandLossCount;
    private Button btn_writeBandData;
    private Button btn_checkBandId;
    private TextView tv_checkBandIdResult;


    public static void startActivity(Activity context){
        context.startActivity(new Intent(context, MainActivity.class));

    }

    @Override
    protected int getContentView() {
        return R.layout.activity_main_2;
    }

    @Override
    protected void initData() {
        dspManager = DSPManager.getDSPManager();
        adpt=new TunerDataAdapter(list,this);
        listView.setAdapter(adpt);

        bandAdpt=new TunerDataAdapter(bandList,this);
        bandListView.setAdapter(bandAdpt);
    }


    private void setDspCommuParams(DSP dsp){
        usbDevice= dsp.getUsbDevice();
        usbInterface= dsp.getUsbInterface();
        isConnected= dsp.isReadyToCommu();
    }


    @Override
    protected void initView() {
        tv_vendorId= (TextView) findViewById(R.id.main_tv_vendor_id);
        tv_productId= (TextView) findViewById(R.id.main_tv_product_id);
        btn_detectDevice= (Button) findViewById(R.id.main_btn_detect_device);
        btn_upgradeDevice= (Button) findViewById(R.id.main_btn_upgrade_device);
        tv_upgradeFeedback= (TextView) findViewById(R.id.main_tv_upgrade_feedback);
        btn_sendUpgradePackage= (Button) findViewById(R.id.main_btn_send_update_package);
        ll_upgradePkFb= (LinearLayout) findViewById(R.id.main_ll_update_package_feedback_container);
        btn_activate_DSP= (Button) findViewById(R.id.main_btn_activate_device);
        tv_activate_feedback= (TextView) findViewById(R.id.main_tv_activate_feedback);
        edt_inputPinpian= (EditText) findViewById(R.id.main_edt_input_pinpian);
//        btn_setPinpian= (Button) findViewById(R.id.main_btn_set_pinpian);
//        tv_pinpianResult= (TextView) findViewById(R.id.main_tv_pinpian_setting_result);
        edt_inputShipian= (EditText) findViewById(R.id.main_edt_input_shipian);
//        btn_setShipian= (Button) findViewById(R.id.main_btn_set_shipian);
//        tv_shipianResult= (TextView) findViewById(R.id.main_tv_shipian_setting_result);
        edt_inputPindian= (EditText) findViewById(R.id.main_edt_input_pindian);
//        btn_setPindian= (Button) findViewById(R.id.main_btn_set_pindian);
//        tv_pindianResult= (TextView) findViewById(R.id.main_tv_pindian_setting_result);
        btn_deactivateDSP= (Button) findViewById(R.id.main_btn_deactivate_device);
        tv_deactivateDSP_feedback= (TextView) findViewById(R.id.main_tv_deactivate_feedback);
        btn_getDSPInfo= (Button) findViewById(R.id.main_btn_get_device_info);
        tv_getDSPInfoFeedback= (TextView) findViewById(R.id.main_tv_device_info_feedback);
        btn_getDSPStatus= (Button) findViewById(R.id.main_btn_get_status);
        tv_getDSPStatusFb= (TextView) findViewById(R.id.main_tv_get_status_feedback);
        btn_setGaotong= (Button) findViewById(R.id.btn_set_gaotong_param);
        tv_showGaotong= (TextView) findViewById(R.id.tv_set_gaotong_param_result);
        btn_setDaitong= (Button) findViewById(R.id.btn_set_daitong_param);
        tv_showDaitong= (TextView) findViewById(R.id.tv_set_daitong_param_result);
        btn_startBrandTransfer= (Button) findViewById(R.id.main_btn_start_brand_transfer);
        tv_brandTransferFb= (TextView) findViewById(R.id.main_tv_brand_transfer_feedback);
//        btn_toBznBage= (Button) findViewById(R.id.main_btn_start_service_data);

        listView= (ListView) findViewById(R.id.show_brand_transfer_listview);
        btn_stopTranfer= (Button) findViewById(R.id.show_brand_transfer_stop);
        tv_stopTranferFb= (TextView) findViewById(R.id.show_brand_transfer_feed_back);
        tv_missCount= (TextView) findViewById(R.id.show_brand_transfer_tv_miss_count);
        btn_checkFrameId= (Button) findViewById(R.id.main_btn_check_frame_id);
//        tv_currentId= (TextView) findViewById(R.id.main_tv_current_num);
//        tv_nextId= (TextView) findViewById(R.id.main_tv_next_num);
        tv_matchResult= (TextView) findViewById(R.id.main_tv_miss_count_result);
        btn_totalMemo= (Button) findViewById(R.id.main_btn_total_memo);
        tv_totalMemo= (TextView) findViewById(R.id.main_tv_total_memo);
        btn_freeMemo= (Button) findViewById(R.id.main_btn_free_memo);
        tv_freeMemo= (TextView) findViewById(R.id.main_tv_free_memo);
        btn_maxMemo= (Button) findViewById(R.id.main_btn_max_memo);
        tv_maxMemo= (TextView) findViewById(R.id.main_tv_max_memo);
        btn_writeData= (Button) findViewById(R.id.btn_write_tuner_data);

        btn_startService= (Button) findViewById(R.id.main_btn_start_service_data);
        tv_startServcieFb= (TextView) findViewById(R.id.main_tv_start_service_data_feed_back);
        btn_stopService= (Button) findViewById(R.id.main_btn_stop_service_data);
        tv_stopServiceFb= (TextView) findViewById(R.id.main_tv_stop_service_data_feed_back);
        btn_writeService= (Button) findViewById(R.id.main_btn_write_service_data);
        btn_testService= (Button) findViewById(R.id.main_btn_test_service_data);
        tv_bizDataCount = (TextView) findViewById(R.id.main_tv_service_data_test_result);
        btn_setParams= (Button) findViewById(R.id.btn_set_params_at_once);

        btn_clearCache= (Button) findViewById(R.id.main_btn_clear_cache);
        btn_dspDetail= (Button) findViewById(R.id.main_btn_check_dsp_detail);
        btn_madStart= (Button) findViewById(R.id.main_btn_mad_start);
        tv_madStartFb= (TextView) findViewById(R.id.main_tv_mad_start_fb);

        bandListView= (ListView) findViewById(R.id.main_lstv_service_data_console);
        tv_bandLossCount= (TextView) findViewById(R.id.main_tv_band_data_loss_count);
        btn_writeBandData= (Button) findViewById(R.id.main_btn_write_band_data);

        btn_checkBandId= (Button) findViewById(R.id.main_btn_check_band_id);
        tv_checkBandIdResult= (TextView) findViewById(R.id.main_tv_check_band_id_result);

        btn_toBznBage= (Button) findViewById(R.id.main_btn_to_biz_page);

    }

    @Override
    protected void initListener() {
        //点击检测DSP
        btn_detectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSuccess= dspManager.connectDSP();
                if(isSuccess){
                    setDspCommuParams(CDRadioApplication.getDsp());
                    tv_vendorId.setText("" + usbDevice.getVendorId());
                    tv_productId.setText("" + usbDevice.getProductId());
                }else {
                    tv_vendorId.setText("null");
                    tv_productId.setText("null");
                }
            }
        });
        //检测dsp同时初始化dsp的频点、两项滤波参数
        btn_madStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSuccess= false;
                try {
                    isSuccess = dspManager.apiOpenCDRadio(98, 36, 45);
                } catch (DSPManager.FreqOutOfRangeException e) {
                    e.printStackTrace();
                }
                if(isSuccess){
                    setDspCommuParams(CDRadioApplication.getDsp());
                    tv_madStartFb.setText("Success!");
                }else {
                    tv_madStartFb.setText("Fail!");
                }

            }
        });

        //跳到显示dsp参数的页面
        btn_dspDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CDRadioApplication.getDsp()!=null){
                    DSPInfo.startActivity(MainActivity.this);
                }
            }
        });

        //显示当前app占用内存
        btn_totalMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long totalMemory = Runtime.getRuntime().totalMemory();
                tv_totalMemo.setText(totalMemory/1024/1024+"M");
            }
        });

        //显示当前app被分配的内存中可用部分
        btn_freeMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_freeMemo.setText(Runtime.getRuntime().freeMemory() / 1024 / 1024 + "M");
            }
        });

        //显示当前app被系统允许分配的最大内存
        btn_maxMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_maxMemo.setText(Runtime.getRuntime().maxMemory()/1024/1024+"M");
            }
        });

        //点击升级DSP
        btn_upgradeDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("待补充");
            }
        });

        //点击发送升级包
        btn_sendUpgradePackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //启动设备
        btn_activate_DSP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSuccess = dspManager.startDevice();
                if(isSuccess){
                    tv_activate_feedback.setText("启动成功");
                }else {
                    tv_activate_feedback.setText("启动失败");
                }
            }
        });

        //停止设备
        btn_deactivateDSP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSuccess = dspManager.apiStopDevice();
                if(isSuccess){
                    tv_deactivateDSP_feedback.setText("停止成功");
                }else {
                    tv_deactivateDSP_feedback.setText("停止失败");
                }
            }
        });

//        //查询设备
//        btn_getDSPInfo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dspManager.getDSPVersionInfo(new RequestUtils.RequestCallBack() {
//                    @Override
//                    public void onGetAckBean(AckBean ackBean) {
//                        if (ackBean != null && ackBean.getAckStatusCode() == 1) {
//                            tv_getDSPInfoFeedback.setText(ackBean.getVersionInfo());
//                        } else {
//                            tv_getDSPInfoFeedback.setText("操作失败");
//                        }
//                    }
//                });
//
//            }
//        });
//
//        //读取设备工作状态
//        btn_getDSPStatus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dspManager.getDeviceStatusInfo(new RequestUtils.RequestCallBack() {
//                    @Override
//                    public void onGetAckBean(AckBean ackBean) {
//                        if (ackBean != null && ackBean.getAckStatusCode() == 1) {
//                            tv_getDSPStatusFb.setText(ackBean.getDSPStatus());
//                        } else {
//                            tv_getDSPStatusFb.setText("操作失败");
//                        }
//                    }
//                });
//            }
//        });

//        //设置dsp参数
//        btn_setParams.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int fOffset;
//                int tOffset;
//                int f;
//                String freqOffset = edt_inputPinpian.getText().toString().trim();
//                String timeOffset=edt_inputShipian.getText().toString().trim();
//                String freq=edt_inputPindian.getText().toString().trim();
//                if(!TextUtils.isEmpty(freqOffset)){
//                    fOffset=Integer.valueOf(freqOffset);
//                }else {
//                    fOffset=Integer.MAX_VALUE;
//                }
//                if(!TextUtils.isEmpty(timeOffset)){
//                    tOffset=Integer.valueOf(timeOffset);
//                }else {
//                    tOffset=Integer.MAX_VALUE;
//                }
//                if(!TextUtils.isEmpty(freq)){
//                    f=Integer.valueOf(freq);
//                }else {
//                    f=Integer.MAX_VALUE;
//                }
//                boolean b = false;
//                b = dspManager.setDSPParams(fOffset, tOffset);
//                if(b){
//                    ToastUtil.showToast("设置成功");
//                }else {
//                    ToastUtil.showToast("设置失败");
//                }
//            }
//        });

        //设置滤波参数
        btn_setGaotong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setLvParam(LvParameter.LvParamType.HPF);

            }
        });

        btn_setDaitong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setLvParam(LvParameter.LvParamType.SBPF);
            }
        });

        //开始原始数据传输
        btn_startBrandTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = dspManager.apiOpenDAQ(new DSPManager.TunerDataListener() {
                    @Override
                    public void preTask() {
                        daqData.clear();
                        list.clear();
                        adpt.notifyDataSetChanged();
                    }

                    @Override
                    public void onReceiveData(final byte[] data, final int frameId) {
                        daqData.add(data);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                list.add(new TunerData(frameId, (short) 4106));
                                if (list.size() > 20) {
                                    list.remove(0);
                                }
                                adpt.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onPacketMiss(final int missCount) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                tv_missCount.setText(missCount + "");
                            }
                        });

                    }

                    @Override
                    public void onTunerStop() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                tv_stopTranferFb.setText("Status:Stop");
                            }
                        });
                    }
                });
                if(result){
                    tv_brandTransferFb.setText("Status:On");
                }else {
                    tv_brandTransferFb.setText("Status: Request Failed");
                }

            }
        });

        //结束原始数据传输
        btn_stopTranfer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!dspManager.apiStopDAQ()){
                    tv_stopTranferFb.setText("Fail");
                }
            }
        });


        //检测缓存中的原始数据序号是否连续
        btn_checkFrameId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchronized (this){
                    missCount=0;
                    len=-1;
                    loop=0;
                    previousFrameId=0;
                    currentFrameId=0;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for(byte[] data:daqData){
                                if(loop==0){
                                    previousFrameId=(data[7]<<8|data[6]&0xff)&0x0ffff;
                                }else {
                                    currentFrameId=(data[7]<<8|data[6]&0xff)&0x0ffff;
                                    if(currentFrameId-previousFrameId!=1){
                                        LogUtils.showLog(MainActivity.this, "------result mismatch,first num: " + previousFrameId + ",second num: " + currentFrameId + ".");
                                        missCount++;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                tv_matchResult.setText(missCount + "");
                                            }
                                        });
                                    }else {
                                        LogUtils.showLog(MainActivity.this,"------result ok,first num: "+previousFrameId+",second num: "+currentFrameId+".");
                                    }
                                    previousFrameId=currentFrameId;
                                }
                                loop++;
                            }
                        }
                    }).start();
                }
            }
        });

//        //把缓存中原始数据写到本地
//        btn_writeData.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(daqData.size()==0){
//                    ToastUtil.showToast("无数据");
//                    return;
//                }
//                LocalFileUtils.prepareTunerFile();
//                for(byte[] data:daqData){
//                    LocalFileUtils.writeTunerFile(data);
//                }
//                LocalFileUtils.stopWriting();
//                daqData.clear();
//                ToastUtil.showToast("写入完成！");
//            }
//        });



//        //开始基带数据传输
//        btn_startService.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                boolean isSuccess = dspManager.apiGetService(serviceData,64, (byte) 33, new DSPManager.BusinessDataListener() {
//                    @Override
//                    public void preTask() {
//                        bandDataPool.clear();
//                        bizData.clear();
//                        bandList.clear();
//                        bandAdpt.notifyDataSetChanged();
//                        missCount=0;
//                        tv_bandLossCount.setText(missCount + "");
//                    }
//
//                    @Override
//                    public void onGetBandData(final byte[] bandData, final int frameId) {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                bandDataPool.add(bandData);
//                                bandList.add(new TunerData(frameId, (short) 4106));
//                                if(bandList.size()>20){
//                                    bandList.remove(0);
//                                }
//                                bandAdpt.notifyDataSetChanged();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onGetBizData(byte[] data) {
//                        bizData.add(data);
//                    }
//
//                    @Override
//                    public void onPacketMiss(final int missCount) {
//                        tv_stopServiceFb.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                tv_bandLossCount.setText(missCount + "");
//                            }
//                        });
//
//                    }
//
//                    @Override
//                    public void onServiceStop() {
//                        tv_stopServiceFb.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                tv_stopServiceFb.setText("Success");
//                            }
//                        });
//                    }
//                });
//                if(isSuccess){
//                    tv_startServcieFb.setText("Success");
//                }else {
//                    tv_startServcieFb.setText("Fail");
//                }
//            }
//        });

        //停止基带数据传输
        btn_stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                isRunning=false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!dspManager.apiStopService()){
                            tv_stopServiceFb.post(new Runnable() {
                                @Override
                                public void run() {
                                    tv_stopServiceFb.setText("Fail");
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        //显示业务数据数量
        btn_testService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_bizDataCount.setText(bizData.size() + "");
            }
        });


        //检查缓存中基带数据是否掉帧
        btn_checkBandId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchronized (this){
                    missCount=0;
                    len=-1;
                    loop=0;
                    previousFrameId=0;
                    currentFrameId=0;
                    tv_checkBandIdResult.setText(missCount+"");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for(byte[] data:bandDataPool){
                                if(loop==0){
                                    previousFrameId=(data[7]<<8|data[6]&0xff)&0x0ffff;
                                }else {
                                    currentFrameId=(data[7]<<8|data[6]&0xff)&0x0ffff;
                                    if(currentFrameId-previousFrameId!=1){
                                        LogUtils.showLog(MainActivity.this,"------result mismatch,first num: "+previousFrameId+",second num: "+currentFrameId+".");
                                        missCount++;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                tv_checkBandIdResult.setText(missCount + "");
                                            }
                                        });
                                    }else {
                                        LogUtils.showLog(MainActivity.this,"------result ok,first num: "+previousFrameId+",second num: "+currentFrameId+".");
                                    }
                                    previousFrameId=currentFrameId;
                                }
                                loop++;
                            }
                        }
                    }).start();
                }

            }
        });

//        //把基带数据写到本地
//        btn_writeBandData.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(bandDataPool.size()==0){
//                    ToastUtil.showToast("无数据");
//                    return;
//                }
//                LocalFileUtils.prepareBandFile();
//                for(byte[] data: bandDataPool){
//                    LocalFileUtils.writeBandFile(data);
//                }
//                LocalFileUtils.stopWritingBandFile();
//                bandDataPool.clear();
//                ToastUtil.showToast("写入完成！");
//            }
//        });
//
//        //把业务数据写到本地
//        btn_writeService.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(bizData.size()==0){
//                    ToastUtil.showToast("无数据");
//                    return;
//                }
//                LocalFileUtils.prepareBusinessFile();
//                for(byte[] data: bizData){
//                    LocalFileUtils.writeBusinessFile(data);
//                }
//                LocalFileUtils.stopWriting();
//                bizData.clear();
//                ToastUtil.showToast("写入完成！");
//            }
//        });

        //清除缓存
        btn_clearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bizData.clear();
                daqData.clear();
                bandDataPool.clear();
            }
        });

        btn_toBznBage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusinessDemo.startActivity(MainActivity.this);
            }
        });



    }





}
