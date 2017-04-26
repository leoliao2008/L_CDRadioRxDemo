package com.skycaster.l_cdradiorxdemo.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by 廖华凯 on 2017/4/7.
 */
public class HistoryLogAdapter extends RecyclerView.Adapter<HistoryLogAdapter.HistoryLogViewHolder> {

    private ArrayList<String>list;
    private Activity context;

    public HistoryLogAdapter(ArrayList<String> list, Activity context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public HistoryLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView textView=new TextView(context);
        RecyclerView.LayoutParams params=new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(15);
        textView.setPadding(2,2,2,2);
        return new HistoryLogViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(HistoryLogViewHolder holder, int position) {
        holder.getTextView().setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HistoryLogViewHolder extends RecyclerView.ViewHolder{

        private TextView textView;

        public HistoryLogViewHolder(View itemView) {
            super(itemView);
            textView= (TextView) itemView;
        }

        public TextView getTextView() {
            return textView;
        }
    }
}
