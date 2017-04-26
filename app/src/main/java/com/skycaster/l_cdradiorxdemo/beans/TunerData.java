package com.skycaster.l_cdradiorxdemo.beans;

/**
 * Created by 廖华凯 on 2016/11/23.
 */
public class TunerData {
    private int frameId;
    private short validLength;

    public TunerData(int frameId, short validLength) {
        this.frameId = frameId;
        this.validLength = validLength;
    }

    public int getFrameId() {
        return frameId;
    }

    public void setFrameId(short frameId) {
        this.frameId = frameId;
    }

    public short getValidLength() {
        return validLength;
    }

    public void setValidLength(short validLength) {
        this.validLength = validLength;
    }
}
