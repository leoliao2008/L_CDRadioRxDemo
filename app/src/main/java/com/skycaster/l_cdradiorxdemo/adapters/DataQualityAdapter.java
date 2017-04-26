package com.skycaster.l_cdradiorxdemo.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skycaster.l_cdradiorxdemo.R;
import com.skycaster.skc_cdradiorx.beans.DataQuality;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2016/12/15.
 */
public class DataQualityAdapter extends BaseAdapter {
    private ArrayList<DataQuality>list;
    private Context context;
    private DecimalFormat decimalFormat =new DecimalFormat("#.00");

    public DataQualityAdapter(ArrayList<DataQuality> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null){
            convertView=View.inflate(context, R.layout.item_lstv_data_quality,null);
            viewHolder=new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        DataQuality temp = list.get(position);
        viewHolder.getTv_status().setText(temp.getStatus());
        viewHolder.getTv_successCount().setText(temp.getSuccessCount()+"");
        viewHolder.getTv_failCount().setText(temp.getFailCount() + "");
        viewHolder.getTv_SNR().setText(decimalFormat.format(temp.getSnrRead()));
        viewHolder.getTv_timeOffset().setText(temp.getOffSets()[1]+"");
        viewHolder.getTv_freqOffset().setText(temp.getOffSets()[0]+"");
        return convertView;
    }

    private class ViewHolder{
        private TextView tv_successCount;
        private TextView tv_failCount;
        private TextView tv_SNR;
        private TextView tv_status;
        private TextView tv_timeOffset;
        private TextView tv_freqOffset;

        public ViewHolder(View convertView) {
            this.tv_successCount = (TextView) convertView.findViewById(R.id.item_data_quality_tv_success);
            this.tv_failCount = (TextView) convertView.findViewById(R.id.item_data_quality_tv_fail);
            this.tv_SNR = (TextView) convertView.findViewById(R.id.item_data_quality_tv_SNR);
            this.tv_status= (TextView) convertView.findViewById(R.id.item_data_quality_tv_status);
            this.tv_timeOffset= (TextView) convertView.findViewById(R.id.item_data_quality_tv_time_offset);
            this.tv_freqOffset= (TextView) convertView.findViewById(R.id.item_data_quality_tv_freq_offset);
        }

        public TextView getTv_successCount() {
            return tv_successCount;
        }

        public TextView getTv_failCount() {
            return tv_failCount;
        }

        public TextView getTv_SNR() {
            return tv_SNR;
        }

        public TextView getTv_status() {
            return tv_status;
        }

        public TextView getTv_timeOffset() {
            return tv_timeOffset;
        }

        public TextView getTv_freqOffset() {
            return tv_freqOffset;
        }
    }
}
