package com.skycaster.skc_cdradiorx.beans;


/**
 * 移动端发送给DSP的请求
 * @author 廖华凯
 *
 */
public class RequestBean {
	private byte initCode=0x7e;//起始码
	private byte frameType=0x01;//帧类型
	private short commandCode;//命令码
	private short totalFrame;//总帧数
	private short frameId;//当前帧序号
	private short validLength;//有效数据长度
	private byte[] data=new byte[4096];//请求体输送给DSP的数据，固定大小为4096byte，不足部分用0xff填充
	private DSPParam dspConfig;//DSP配置的参数
	private LvParameter lvParameter;//滤波参数
	/**
	 * 初始化一个简单的请求体，默认总帧数1，当前帧序号1，有效数据长度0，数据冗余部分用0xff填充。
	 * @param commandCode 命令码
	 */
	public RequestBean(int commandCode) {
		super();
		this.commandCode=(short) commandCode;
		this.totalFrame = 1;
		this.frameId = 1;
		this.validLength = 0;
		for(int i=0;i<4096;i++){
			data[i]=(byte) 0xff;
		}
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
	public void setCommandCode(int commandCode) {
		this.commandCode = (short) commandCode;
	}
	public short getTotalFrame() {
		return totalFrame;
	}
	public void setTotalFrame(int totalFrame) {
		this.totalFrame = (short) totalFrame;
	}
	public short getFrameId() {
		return frameId;
	}
	public void setFrameId(int frameId) {
		this.frameId = (short) frameId;
	}
	public short getValidLength() {
		return validLength;
	}
	public void setValidLength(int validLength) {
		this.validLength = (short) validLength;
	}
	public DSPParam getDspConfig() {
		return dspConfig;
	}

	/**
	 * 当需要设置DSP基本参数时，通过此方法把参数放到请求体主体内容中。
	 * 注意：如果之前已经给请求体设置了滤波参数，这里将会把滤波参数去除。
	 * @param dspConfig DSP参数对象
	 */
	public void setDspConfig(DSPParam dspConfig) {
		this.dspConfig = dspConfig;
		this.lvParameter=null;
	}
	public LvParameter getLvParameter() {
		return lvParameter;
	}

	/**
	 * 当需要设置DSP滤波参数时，通过此方法把参数放到请求体主体内容中。
	 * 注意：如果之前已经给请求体设置了基本参数，这里将会把基本参数去除。
	 * @param lvParameter 滤波参数对象
	 */
	public void setLvParameter(LvParameter lvParameter) {
		this.lvParameter = lvParameter;
		this.dspConfig=null;
	}
	public byte[] getData() {
		return data;
	}
	/**
	 * 设置请求体的主要内容，即后面4096byte的内容。
	 * @param data 一个大小不超过4096byte的字节数组，将复制给请求体
	 * @throws Exception 字节数组大小超出4096byte后将触发此项
	 */
	public void setData(byte[] data) throws DataOverSizeException {
		if(data.length>4096){
			throw new DataOverSizeException();
		}else{
			validLength= (short) data.length;
			System.arraycopy(data,0,this.data,0,validLength);
		}
	}

	public class DataOverSizeException extends Exception{
        public DataOverSizeException() {
            this("字节数组大小不可以超出4096byte");
        }
        private DataOverSizeException(String detailMessage) {
			super(detailMessage);
		}
	}

    /**
     * 把该请求体转成一个4106byte的数组，前面10byte包含了请求内容的基本信息，如命令码、有效长度、帧序号、
	 * 总帧序号等等，后面4096byte包含了请求体的主体数据。此数组作为请求，可以通过USB协议传输给DSP。
     * @return 拟通过USB协议传输给DSP的最终请求数据
     */
	public byte[] toBytes() {
		byte[] request=new byte[4106];//目标数组
		request[0]=initCode;//初始码，1个字节
		request[1]=frameType;//帧类型，1个字节
		request[2]= (byte) commandCode;//命令码，低8位
		request[3]= (byte) (commandCode>>8);//命令码，高8位
		request[4]= (byte) totalFrame;//总帧数低8位
		request[5]= (byte) (totalFrame>>8);//总帧数高8位
		request[6]= (byte) frameId;//帧序号低8位
		request[7]= (byte) (frameId>>8);//帧序号高8位
		byte[] temp;//临时容器，用来存放有效数据
		if(dspConfig!=null){
			temp = dspConfig.toBytes();
			validLength=12;
		}else if(lvParameter!=null){
			temp = lvParameter.toBytes();
			validLength= (short) temp.length;
        }else {
			temp=data;
		}
		request[8]= (byte) validLength;//有效数据长度低8位
		request[9]= (byte) (validLength>>8);//有效数据长度高8位
		System.arraycopy(temp,0,request,10,temp.length);//把容器的内容复制到数组后面
		return request;
	}

    @Override
    public String toString() {
        return "RequestBean{" +
                "initCode=" + "0x"+Integer.toHexString(initCode) +
                ", frameType=" + "0x"+Integer.toHexString(frameType)  +
                ", commandCode=" + "0x"+Integer.toHexString(commandCode)  +
                ", totalFrame=" + totalFrame +
                ", frameId=" + frameId +
                ", validLength=" + validLength +
                '}';
    }
}
