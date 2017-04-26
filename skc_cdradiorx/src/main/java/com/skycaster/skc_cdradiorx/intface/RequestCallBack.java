package com.skycaster.skc_cdradiorx.intface;

import com.skycaster.skc_cdradiorx.beans.AckBean;


/**
 * 回调，用来接收DSP应答
 */
public interface RequestCallBack {
    /**
     * 返回DSP应答对象
     * @param ackBean DSP应答对象
     */
    void onGetAckBean(AckBean ackBean);
}
