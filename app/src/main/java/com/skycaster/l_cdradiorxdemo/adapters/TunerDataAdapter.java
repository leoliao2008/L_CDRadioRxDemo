package com.skycaster.l_cdradiorxdemo.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skycaster.l_cdradiorxdemo.R;
import com.skycaster.l_cdradiorxdemo.beans.TunerData;

import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2016/11/23.
 */
public class TunerDataAdapter extends BaseAdapter {
    private ArrayList<TunerData>list;
    private Activity context;

    public TunerDataAdapter(ArrayList<TunerData> list, Activity context) {
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
        ViewHolder vh;
        if(convertView==null){
            convertView=View.inflate(context,R.layout.item_listview_brand_transfer,null);
            vh=new ViewHolder(convertView);
            convertView.setTag(vh);
        }else {
            vh= (ViewHolder) convertView.getTag();
        }
        vh.getTv_frameId().setText(""+list.get(position).getFrameId());
        vh.getTv_validLength().setText(""+list.get(position).getValidLength());
        return convertView;
    }

    private class ViewHolder{
        private TextView tv_frameId;
        private TextView tv_validLength;

        public ViewHolder(View rootView) {
            this.tv_frameId = (TextView) rootView.findViewById(R.id.lstv_brand_transf_frame_id);
            this.tv_validLength = (TextView) rootView.findViewById(R.id.lstv_brand_transf_valid_length);
        }

        public TextView getTv_frameId() {
            return tv_frameId;
        }

        public TextView getTv_validLength() {
            return tv_validLength;
        }
    }
}
