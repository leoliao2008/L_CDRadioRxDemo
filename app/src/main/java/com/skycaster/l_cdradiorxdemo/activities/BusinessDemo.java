package com.skycaster.l_cdradiorxdemo.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.skycaster.l_cdradiorxdemo.R;
import com.skycaster.l_cdradiorxdemo.adapters.BizDataAdapter;
import com.skycaster.l_cdradiorxdemo.adapters.DataQualityAdapter;
import com.skycaster.l_cdradiorxdemo.adapters.HistoryLogAdapter;
import com.skycaster.l_cdradiorxdemo.adapters.TunerDataAdapter;
import com.skycaster.l_cdradiorxdemo.beans.TunerData;
import com.skycaster.l_cdradiorxdemo.utils.AlertDialogueUtils;
import com.skycaster.l_cdradiorxdemo.utils.LocalFileUtils;
import com.skycaster.l_cdradiorxdemo.utils.TimingUtils;
import com.skycaster.l_cdradiorxdemo.utils.ToastUtil;
import com.skycaster.l_cdradiorxdemo.widgets.WaveView;
import com.skycaster.skc_cdradiorx.abstr.BaseActivity;
import com.skycaster.skc_cdradiorx.abstr.BusinessDataListener;
import com.skycaster.skc_cdradiorx.abstr.TunerDataListener;
import com.skycaster.skc_cdradiorx.bases.CDRadioApplication;
import com.skycaster.skc_cdradiorx.beans.AckBean;
import com.skycaster.skc_cdradiorx.beans.DataQuality;
import com.skycaster.skc_cdradiorx.intface.RequestCallBack;
import com.skycaster.skc_cdradiorx.manager.DSPManager;
import com.skycaster.skc_cdradiorx.utils.LogUtils;

import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2016/12/14.
 */
public class BusinessDemo extends BaseActivity {
    private Button btn_back;
    private TextView tv_dspVersion;
    private Button btn_dspInfo;
    private DSPManager dspManager;
    private EditText edt_setFreq;
    private EditText edt_toneLeft;
    private EditText edt_toneRight;
    private RadioGroup rdgp_format;
    private ToggleButton tgbtn_startBiz;
    private Button btn_resetConnection;
    private ListView lstv_bizData;
    private ArrayList<byte[]>bizDataList=new ArrayList<>();
    private BizDataAdapter bizDataAdapter=new BizDataAdapter(this,bizDataList);
    private byte[] bizData=new byte[81];
    private Handler handler=new Handler();
    private TextView tv_bizDataCount;
    private TextView tv_missCount;
    private ToggleButton tgbtn_startTuner;
    private ArrayList<DataQuality>qualityList=new ArrayList<>();
    private DataQualityAdapter qualityAdapter=new DataQualityAdapter(qualityList,this);
    private ListView lstv_quality;
    private ToggleButton tgbtn_qualityCheck;
    private ListView lstv_tunerData;
    private ArrayList<TunerData>tunerDataList=new ArrayList<>();
    private TunerDataAdapter tunerDataAdapter =new TunerDataAdapter(tunerDataList,this);
    private TextView tv_successCount;
    private TextView tv_failCount;
    private TextView tv_qualityTime;
    private long successCount;
    private long failCount;
    private long checkQualityTimeLength;
    private double freq=98;
    private int toneLeft=36;
    private int toneRight=45;
    private TextView tv_dataFileName;
    private TextView tv_dataPassTime;
    private long data_count;
    private TextView tv_appVersion;
    private Button btn_upGrade;
    private RadioGroup rdgp_isKeepRecord;
    private Button btn_deleteAllRecord;
    private boolean isKeepRecord;
    private LinearLayout ll_recordId;
    private TunerDataListener tunerDataListener;
    private Button btn_setConfig;
    private HorizontalScrollView scrv_viewContainer;
    private WaveView wv_SNRWaveView;
    private ToggleButton tgbtn_isKeepBandData;
    private boolean isKeepBandData;
    private Button btn_showLog;


