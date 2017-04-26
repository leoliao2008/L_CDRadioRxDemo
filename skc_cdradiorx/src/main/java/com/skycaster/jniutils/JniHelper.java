package com.skycaster.jniutils;

/**
 * Created by 廖华凯 on 2016/12/11.
 */
public class JniHelper {
    private static JniHelper jniHelper;

    private JniHelper(){}


    /**
     * 使用单例模式获得实现类
     * @return 返回实现类
     */
    public synchronized static JniHelper getJniHelper(){
        if(jniHelper==null){
            jniHelper=new JniHelper();
            System.loadLibrary("locSDK1");
        }
        return jniHelper;
    }


    /**
     * 初始化时用，根据当前滤波参数的类型获取相应的ToneIndexLeft、ToneIndexRight，发送给so库
     * @param ToneIndexLeft 固定36
     * @param ToneIndexRight 固定45
     * @return 成功则返回true,失败则返回false
     */
    public native boolean cdradio_rx_cfg_spectrum(int ToneIndexLeft, int ToneIndexRight);

    /**
     * 根据上一步两个参数获取两个滤波参数
     * @param FmfCoef 一个长度140bytes的数组，每两个相邻byte成一个short类型，低位在前，高位在后，最后将
     *                转成一个长度为70的short数组,作为高通滤波参数
     * @param BpfCoef 一个长度180bytes的数组，每两个相邻byte成一个short类型，低位在前，高位在后，最后将
     *                转成一个长度为90的short数组,作为带通滤波参数
     */
    public native void cdradio_rx_get_fir_coef(byte FmfCoef[], byte BpfCoef[]);

    /**
     * 启动so库，注意启动之前要先初始化频点、高通参数、带通参数
     * @return true则成功，false则失败
     */
    public native boolean cdradio_rx_start();

    /**
     * 把基带数据作为参数传给so库处理
     * @param  DataIn 基带数据，大小为4096bytes
     * @param DataLen 固定为1024
     * @return true则成功，false则失败
     */
    public native boolean cdradio_rx_write_baseband(byte[] DataIn, int DataLen);

    /**
     * 根据上一步的基带参数从so库获得时偏及频偏
     * @param fo 长度为2的数组，index0是时偏，index1是频偏
     * @return true则成功，false则失败。并不会每次都返回true，只在true的情况下调用参数即可。
     */
    public native boolean cdradio_rx_get_offset(int[] fo);

    /**
     * 根据上一步的基带数据获得最终业务数据
     * @param ServicesData 存放业务数据的数组
     * @param DataLen 业务数据目标长度，只有达标才返回
     * @param FormCode 暂定是 33
     * @return true则成功，false则失败，只有返回数据长度与DataLen一致时，才会返回true
     */
    public native boolean cdradio_rx_read_service_frame(byte[] ServicesData, int DataLen, byte FormCode);

    /**
     *根据上一步的基带数据获得最终业务数据
     * @param ServicesData 存放业务数据的数组
     * @param DataLen 业务数据长度上限
     * @param FormCode 暂定是 33
     * @return 业务数据有效长度
     */
    public native int cdradio_rx_read_services_bytes(byte[] ServicesData, int DataLen, byte FormCode);

    /**
     * 检查dsp数据传输质量，可以隔一段时间操作一下，获得该段时间传输质量的统计值
     * @param State 当前so库工作状态
     * @param Snr 信噪比，即有效解析的数据的比例，数组长度为1
     * @param LdpcCnt 一个int数组，其中index 0 表示距离上一次调用此方法后到本次累计解析成功的数量，index 1 表示解析失败的数量
     */
    public native void cdradio_rx_get_state(byte[] State, double[] Snr, int[] LdpcCnt);

    /**
     * 关闭so库
     */
    public native void cdradio_rx_stop();


}
