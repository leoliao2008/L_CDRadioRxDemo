package com.skycaster.l_cdradiorxdemo.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 廖华凯 on 2016/12/15.
 */
public class BizDataAdapter extends BaseAdapter {
    private ArrayList<byte[]>list;
    private Context context;
    private boolean isHex=true;
    private static final String TARGET ="0xD3 0x00";
    private final Pattern pattern;


    public void changeFormat(boolean isHex){
        this.isHex=isHex;
    }

    public BizDataAdapter(Context context, ArrayList<byte[]> list) {
        this.context = context;
        this.list = list;
        pattern = Pattern.compile(TARGET);
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
                sb.append("0x").append(String.format(Locale.CHINA, "%02X", b)).append(" ");
            }
            ((TextView)convertView).setText(outLineString(sb.toString().trim()));
        }else {
            ((TextView)convertView).setText(new String(temp));
        }
        return convertView;
    }

    private SpannableString outLineString(String string){
        SpannableString sps=new SpannableString(string);
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()){
            sps.setSpan(new ForegroundColorSpan(Color.RED),matcher.start(),matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return sps;
    }
}