    public static void startActivity(Activity context){
        if(CDRadioApplication.getDsp()!=null){
            context.startActivity(new Intent(context, BusinessDemo.class));
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_latest_demo;
    }

    @Override
    protected void initView() {
        btn_back= (Button) findViewById(R.id.activity_biz_btn_back);
        tv_dspVersion= (TextView) findViewById(R.id.activity_biz_action_bar_title);
        btn_dspInfo= (Button) findViewById(R.id.activity_biz_btn_dsp_info);
        edt_setFreq= (EditText) findViewById(R.id.widget_dsp_config2_edt_freq);
        edt_toneLeft= (EditText) findViewById(R.id.widget_dsp_config2_edt_tone_left);
        edt_toneRight= (EditText) findViewById(R.id.widget_dsp_config2_edt_tone_right);
        rdgp_format= (RadioGroup) findViewById(R.id.activity_biz_rdgp_display_format);
        tgbtn_startBiz = (ToggleButton) findViewById(R.id.activity_biz_tgbtn_start_biz);
        btn_resetConnection= (Button) findViewById(R.id.activity_biz_btn_reset_connection);
        lstv_bizData= (ListView) findViewById(R.id.activity_biz_lstv_biz_data);
        tv_bizDataCount= (TextView) findViewById(R.id.activity_biz_tv_biz_data_count);
        tv_missCount= (TextView) findViewById(R.id.activity_biz_tv_miss_count);
        tgbtn_startTuner = (ToggleButton) findViewById(R.id.activity_biz_tgbtn_start_tuner);
        lstv_quality= (ListView) findViewById(R.id.activity_biz_lstv_data_quality);
        tgbtn_qualityCheck= (ToggleButton) findViewById(R.id.activity_biz_tgbtn_quality_check);
        lstv_tunerData= (ListView) findViewById(R.id.activity_biz_lstv_tuner_data);
        tv_successCount= (TextView) findViewById(R.id.widget_statistic_tv_success_count);
        tv_failCount= (TextView) findViewById(R.id.widget_statistic_tv_fail_count);
        tv_qualityTime= (TextView) findViewById(R.id.widget_statistic_tv_pass_time);
        tv_dataFileName= (TextView) findViewById(R.id.activity_biz_tv_data_file_name);
        tv_dataPassTime= (TextView) findViewById(R.id.activity_biz_tv_file_time_phrase);
        tv_appVersion= (TextView) findViewById(R.id.activity_biz_action_bar_tv_app_version);
        btn_upGrade= (Button) findViewById(R.id.activity_biz_btn_upgrade);
        rdgp_isKeepRecord = (RadioGroup) findViewById(R.id.activity_biz_rdgp_is_keep_record);
        btn_deleteAllRecord= (Button) findViewById(R.id.activity_biz_btn_delete_all_files);
        ll_recordId= (LinearLayout) findViewById(R.id.activity_biz_ll_record_id);
        btn_setConfig= (Button) findViewById(R.id.widget_dsp_config2_btn_confirm);
        scrv_viewContainer= (HorizontalScrollView) findViewById(R.id.activity_biz_scrv_view_container);
        wv_SNRWaveView= (WaveView) findViewById(R.id.activity_biz_wv_snr_wave_view);
        tgbtn_isKeepBandData = (ToggleButton) findViewById(R.id.activity_biz_tgbtn_is_keep_band_data);
        btn_showLog= (Button) findViewById(R.id.activity_biz_btn_show_log);

    }

    @Override
    protected void initData() {

        if(Build.VERSION.SDK_INT>=23){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 194);
        }

        dspManager=DSPManager.getDSPManager();
        if(CDRadioApplication.getDsp()!=null){
            dspManager.getDSPVersionInfo(new RequestCallBack() {
                @Override
                public void onGetAckBean(AckBean ackBean) {
                    tv_dspVersion.setText(ackBean.getVersionInfo());
                }
            });
        }

        rdgp_format.check(R.id.activity_biz_rbtn_format_hex);
        rdgp_isKeepRecord.check(R.id.activity_biz_rbtn_do_not_keep_record);

        try {
            String versionName = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tv_appVersion.setText("当前APP版本： "+versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        lstv_bizData.setAdapter(bizDataAdapter);
        lstv_quality.setAdapter(qualityAdapter);
        lstv_tunerData.setAdapter(tunerDataAdapter);

        scrv_viewContainer.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case 194:
                if(grantResults[0]==PackageManager.PERMISSION_DENIED){
                    ToastUtil.showToast("本程序需要SD卡写入权限，否则会报错");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void initListener() {
        //显示dsp版本号
        tv_dspVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(CDRadioApplication.getDsp()!=null){
                   dspManager.getDSPVersionInfo(new RequestCallBack() {
                       @Override
                       public void onGetAckBean(AckBean ackBean) {
                           tv_dspVersion.setText(ackBean.getVersionInfo());
                       }
                   });
               }else {
                   tv_dspVersion.setText("DSP链接失败，请重新尝试");
               }
            }
        });
        //后退
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //查看dsp详情
        btn_dspInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DSPInfo.startActivity(BusinessDemo.this);
            }
        });

        //设置dsp参数
        btn_setConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s_freq = edt_setFreq.getText().toString().trim();
                if (!TextUtils.isEmpty(s_freq)) {
                    freq = Double.valueOf(s_freq);
                }
                String s_toneLeft = edt_toneLeft.getText().toString().trim();
                if (!TextUtils.isEmpty(s_toneLeft)) {
                    toneLeft = Integer.valueOf(s_toneLeft);
                }
                String s_toneRight = edt_toneRight.getText().toString().trim();
                if (!TextUtils.isEmpty(s_toneRight)) {
                    toneRight = Integer.valueOf(s_toneRight);
                }
                try {
                    if (dspManager.apiOpenCDRadio(freq, toneLeft, toneRight)) {
                        ToastUtil.showToast("设置参数成功");
                    } else {
                        ToastUtil.showToast("设置参数失败");
                    }
                } catch (DSPManager.FreqOutOfRangeException e) {
                    e.printStackTrace();
                }
            }
        });


        //选择业务数据显示格式
        rdgp_format.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton child = (RadioButton) rdgp_format.getChildAt(0);
                if (child.isChecked()) {
                    bizDataAdapter.changeFormat(true);
                } else {
                    bizDataAdapter.changeFormat(false);
                }
            }
        });

        //选择是否保存数据到本地
        rdgp_isKeepRecord.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton child = (RadioButton) rdgp_isKeepRecord.getChildAt(0);
                isKeepRecord = !child.isChecked();
            }
        });

        //选择是否保存基带数据到本地
        tgbtn_isKeepBandData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isKeepBandData = isChecked;
            }
        });

        //启动/停止业务数据
        tgbtn_startBiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tgbtn_startBiz.isChecked()) {
                    boolean isSuccess = dspManager.apiGetService(BusinessDemo.this, 88, (byte) 33, new BusinessDataListener() {
                        @Override
                        public void preTask() {
                            bizDataList.clear();
                            bizDataAdapter.notifyDataSetChanged();
                            TimingUtils.startTiming(new TimingUtils.TimeUpdater() {
                                @Override
                                public void onUpdate(long s) {
                                    tv_dataPassTime.setText(s + "");
                                }
                            });
                            data_count = 0;
                            tv_bizDataCount.setText("返回数量：0");
                            tv_missCount.setText("跳帧数量: 0");
                            lstv_tunerData.setVisibility(View.GONE);
                            lstv_bizData.setVisibility(View.VISIBLE);
                            tgbtn_startTuner.setVisibility(View.GONE);
                            enableWidgets(false);
                            if (isKeepRecord) {
                                ll_recordId.setVisibility(View.VISIBLE);
                                tv_dataFileName.setText(LocalFileUtils.prepareFile("business_data"));
                            } else {
                                ll_recordId.setVisibility(View.GONE);
                            }
                            if (isKeepBandData) {
                                LocalFileUtils.prepareBandDataFile();
                            }

                        }

                        @Override
                        public void onGetBandData(byte[] bandData, int frameId) {
                            if (isKeepBandData) {
                                LocalFileUtils.writeBandFile(bandData, 0, bandData.length);
                            }
                        }

                        @Override
                        public void onGetBizData(final byte[] data) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    bizDataList.add(data);
                                    if (bizDataList.size() > 50) {
                                        bizDataList.remove(0);
                                    }
                                    bizDataAdapter.notifyDataSetChanged();
                                    lstv_bizData.smoothScrollToPosition(bizDataList.size() - 1);
                                    data_count++;
                                    tv_bizDataCount.setText("返回数量：" + data_count);
                                    if (isKeepRecord) {
                                        LocalFileUtils.writeNewFile(data, 0, data.length);
                                        LocalFileUtils.writeNewFile("\r\n".getBytes(), 0, "\r\n".length());
                                    }
                                }
                            });
                        }

                        @Override
                        public void onPacketMiss(final int missCount) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tv_missCount.setText("跳帧数量: " + missCount);
                                }
                            });
                        }

                        @Override
                        public void onServiceStop() {
                            TimingUtils.stopTiming();
                            if (isKeepRecord) {
                                LocalFileUtils.stopWritingNewFile();
                            }
                            if (isKeepBandData) {
                                LocalFileUtils.stopWritingBandFile();
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tgbtn_startTuner.setVisibility(View.VISIBLE);
                                    tgbtn_startBiz.setChecked(false);
                                    enableWidgets(true);
                                }
                            });
                        }
                    });

                    if (!isSuccess) {
                        tgbtn_startBiz.setChecked(false);
                        enableWidgets(true);
                    }

                } else {
                    dspManager.apiStopService();
                }
            }
        });


        //开始/停止原始数据传输
        tgbtn_startTuner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tgbtn_startTuner.isChecked()) {
                    tunerDataListener = new TunerDataListener() {
                        @Override
                        public void preTask() {
                            TimingUtils.startTiming(new TimingUtils.TimeUpdater() {
                                @Override
                                public void onUpdate(long s) {
                                    tv_dataPassTime.setText(s + "");
                                }
                            });
                            tgbtn_startBiz.setVisibility(View.GONE);
                            lstv_bizData.setVisibility(View.GONE);
                            lstv_tunerData.setVisibility(View.VISIBLE);
                            tunerDataList.clear();
                            tunerDataAdapter.notifyDataSetChanged();
                            enableWidgets(false);
                            if (isKeepRecord) {
                                ll_recordId.setVisibility(View.VISIBLE);
                                tv_dataFileName.setText(LocalFileUtils.prepareFile("tuner_data"));
                            } else {
                                ll_recordId.setVisibility(View.GONE);
                            }
                            data_count = 0;
                            tv_missCount.setText("跳帧数量：0");
                            tv_bizDataCount.setText("当前帧序号：0");
                        }

                        @Override
                        public void onReceiveData(byte[] data, final int frameId) {
                            if (isKeepRecord) {
                                LocalFileUtils.writeNewFile(data, 10, 4096);
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tv_bizDataCount.setText("当前帧序号：" + frameId);
                                    tunerDataList.add(new TunerData(frameId, (short) 4106));
                                    if (tunerDataList.size() > 50) {
                                        tunerDataList.remove(0);
                                    }
                                    tunerDataAdapter.notifyDataSetChanged();
                                    lstv_tunerData.smoothScrollToPosition(tunerDataList.size() - 1);
                                }
                            });
                        }


                        @Override
                        public void onPacketMiss(final int missCount) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    tv_missCount.setText("跳帧数量：" + missCount);
                                }
                            });

                        }

                        @Override
                        public void onTunerStop() {
                            TimingUtils.stopTiming();
                            if (isKeepRecord) {
                                LocalFileUtils.stopWritingNewFile();
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    enableWidgets(true);
                                    tgbtn_startBiz.setVisibility(View.VISIBLE);
                                    tgbtn_startTuner.setChecked(false);
                                }
                            });
                        }
                    };
                    boolean isSuccess = dspManager.apiOpenDAQ(BusinessDemo.this, tunerDataListener);
                    if (!isSuccess) {
                        tgbtn_startTuner.setChecked(false);
                    }
                } else {
                    if (!dspManager.apiStopDAQ()) {
                        tgbtn_startTuner.setChecked(true);
                    }
                }

            }
        });



        //检测数据质量
        tgbtn_qualityCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tgbtn_qualityCheck.isChecked()) {
                    boolean isSuccess = dspManager.checkDataQuality(500, new DSPManager.DataQualityMonitor() {
                        @Override
                        public void onCallBack(DataQuality dataQuality) {
                            qualityList.add(dataQuality);
                            qualityAdapter.notifyDataSetChanged();
                            lstv_quality.smoothScrollToPosition(qualityList.size() - 1);
                            successCount += dataQuality.getSuccessCount();
                            failCount += dataQuality.getFailCount();
                            checkQualityTimeLength += 500;
                            updateSNRReads();
                            wv_SNRWaveView.updateView((float) dataQuality.getSnrRead());
                            int gap = wv_SNRWaveView.getValidLength() - scrv_viewContainer.getMeasuredWidth();
                            if (gap > 0) {
                                scrv_viewContainer.smoothScrollTo(gap, 0);
                            } else {
                                scrv_viewContainer.smoothScrollTo(0, 0);
                            }
                        }
                    });
                    if (isSuccess) {
                        showLog("bizDemo:开始质量监测");
                        qualityList.clear();
                        qualityAdapter.notifyDataSetChanged();
                        successCount = 0;
                        failCount = 0;
                        checkQualityTimeLength = 0;
                        updateSNRReads();
                        scrv_viewContainer.setVisibility(View.VISIBLE);
                        wv_SNRWaveView.reset();
                    } else {
                        tgbtn_qualityCheck.setChecked(false);
                    }
                } else {
                    dspManager.stopCheckDataQuality();
                }
            }
        });

        //重新链接DSP
        btn_resetConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dspManager.resetConnection()) {
                    ToastUtil.showToast("重置成功");
                } else {
                    ToastUtil.showToast("重置失败");
                }
            }
        });

        //升级DSP
        btn_upGrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dspManager.prepareDSPUpgrade(LocalFileUtils.getUpgradeSourceFile(), new DSPManager.UpgradeListener() {
                    @Override
                    public void onProgress(int totalFrame, int currentFrame) {
                        showLog("总帧数：" + totalFrame + ",当前帧序号：" + currentFrame);
                    }

                    @Override
                    public void onFinish(boolean isSuccess) {
                        if (isSuccess) {
                            showLog("升级完成");
                        } else {
                            showLog("升级失败");
                        }
                    }
                }, true);
            }
        });

        //清除所有记录
        btn_deleteAllRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogueUtils.showDialogue(
                        BusinessDemo.this,
                        "点击确定将把设备中所有原始数据、业务数据的历史测试记录一次清空，您确定要这样吗？",
                        new Runnable() {
                            @Override
                            public void run() {
                                if (LocalFileUtils.deleteAllFiles()) {
                                    ToastUtil.showToast("已经把本地所有测试记录删除");
                                } else {
                                    ToastUtil.showToast("找不到测试记录");
                                }
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                            }
                        });

            }
        });

        btn_showLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistoryLog();
            }
        });

    }

    private void showHistoryLog() {
        ArrayList<String> historyLog1 = LogUtils.historyLog;
        ArrayList<String> historyLog2 = com.skycaster.l_cdradiorxdemo.utils.LogUtils.historyLog;
        LinearLayout rootView= (LinearLayout) getLayoutInflater().inflate(R.layout.widget_log_layout,null);
        RecyclerView recyclerView= (RecyclerView) rootView.findViewById(R.id.log_console);
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.addAll(historyLog1);
        arrayList.addAll(historyLog2);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        HistoryLogAdapter adapter=new HistoryLogAdapter(arrayList,this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                Paint paint=new Paint();
                paint.setColor(Color.BLACK);
                int childCount = parent.getChildCount();
                int width = parent.getWidth();
                for(int i=0;i<childCount;i++){
                    View child = parent.getChildAt(i);
                    c.drawLine(0,child.getBottom(),width,child.getBottom(),paint);
                }
            }
        });
        recyclerView.setAdapter(adapter);
        LogUtils.historyLog.clear();
        com.skycaster.l_cdradiorxdemo.utils.LogUtils.historyLog.clear();
        new AlertDialog.Builder(this)
                .setView(rootView)
                .setCancelable(true)
                .create()
                .show();
        historyLog1.clear();
        historyLog2.clear();
    }

    private void updateSNRReads(){
        tv_successCount.setText(successCount+"");
        tv_failCount.setText(""+failCount);
        tv_qualityTime.setText(checkQualityTimeLength /1000+"");
    }

    /**
     * 启动/取消数据传输时一些基本功能
     * @param isEnable true为恢复功能，false为暂时屏蔽
     */
    private void enableWidgets(boolean isEnable){
        //是否保存传输数据
        for(int i=0;i<rdgp_isKeepRecord.getChildCount();i++){
            rdgp_isKeepRecord.getChildAt(i).setEnabled(isEnable);
        }
        tgbtn_isKeepBandData.setEnabled(isEnable);
        //设置dsp参数
        btn_setConfig.setEnabled(isEnable);
        btn_setConfig.setClickable(isEnable);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
