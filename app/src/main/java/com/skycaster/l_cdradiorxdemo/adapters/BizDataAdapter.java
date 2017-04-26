package com.skycaster.l_cdradiorxdemo.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2016/12/15.
 */
public class BizDataAdapter extends BaseAdapter {
    private ArrayList<byte[]>list;
    private Context context;
    private boolean isHex=true;


    public void changeFormat(boolean isHex){
        this.isHex=isHex;
    }

    public BizDataAdapter(Context context, ArrayList<byte[]> list) {
        this.context = context;
        this.list = list;
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
        if(convertView==null){
            convertView=new TextView(context);
        }
        byte[] temp = list.get(position);
        if(isHex){
            StringBuffer sb=new StringBuffer();
            for(byte b:temp){
                sb.append(Integer.toHexString(b)).append(" ");
            }
            ((TextView)convertView).setText(sb.toString());
        }else {
            ((TextView)convertView).setText(new String(temp));
        }
        return convertView;
    }
}
