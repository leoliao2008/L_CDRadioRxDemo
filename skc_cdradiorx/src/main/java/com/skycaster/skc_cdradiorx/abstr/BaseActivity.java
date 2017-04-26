package com.skycaster.skc_cdradiorx.abstr;

import android.os.Bundle;
import android.view.Window;

import com.skycaster.skc_cdradiorx.bases.CDRadioActivity;
import com.skycaster.skc_cdradiorx.utils.LogUtils;


/**
 * Created by 廖华凯 on 2016/12/26.
 */
public abstract class BaseActivity extends CDRadioActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        initView();
        initData();
        initListener();
    }

    /**
     * 给activity设置页面
     * @return activity对应的layout
     */
    protected abstract int getContentView();

    /**
     * 初始化activity的各种view
     */
    protected abstract void initView();

    /**
     * 初始化activity的一些基础数据
     */
    protected abstract void initData();

    /**
     * 初始化activity的监听、点击事件
     */
    protected abstract void initListener();

    protected void showLog(String s){
        LogUtils.showLog(BaseActivity.this, s);
    }

}
