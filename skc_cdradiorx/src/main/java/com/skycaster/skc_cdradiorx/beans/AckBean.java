package com.skycaster.skc_cdradiorx.beans;

/**
 * 封装好的DSP应答对象
 * Created by 廖华凯 on 2016/11/21.
 */
public class AckBean {
    private byte initCode;//起始码
    private byte frameType;//帧类型
    private short commandCode;//命令码
    private int totalFrame;//总帧数
    private int frameId;//当前帧序号
    private short validLength;//有效数据长度
    private short ackStatusCode;//本次应答的状态码
    private String statusDescription="获取数据失败";//本次应答的状态码对应的文字描述
    private byte[] data=new byte[4096];//返回数据内容主体
    private String versionInfo="获取数据失败";//软件版本号的文字描述
    private String DSPStatus="获取数据失败";//DSP工作状态的文字描述

    /**
     * 根据DSP返回的数据，生成此应答对象
     * @param ack DSP返回来的数组，大小为4106位
     */
    public AckBean(byte[] ack) {
        initCode=ack[0];
        frameType=ack[1];
        commandCode= (short) (ack[3]<<8|ack[2]&0xff);
        totalFrame= (short) (ack[5]<<8|ack[4]&0xff);
        totalFrame= (ack[5]<<8|ack[4]&0xff)&0x0ffff;
        frameId=  (ack[7]<<8|ack[6]&0xff)&0x0ffff;
        validLength= (short) (ack[9]<<8|ack[8]&0xff);
        ackStatusCode = (short) (ack[11]<<8|ack[10]&0xff);
        statusDescription=getAckStatusDescription();
        //根据命令码生成内容主体
        switch (commandCode){
            case 0x0005://查询设备版本
                byte[] versionInfoBytes=new byte[validLength-2];
//                for(int i=0;i<versionInfoBytes.length;i++){
//                    versionInfoBytes[i]=ack[12+i];
//                }
                System.arraycopy(ack,12,versionInfoBytes,0,validLength-2);
                versionInfo=new String(versionInfoBytes);//解析版本信息
                break;
            case 0x0006://读取设备状态
                short DeviceStatusCode= (short) (ack[13]<<8|ack[12]&0xff);
                setDSPStatus(DeviceStatusCode);
                break;
            case 0x000B://上传原始采集数据
                break;
            case 0x000E://上传业务数据
                break;
            default://其他命令
                break;
        }
    }

    /**
     * 获取本次应答的状态码，如果是1，则代表正常
     * @return  本次应答的状态码
     */
    public short getAckStatusCode() {
        return ackStatusCode;
    }

    /**
     * 获取DSP当前工作状态
     * @return 相应的状态文字描述
     */
    public String getDSPStatus() {
        return DSPStatus;
    }

    /**
     * 设置DSP设备目前的状态
     * @param DeviceStatusCode 状态码
     */
    private void setDSPStatus(short DeviceStatusCode) {
        switch (DeviceStatusCode){
            case 1:
                DSPStatus="设备启动并空闲";
                break;
            case 2:
                DSPStatus="设备已关闭";
                break;
            case 3:
                DSPStatus="正在传输业务数据";
                break;
            case 4:
                DSPStatus="正在传输原始数据";
                break;
            default:
                DSPStatus="状态码不存在";
                break;
        }
    }

    /**
     * 获取本次应答的状态码对应的文字描述
     * @return 相应字符串
     */
    public String getAckStatusDescription(){
        String dsc;
        switch (ackStatusCode){
            case 1:
                dsc="正常";
                break;
            case 2:
                dsc="不支持的命令";
                break;
            case 3:
                dsc="设备忙";
                break;
            case 4:
                dsc="验校错误";
                break;
            case 5:
                dsc="参数错误";
                break;
            case 6:
                dsc="设备已关闭";
                break;
            case 7:
                dsc="设备认证错误";
                break;
            default:
                dsc="无效的状态码";
                break;
        }
        return dsc;
    }

    public AckBean() {
    }

    /**
     * 获取DSP系统版本信息
     * @return DSP系统版本信息的文字描述
     */
    public String getVersionInfo() {
        return versionInfo;
    }

    public byte getInitCode() {
        return initCode;
    }

    public void setInitCode(byte initCode) {
        this.initCode = initCode;
    }

    public byte getFrameType() {
        return frameType;
    }

    public void setFrameType(byte frameType) {
        this.frameType = frameType;
    }

    public short getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(short commandCode) {
        this.commandCode = commandCode;
    }

    public int getTotalFrame() {
        return totalFrame;
    }

    public void setTotalFrame(short totalFrame) {
        this.totalFrame = totalFrame;
    }

    public short getValidLength() {
        return validLength;
    }

    public void setValidLength(short validLength) {
        this.validLength = validLength;
    }

    public int getFrameId() {
        return frameId;
    }

    public void setFrameId(short frameId) {
        this.frameId = frameId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "AckBean{" +
                "initCode=" + "0x"+Integer.toHexString(initCode) +
                ", frameType=" + "0x"+Integer.toHexString(frameType) +
                ", commandCode=" + "0x"+Integer.toHexString(commandCode) +
                ", totalFrame=" + totalFrame +
                ", frameId=" + frameId +
                ", validLength=" + validLength +
                ", statusDescription='" + statusDescription+
                '}';
    }
}
